(ns dandy.prefs
  (require [seesaw.core :as s]
           [seesaw.pref :as prefs])
  (use [seesaw.chooser :only (choose-file)]))

(def prefs (prefs/preference-atom "prefs" {}))
;; (println @prefs)
;; (reset! prefs {:hello "world"})
;; (println @prefs)

(def directory-text-field
  (s/text :text (get @prefs :dir)
          :columns 30
          :enabled? false))

(defn dir-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :dir path }))
    (s/text! directory-text-field path)))

(defn make-layers-tab []
  (s/border-panel))

(defn make-default-directory-tab []
  (s/border-panel :border 5
                  :hgap 5
                  :vgap 5
                  :center directory-text-field
                  :east (s/button :text "Choose"
                                  :listen [:action (fn [e]
                                                     (choose-file :type :open
                                                                  :selection-mode :dirs-only
                                                                  :dir (get @prefs :dir)
                                                                  :success-fn dir-chosen))])))

(defn make-preference-tabs []
  (s/tabbed-panel
   :tabs [{ :title "Default directory" :content (make-default-directory-tab) }
          { :title "Layers" :content (make-layers-tab) }]))

(defn make-prefs-dialog []
  (let [frame (s/frame :title "Preferences"
                       :on-close :dispose
                       :resizable? false)
        layout (s/border-panel :border 5
                               :hgap 5
                               :vgap 5
                               :center (make-preference-tabs))]
    (s/config! frame :content layout)
    (-> frame s/pack! s/show!)))
