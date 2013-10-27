(ns dandy.gui.prefs
  (require [seesaw.core :as s]
           [seesaw.pref :as prefs]
           [seesaw.bind :as bind]
           [dandy.gui.behaviour :as behaviour])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)])
  (import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  )

(def prefs (prefs/preference-atom "prefs" {}))

(defn directory-label []
  (s/label :text "Choose default directory:"))

(defn directory-text-field []
  (s/text :id :directory-field
          :text (get @prefs :dir)
          :enabled? false))

(defn dir-chosen [chooser file]
  (let [path (.getAbsolutePath file)]
    (swap! prefs conj { :dir path })
    (bind/notify behaviour/directory-notifier path)))

(defn directory-button []
  (s/button :text "Browse"
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :dirs-only
                                            :dir (get @prefs :dir)
                                            :success-fn dir-chosen))]))

(defn icons-label []
  (s/label :text "Choose icons:"))

(defn icon-list []
  (s/listbox :id :icons
             :model (keys (get @prefs :icons))
             :border (seesaw.border/line-border :color :grey)))

(defn icon-preview []
  (s/label :id :icon-preview
           :border (seesaw.border/line-border :color :grey)
           :preferred-size [64 :by 64]))

(defn icon-path []
  (s/text :id :icon-path))

(defn icon-browse []
  (s/button :text "Browse"))

(defn icon-show-by-default []
  (s/checkbox :id :icon-show-by-default
              :text "Show by default"))

(defn icon-add []
  (s/button :id :icon-add
            :icon (seesaw.icon/icon "icons/plus.png")))

(defn icon-remove []
  (s/button :id :icon-remove
            :icon (seesaw.icon/icon "icons/bin.png")))

(defn make-preference-content []
  (let [pgroup (s/button-group)]
    (mig-panel :constraints ["gap 5px, fill"
                             "[90px]10px[:64px:64px][240px:][]"
                             ""]
               :items [[(s/label :text "Choose default directory:")
                        "align left, wrap, span 4"]
                       [(directory-text-field) "span 3, growx"]
                       [(directory-button) "wrap"]

                       [(s/separator) "span 4, growx, wrap"]

                       [(s/label :text "Choose icons:")
                        "align left, span 5, wrap"]

                       [(icon-list) "grow, spany 5"]
                       [(icon-preview) "spany 5, aligny top"]
                       [(icon-path) "aligny top, growx"]
                       [(icon-browse) "aligny top, wrap"]

                       [(s/label :text "Position:") "skip 1, wrap"]
                       [(s/radio :text "Top left"
                                 :id :top-left
                                 :group pgroup) "skip 1, split 2, width 105px"]
                       [(s/radio :text "Top right"
                                 :id :top-right
                                 :selected? true
                                 :group pgroup) "wrap"]
                       [(s/radio :text "Bottom left"
                                 :id :bottom-left
                                 :group pgroup) "skip 1, split 2, width 105px"]
                       [(s/radio :text "Bottom right"
                                 :id :bottom-right
                                 :group pgroup) "wrap"]
                       [(icon-show-by-default) "skip 1, wrap"]
                       [(icon-add) "split 2"]
                       [(icon-remove) ""]])))

(defn make-prefs-dialog []
  (s/invoke-later
   (-> (get (SubstanceLookAndFeel/getAllSkins) "Office Silver 2007")
       .getClassName
       SubstanceLookAndFeel/setSkin))
  (s/invoke-later
   (-> (s/frame
        :title "Preferences"
        :on-close :dispose
        :resizable? false
        :content (make-preference-content))
       behaviour/add-behaviours
       s/pack!
       s/show!)))
