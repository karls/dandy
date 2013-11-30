(ns dandy.gui.settings-behaviour
  (:require [seesaw.core :as s]
            [clojure.java.io :as io]
            [seesaw.bind :as bind])
  (:use [dandy.prefs :only (prefs)]
        [seesaw.chooser :only (choose-file)])
  (:import java.awt.event.ItemEvent
           java.io.File
           javax.imageio.ImageIO
           java.awt.RenderingHints
           dandy.ResizeUtils))

; notification channel for when the default directory has been chosen
(def directory-notifier (bind/notify-later))

; notification channel for when an icon is added to or removed from icon list
(def icon-list-notifier (bind/notify-later))

; notification channel for when an icon is selected in icon list
(def icon-selection-notifier (bind/notify-later))

; notification channel for position radio button group
(def position-selection-notifier (bind/notify-later))

; this is used in settings UI, but declared here to avoid cyclic deps
(def position-group (s/button-group))

(def not-nil? (complement nil?))

(defn- keyword->id [kw] (keyword (str "#" (name kw))))

(defn parse-position
  "Parses icon's position (:id attribute on the RadioButton) from an ItemEvent,
which is fired when the selection of dandy.gui.settings/position-group changes."
  [e]
  (when (= (.getStateChange e) ItemEvent/SELECTED)
    (s/config (.getItem e) :id)))

(defn- icon-chosen [chooser file]
  (.getAbsolutePath file))

(defn- choose-icon []
  (choose-file :type :open
               :selection-mode :files-only
               :remember-directory? true
               :success-fn icon-chosen))

; the main function that adds behaviour to UI elements
(defn add-behaviours
  "Adds behaviours to all the settings UI elements"
  [root]
  ; finds elements under root
  (defn find-elem [id] (s/select root [id]))

  (bind/bind directory-notifier
             (bind/property (find-elem :#directory-field) :text))

  (bind/bind icon-list-notifier
             (bind/transform #(-> (get % :icons) keys sort))
             (bind/property (find-elem :#icon-list) :model))

  (bind/bind icon-selection-notifier
             (bind/b-do* #(s/selection! (find-elem :#icon-list) %)))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/property (find-elem :#icon-remove) :enabled?))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/transform #(get-in @prefs [:icons % :path]))
             (bind/property (find-elem :#icon-path) :text))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/filter nil?)
             (bind/b-do* (fn [_] (s/selection! position-group nil))))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/transform #(get-in @prefs [:icons % :path]))
             (bind/filter not-nil?)
             (bind/transform #(ImageIO/read (clojure.java.io/file %)))
             (bind/transform
              #(ResizeUtils/getScaledInstance %
                                              64
                                              RenderingHints/VALUE_INTERPOLATION_BILINEAR))
             (bind/property (find-elem :#icon-preview) :icon))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/filter nil?)
             (bind/property (find-elem :#icon-preview) :icon))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/transform #(get-in @prefs [:icons % :position]))
             (bind/b-do* (fn [pos]
                           (when pos
                             (s/selection! position-group
                                           (find-elem (keyword->id pos)))))))

  (bind/bind (bind/selection (find-elem :#icon-list))
             (bind/transform #(get-in @prefs [:icons % :show-by-default]))
             (bind/property (find-elem :#icon-show-by-default) :selected?))

  (s/listen position-group
            :selection
            (fn [e]
              (let [icon (s/selection (find-elem :#icon-list))
                    new-position (parse-position e)]
                (when (and icon new-position)
                  (swap! prefs
                         update-in
                         [:icons icon]
                         assoc :position new-position)))))

  ; new icon added
  (s/listen (find-elem :#icon-add) :mouse-clicked
            (fn [e]
              (swap! prefs assoc-in [:icons "<new icon>"] {:position :top-right})
              (bind/notify icon-list-notifier @prefs)
              (bind/notify icon-selection-notifier "<new icon>")))

  ; icon removed
  (s/listen (find-elem :#icon-remove) :mouse-clicked
            (fn [e]
              (let [key (s/selection (find-elem :#icon-list))]
                (swap! prefs
                       update-in
                       [:icons]
                       dissoc
                       key)
                (bind/notify icon-list-notifier @prefs))))

  ; icon path added/updated
  (s/listen (find-elem :#icon-browse) :mouse-clicked
            (fn [e]
              (if-let [path (choose-icon)]
                (let [key (s/selection (find-elem :#icon-list))
                      name (.getName (File. path))]
                  (swap! prefs
                         update-in
                         [:icons]
                         dissoc
                         key)
                  (swap! prefs
                         update-in
                         [:icons name]
                         assoc :path path :name name :position :top-right :show-by-default false)
                  (bind/notify icon-list-notifier @prefs)
                  (bind/notify icon-selection-notifier name)))))

  (s/listen (find-elem :#icon-show-by-default) :mouse-clicked
            (fn [e]
              (if-let [icon (s/selection (find-elem :#icon-list))]
                (swap! prefs
                       update-in
                       [:icons icon]
                       assoc :show-by-default (s/selection e)))))
  root)
