(ns dandy.prefs
  (require [seesaw.core :as s]
           [seesaw.pref :as prefs]
           [seesaw.forms])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)]))

(def prefs (prefs/preference-atom "prefs" {}))

(def directory-text-field
  (s/text :text (get @prefs :dir)
          :columns 20
          :enabled? false))

(defn dir-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :dir path }))
    (s/text! directory-text-field path)))

(def directory-button
  (s/button :text "Choose"
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :dirs-only
                                            :dir (get @prefs :dir)
                                            :success-fn dir-chosen))]))

(def layers-panel (s/horizontal-panel :border 5))

(defn layer-chooser []
  (choose-file :type :open :selection-mode :files-only))

(def add-layer-pane
  (mig-panel :items [[(s/text :text "Name" :columns 10) ""]
                     [(s/button :text "Choose layer" :listen [:action (fn [e] (layer-chooser))]) ""]
                     [(s/button :text "Add") ""]]))

(defn make-layers-tab []
  (s/vertical-panel :items ["foo" "bar" "baz" "qux" "quuz"]))
  ;; (seesaw.forms/forms-panel :items ["Login" (s/text) (seesaw.forms/next-line)
  ;;                                   "Password" (seesaw.forms/span (s/text) 3)]))
  ;; (mig-panel :items [[layers-panel "growy, wrap"]
  ;;                    [add-layer-pane ""]]))

(defn make-default-directory-tab []
  (mig-panel :items [[directory-text-field ""]
                     [directory-button ""]]))

(defn make-preference-tabs []
  (mig-panel :items [[(s/tabbed-panel
                       :tabs [{ :title "Default directory" :content (make-default-directory-tab) }
                              { :title "Layers" :content (make-layers-tab) }])
                      "grow, shrink"]]))

(defn make-prefs-dialog []
  (let [frame (s/frame :title "Preferences"
                       :on-close :dispose
                       :resizable? false)]
    (s/config! frame :content (make-preference-tabs))
    (-> frame s/pack! s/show!)))
