(ns dandy.prefs
  (require [seesaw.core :as s]
           [seesaw.pref :as prefs]
           [seesaw.forms])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)]))

(def prefs (prefs/preference-atom "prefs" {}))

(def directory-label
  (s/label :text "Images directory"))

(def directory-text-field
  (s/text :text (get @prefs :dir)
          :enabled? false))

(defn dir-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :dir path }))
    (s/text! directory-text-field path)))

(def directory-button
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :dirs-only
                                            :dir (get @prefs :dir)
                                            :success-fn dir-chosen))]))
(def new-logo-label
  (s/label :text "'Logo' image"))

(def new-logo-text-field
  (s/text :text (get @prefs :new-logo)
          :enabled? false))

(defn new-logo-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :new-logo path }))
    (s/text! new-logo-text-field path)))

(def new-logo-button
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-logo-chosen))]))
(def new-price-label
  (s/label :text "'New price' image"))

(def new-price-text-field
  (s/text :text (get @prefs :new-price)
          :enabled? false))

(defn new-price-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :new-price path }))
    (s/text! new-price-text-field path)))

(def new-price-button
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-price-chosen))]))
(def new-label
  (s/label :text "'New' image"))

(def new-text-field
  (s/text :text (get @prefs :new)
          :enabled? false))

(defn new-chosen [fc file]
  (let [path (.getAbsolutePath file)]
    (reset! prefs (conj @prefs { :new path }))
    (s/text! new-text-field path)))

(def new-button
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-chosen))]))

(defn make-preference-content []
  (mig-panel :items [[directory-label "align right"]
                     [directory-text-field "width 300px"]
                     [directory-button "wrap 30px"]

                     [new-logo-label "align right"]
                     [new-logo-text-field "growx"]
                     [new-logo-button "wrap"]

                     [new-price-label "align right"]
                     [new-price-text-field "growx"]
                     [new-price-button "wrap"]

                     [new-label "align right"]
                     [new-text-field "growx"]
                     [new-button ""]]))
  

(defn make-prefs-dialog []
  (let [frame (s/frame :title "Preferences"
                       :on-close :dispose
                       :resizable? false)]
    (s/config! frame :content (make-preference-content))
    (-> frame s/pack! s/show!)))
