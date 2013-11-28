(ns dandy.gui.main-behaviour
  (:require [clojure.core.async :refer :all]
            [clojure.java.io :as io]
            [seesaw.core :as s]
            [seesaw.dnd :as dnd]
            [seesaw.bind :as bind]
            [dandy.layer :refer (apply-layer)]
            [dandy.resize :refer (resize)])
  (:use [seesaw.chooser :only (choose-file)]
        [dandy.prefs :only (prefs)])
  (:import [javax.imageio ImageIO ImageWriteParam IIOImage]))

(def files (atom []))
(def done-count (atom 0))

(def icons (:icons @prefs))
(def icon-names (keys icons))
(def to-show (zipmap icon-names (map #(:show-by-default %1) (vals icons))))
(def to-position (zipmap icon-names (map #(:position %1) (vals icons))))
(def pos-groups (zipmap icon-names (map (fn [_] (s/button-group)) (vals icons))))

(def status-text-notifier (bind/notify-later))

(defn files-selected [fc, fs]
  (bind/notify status-text-notifier
               (str (count fs) " files ready to be converted"))
  (reset! files fs))

(defn open-file-dialog [& args]
  (choose-file :type :open
               :multi? true
               :filters [["Images" (seq (ImageIO/getReaderFormatNames))]]
               :selection-mode :files-only
               :dir (get @prefs :dir)
               :success-fn files-selected))

(defn dnd-handler-fn [data]
  (let [files (map #(io/file %1) (:data data))]
    (files-selected nil files)))

(def dnd-handler (dnd/default-transfer-handler
                   :import [dnd/file-list-flavor dnd-handler-fn]
                   :export {}))

(defn- generate-filename [directory basename ext icon-ids]
  (let [pos-ext (apply str (map (fn [p] (str "_" (name p))) icon-ids))]
    (str directory "/" basename "_resized" pos-ext "." ext)))

(defn- load-icon
  [id]
  (ImageIO/read (io/file (get-in @prefs [:icons id :path]))))

(defn- get-icon-ids
  "Gets the IDs of selected icon checkboxes"
  [root]
  (map (comp name #(s/config % :id))
       (filter s/selection
               (s/select root [:.icon-checkbox]))))

(defn- get-positions
  [icon-ids]
  (map (comp #(s/config % :id) s/selection)
       (-> (select-keys pos-groups icon-ids) sort vals)))

(defn convert-files [e]
  (reset! done-count 0)
  (when (not-empty @files)
    (let [done-chan (chan)
          icon-ids (get-icon-ids (s/to-root e))
          icons (map load-icon icon-ids)
          positions (get-positions icon-ids)]
      (doseq [file @files]
        (go
         (let [image (ImageIO/read file)
               filename (.getName file)
               directory (.getParent file)
               name-parts (-> filename (clojure.string/split #"\."))
               ext (last name-parts)
               basename (clojure.string/join "." (drop-last name-parts))
               resized (-> image resize)
               layered (reduce (fn [i lp] (apply-layer i (first lp) (last lp))) resized (map vector icons positions))
               output-file (io/file (generate-filename directory basename ext icon-ids))
               writer (.next (ImageIO/getImageWritersByFormatName ext))
               write-param (.getDefaultWriteParam writer)
               iioimage (IIOImage. layered nil nil)
               ios (ImageIO/createImageOutputStream output-file)]
           (doto write-param
             (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
             (.setCompressionQuality 1.0))
           (doto writer
             (.setOutput ios)
             (.write nil iioimage write-param)
             (.dispose))
           (doto ios
             (.close))
           ;; (ImageIO/write layered ext output-file)
           (>! done-chan (str "Converting file " (.getPath output-file))))))

      (<!!
       (go
        (doseq [_ @files]
          (swap! done-count inc))))
      (reset! files []) ; reset files list
      (bind/notify status-text-notifier (str @done-count " files converted!")))))

(defn add-behaviours
  "Adds behaviour to the main UI elements"
  [root]

  (defn find-elem [id] (s/select root [id]))

  (bind/bind status-text-notifier
             (bind/property (find-elem :#status-text) :text))

  (s/listen (find-elem :#convert-button)
            :mouse-clicked
            (fn [_] (convert-files root)))

  (s/listen (find-elem :#choose-files)
            :mouse-clicked open-file-dialog)

  root)
