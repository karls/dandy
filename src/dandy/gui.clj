(ns dandy.gui
  (require [seesaw.core :as s]
           [seesaw.dnd :as dnd]
           [dandy.prefs :as prefs]
           [dandy.util :as util]
           [dandy.resize :refer (resize)]
           [dandy.layer :refer (apply-layer)]
           [clojure.core.async :as async]
           [clojure.java.io :as io])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)])
  ;(import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  (import [javax.imageio ImageIO ImageWriteParam IIOImage]))

(def files (atom []))
(def done-count (atom 0))

(def status-text (s/label :text ""))

(def layer-cbs [(s/checkbox :text "Logo" :user-data :new-logo)
                (s/checkbox :text "Uus hind" :user-data :new-price)
                (s/checkbox :text "Uus" :user-data :new)])

(def placement-map {:new-logo (s/button-group)
                    :new-price (s/button-group)
                    :new (s/button-group)})

(defn- pluralise [n s p]
  (if (= n 1)
    s
    p))

(defn get-selected-layers []
  (filter (fn [cb] (s/config cb :selected?)) layer-cbs))

(defn get-user-data-for-layers []
  (map (fn [cb] (s/config cb :user-data)) (get-selected-layers)))

(defn get-position-groups [ids]
  (map placement-map ids))

(defn load-layer [id]
  (let [image-id (get @prefs/prefs id)]
    (ImageIO/read (io/file image-id))))

(defn generate-filename [directory basename ext]
  (let [pos-ext (apply str (map (fn [p] (str "_" (name p))) (get-user-data-for-layers)))]
    (str directory "/" basename "_resized" pos-ext "." ext)))

(defn convert-files [e]
  (reset! done-count 0)
  (when (seq @files) ; recommended idiom for (not (empty? coll))
    (let [done-chan (async/chan)
          layers (map (fn [id] (load-layer id)) (get-user-data-for-layers))
          positions (map (fn [p] (-> p s/selection (s/config :id)))
                         (get-position-groups (get-user-data-for-layers)))]
      (doseq [file @files]
        (async/go
         (let [image (ImageIO/read file)
               filename (.getName file)
               directory (.getParent file)
               name-parts (-> filename (clojure.string/split #"\."))
               ext (last name-parts)
               basename (clojure.string/join "." (drop-last name-parts))
               resized (-> image resize)
               layered (reduce (fn [i lp] (apply-layer i (first lp) (last lp))) resized (map vector layers positions))
               output-file (io/file (generate-filename directory basename ext))
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
           (ImageIO/write layered ext output-file)
           (async/>! done-chan (str "Converting file " (.getPath output-file))))))

      (async/<!!
       (async/go
        (doseq [_ @files]
          (swap! done-count inc))))
      (reset! files []) ; reset files list
      (s/config! status-text :text (str @done-count " "
                                        (pluralise @done-count "pilt" "pilti")
                                        " konverteeritud!")))))

(defn convert-button []
  (s/button :text "Konverteeri" :listen [:action convert-files]))

(defn files-selected [fc, fs]
  (let [fs-count (count fs)]
    (s/text!
     status-text
     (str fs-count " "
          (pluralise fs-count "pilt" "pilti")
          " valmis konverteerimiseks.."))
    (reset! files fs)))

(defn dnd-handler-fn [data]
  (let [files (map (fn [path] io/file path) (:data data))]
    (files-selected nil files)))

(def dnd-handler (dnd/default-transfer-handler
                   :import [dnd/file-list-flavor dnd-handler-fn]
                   :export {}))


(defn open-file-dialog []
  (choose-file :type :open
               :multi? true
               :filters [["Images" (seq (ImageIO/getReaderFormatNames))]]
               :selection-mode :files-only
               :dir (get @prefs/prefs :dir)
               :success-fn files-selected))

(defmulti build-menuitems-for (fn [x] x))
(defmethod build-menuitems-for :edit [_]
  [(s/menu-item :text "Preferences"
                :listen [:action (fn [_] (prefs/make-prefs-dialog))])])

(defmulti build-menu-for (fn [x] x))
(defmethod build-menu-for :edit [_]
  (s/menu :text "Edit" :items (build-menuitems-for :edit)))

(defn build-menubar []
  (s/menubar :items [(build-menu-for :edit)]))

(defn layout []
  (mig-panel :constraints ["gap 5px, ins 5px" "" ""]
             :items [[(s/button :text "Vali pildid"
                                :listen [:action (fn [e] (open-file-dialog))]) ""]
                     [status-text "skip 1, span 2, wrap"]

                     [(nth layer-cbs 0) "span 2 2"]
                     [(s/radio :text "Vasakul ülal" :id :top-left
                               :group (:new-logo placement-map)) ""]
                     [(s/radio :text "Paremal ülal" :id :top-right
                               :group (:new-logo placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Vasakul all" :id :bottom-left
                               :group (:new-logo placement-map)) "skip 1"]
                     [(s/radio :text "Paremal all" :id :bottom-right
                               :group (:new-logo placement-map)) "wrap 20px"]

                     [(nth layer-cbs 1) "span 2 2"]
                     [(s/radio :text "Vasakul ülal" :id :top-left
                               :group (:new-price placement-map)) ""]
                     [(s/radio :text "Paremal ülal" :id :top-right
                               :group (:new-price placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Vasakul all" :id :bottom-left
                               :group (:new-price placement-map)) "skip 1"]
                     [(s/radio :text "Paremal all" :id :bottom-right
                               :group (:new-price placement-map)) "wrap 20px"]

                     [(nth layer-cbs 2) "span 2 2"]
                     [(s/radio :text "Vasakul ülal" :id :top-left
                               :group (:new placement-map)) ""]
                     [(s/radio :text "Paremal ülal" :id :top-right
                               :group (:new placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Vasakul all" :id :bottom-left
                               :group (:new placement-map)) "skip 1"]
                     [(s/radio :text "Paremal all" :id :bottom-right
                               :group (:new placement-map)) "wrap 20px"]

                     [(convert-button) "skip 3"]]))

(defn run []
  ;; (s/invoke-later
  ;;  (-> (get (SubstanceLookAndFeel/getAllSkins) "Office Silver 2007")
  ;;      .getClassName
  ;;      SubstanceLookAndFeel/setSkin))
  (s/invoke-later
   (-> (s/frame
        :title "Dandy"
        :content (layout)
        :menubar (build-menubar)
        :on-close :exit
        :resizable? false
        :icon (seesaw.icon/icon (io/file "assets/dandy.png"))
        :transfer-handler dnd-handler)
       s/pack!
       s/show!)))
