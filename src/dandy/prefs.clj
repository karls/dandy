(ns dandy.prefs
  (require [seesaw.core :as s]
           [seesaw.pref :as prefs]
           [seesaw.bind :as bind])
  (use [seesaw.chooser :only (choose-file)]
       [seesaw.mig :only (mig-panel)])
  ;(import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  )

(s/native!)

(def prefs (prefs/preference-atom "prefs" {}))

(def directory-notifier (bind/notify-later))
(def new-logo-notifier (bind/notify-later))
(def new-price-notifier (bind/notify-later))
(def new-notifier (bind/notify-later))

(defn file-chooser-callback [chooser file]
  nil)

(defn directory-label []
  (s/label :text "Vaikimisi pildikaust"))

(defn directory-text-field []
  (s/text :id :directory-field
          :text (get @prefs :dir)
          :enabled? false))

(defn dir-chosen [chooser file]
  (let [path (.getAbsolutePath file)]
    (swap! prefs conj { :dir path })
    (bind/notify directory-notifier path)))

(defn directory-button []
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :dirs-only
                                            :dir (get @prefs :dir)
                                            :success-fn dir-chosen))]))

(defn new-logo-label []
  (s/label :text "'Logo' pilt"))

(defn new-logo-text-field []
  (s/text :id :new-logo-field
          :text (get @prefs :new-logo)
          :enabled? false))

(defn new-logo-chosen [chooser file]
  (let [path (.getAbsolutePath file)]
    (swap! prefs conj { :new-logo path })
    (bind/notify new-logo-notifier path)))

(defn new-logo-button []
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-logo-chosen))]))
(defn new-price-label []
  (s/label :text "'Uus hind' pilt"))

(defn new-price-text-field []
  (s/text :id :new-price-field
          :text (get @prefs :new-price)
          :enabled? false))

(defn new-price-chosen [chooser file]
  (let [path (.getAbsolutePath file)]
    (swap! prefs conj { :new-price path })
    (bind/notify new-price-notifier path)))

(defn new-price-button []
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-price-chosen))]))
(defn new-label []
  (s/label :text "'Uus' pilt"))

(defn new-text-field []
  (s/text :id :new-field
          :text (get @prefs :new)
          :enabled? false))

(defn new-chosen [chooser file]
  (let [path (.getAbsolutePath file)]
    (swap! prefs conj { :new path })
    (bind/notify new-notifier path)))

(defn new-button []
  (s/button :text "..."
            :listen [:action (fn [e]
                               (choose-file :type :open
                                            :selection-mode :files-only
                                            :success-fn new-chosen))]))

(defn make-preference-content []
  (mig-panel :items [[(directory-label) "align right"]
                     [(directory-text-field) "width 300px"]
                     [(directory-button) "wrap 30px"]

                     [(new-logo-label) "align right"]
                     [(new-logo-text-field) "growx"]
                     [(new-logo-button) "wrap"]

                     [(new-label) "align right"]
                     [(new-text-field) "growx"]
                     [(new-button) "wrap"]

                     [(new-price-label) "align right"]
                     [(new-price-text-field) "growx"]
                     [(new-price-button) "wrap"]]))

(defn add-behaviours [root]
  (bind/bind directory-notifier
             (bind/property (s/select root [:#directory-field]) :text))
  (bind/bind new-logo-notifier
             (bind/property (s/select root [:#new-logo-field]) :text))
  (bind/bind new-price-notifier
             (bind/property (s/select root [:#new-price-field]) :text))
  (bind/bind new-notifier
             (bind/property (s/select root [:#new-field]) :text))

  root)

(defn make-prefs-dialog []
  ;; (s/invoke-later
  ;;  (-> (get (SubstanceLookAndFeel/getAllSkins) "Office Silver 2007")
  ;;      .getClassName
  ;;      SubstanceLookAndFeel/setSkin))
  (s/invoke-later
   (-> (s/frame
        :title "Preferences"
        :on-close :dispose
        :resizable? false
        :content (make-preference-content))
       add-behaviours
       s/pack!
       s/show!)))
