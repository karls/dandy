(ns dandy.gui
  (require [seesaw.core :as s]
           [dandy.prefs :as prefs]
           [dandy.util :as util]
           [dandy.resize :refer (resize)]
           [dandy.layer :refer (apply-layer)]
           [clojure.core.async :refer :all]
           [clojure.java.io :as io])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)])
  (import javax.imageio.ImageIO))

(def files (atom []))
(def done-count (atom 0))

(def status-text (s/label :text ""))

(def layer-cbs [(s/checkbox :text "Logo" :user-data :new-logo)
                (s/checkbox :text "New price" :user-data :new-price)
                (s/checkbox :text "New" :user-data :new)])

(def placement-map {:new-logo (s/button-group)
                    :new-price (s/button-group)
                    :new (s/button-group)})

(defn get-selected-layers []
  (filter (fn [cb] (s/config cb :selected?)) layer-cbs))

(defn get-user-data-for-layers []
  (map (fn [cb] (s/config cb :user-data)) (get-selected-layers)))

(defn get-position-groups [ids]
  (map placement-map ids))

(defn load-layer [id]
  (ImageIO/read (io/file (get @prefs/prefs id))))

(defn generate-filename [directory basename ext]
  (let [pos-ext (apply str (map (fn [p] (str "_" (name p))) (get-user-data-for-layers)))]
    (str directory "/" basename "_resized" pos-ext "." ext)))

(defn convert-files [e]
  (reset! done-count 0)
  (println (filter (fn [cb] (s/config cb :selected?)) layer-cbs))
  (println (get-selected-layers))
  (when (not (empty @files))
    (let [done-chan (chan)
          layers (map (fn [id] (load-layer id)) (get-user-data-for-layers))
          positions (map (fn [p] (-> p s/selection (s/config :id)))
                         (get-position-groups (get-user-data-for-layers)))]
      (doseq [file @files]
        (go
         (let [image (ImageIO/read file)
               filename (.getName file)
               directory (.getParent file)
               name-parts (-> filename (clojure.string/split #"\."))
               ext (last name-parts)
               basename (clojure.string/join "." (drop-last name-parts))
               resized (-> image resize)
               layered (reduce (fn [i lp] (apply-layer i (first lp) (last lp))) resized (map vector layers positions))
               output-file (io/file (generate-filename directory basename ext))]
           (ImageIO/write layered ext output-file)
           (>! done-chan (str "Converting file " (.getPath output-file))))))

      (<!!
       (go
        (doseq [_ @files]
          (println (<! done-chan))
          (swap! done-count inc)
          (println @done-count))))
      (s/config! status-text :text (str @done-count " files converted!")))))

(def convert-button (s/button :text "Convert"
                              :listen [:action convert-files]))

(defn files-selected [fc, fs]
  (s/text! status-text (str (count fs) " files ready to be converted"))
  (reset! files fs))

(defn open-file-dialog []
  (choose-file :type :open
               :multi? true
               :filters [["Images" ["png" "jpg" "jpeg"]]]
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

(def layout
  (mig-panel :constraints ["gap 5px, ins 5px" "" ""]
             :items [[(s/button :text "Choose files"
                                :listen [:action (fn [e] (open-file-dialog))]) ""]
                     [status-text "skip 1, span 2, wrap"]
                     
                     [(nth layer-cbs 0) "span 2 2"]
                     [(s/radio :text "Top left" :id :top-left
                               :group (:new-logo placement-map)) ""]
                     [(s/radio :text "Top right" :id :top-right
                               :group (:new-logo placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Bottom left" :id :bottom-left
                               :group (:new-logo placement-map)) "skip 1"]
                     [(s/radio :text "Bottom right" :id :bottom-right
                               :group (:new-logo placement-map)) "wrap 20px"]

                     [(nth layer-cbs 1) "span 2 2"]
                     [(s/radio :text "Top left" :id :top-left
                               :group (:new-price placement-map)) ""]
                     [(s/radio :text "Top right" :id :top-right
                               :group (:new-price placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Bottom left" :id :bottom-left
                               :group (:new-price placement-map)) "skip 1"]
                     [(s/radio :text "Bottom right" :id :bottom-right
                               :group (:new-price placement-map)) "wrap 20px"]

                     [(nth layer-cbs 2) "span 2 2"]
                     [(s/radio :text "Top left" :id :top-left
                               :group (:new placement-map)) ""]
                     [(s/radio :text "Top right" :id :top-right
                               :group (:new placement-map) :selected? true) "wrap"]
                     [(s/radio :text "Bottom left" :id :bottom-left
                               :group (:new placement-map)) "skip 1"]
                     [(s/radio :text "Bottom right" :id :bottom-right
                               :group (:new placement-map)) "wrap 20px"]
                     
                     [convert-button "skip 3"]]))

(defn run []
  (s/native!)

  (def f (s/frame :title "Dandy"
                  :menubar (build-menubar)
                  :on-close :exit
                  :resizable? false
                  :icon (seesaw.icon/icon (io/file "dandy.png"))))

  (s/config! f :content layout)
  (-> f s/pack! s/show!))
