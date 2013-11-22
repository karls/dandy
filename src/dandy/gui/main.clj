(ns dandy.gui.main
  (:require [clojure.core.async :refer :all]
            [clojure.java.io :as io]
            [seesaw.core :as s]
            [dandy.util :as util]
            [dandy.gui.main-behaviour :as behaviour]
            [dandy.gui.settings])
  (:use [seesaw.chooser :only (choose-file)]
        [seesaw.mig :only (mig-panel)]
        [dandy.prefs :only (prefs)])
  (:import org.pushingpixels.substance.api.SubstanceLookAndFeel))

(defmulti build-menuitems-for identity)
(defmethod build-menuitems-for :edit [_]
  [(s/menu-item :text "Preferences"
                :listen [:action dandy.gui.settings/make-settings-dialog])])

(defmulti build-menu-for identity)
(defmethod build-menu-for :edit [_]
  (s/menu :text "Edit" :items (build-menuitems-for :edit)))

(defn build-menubar []
  (s/menubar :items [(build-menu-for :edit)]))

(defn convert-button []
  (s/button :text "Convert" :id :convert-button))

(defn layout []
  (let [panel (mig-panel :constraints ["gap 5px, ins 5px" "" ""]
                         :items [[(s/button :text "Choose files"
                                            :id :choose-files) ""]
                                 [(s/label :text ""
                                           :id :status-text) "skip 1, span 2, wrap"]])]
    (doseq [icon (sort behaviour/icon-names)]
      (s/add! panel
              [(s/checkbox :text icon
                           :id icon
                           :class :icon-checkbox
                           :selected? (get behaviour/to-show icon))
               "span 2 2"])
      (s/add! panel
              [(s/radio :text "Top left"
                        :id :top-left
                        :group (get behaviour/pos-groups icon)
                        :selected? (= :top-left (get behaviour/to-position icon)))
               ""])
      (s/add! panel
              [(s/radio :text "Top right"
                        :id :top-right
                        :group (get behaviour/pos-groups icon)
                        :selected? (= :top-right (get behaviour/to-position icon)))
               "wrap"])
      (s/add! panel
              [(s/radio :text "Bottom left"
                        :id :bottom-left
                        :group (get behaviour/pos-groups icon)
                        :selected? (= :bottom-left (get behaviour/to-position icon)))
               "skip 1"])
      (s/add! panel
              [(s/radio :text "Bottom right"
                        :id :bottom-right
                        :group (get behaviour/pos-groups icon)
                        :selected? (= :bottom-right (get behaviour/to-position icon)))
               "wrap 20px"]))

    (s/add! panel [(convert-button) "skip 3"])
    panel))

(defn run []
  (s/invoke-later
   (-> (get (SubstanceLookAndFeel/getAllSkins) "Office Silver 2007")
       .getClassName
       SubstanceLookAndFeel/setSkin))

  (s/invoke-later
   (-> (s/frame
        :title "Dandy"
        :content (layout)
        :menubar (build-menubar)
        :on-close :exit
        :resizable? false
        :icon (seesaw.icon/icon (io/file "assets/dandy.png"))
        :transfer-handler behaviour/dnd-handler)
       behaviour/add-behaviours
       s/pack!
       s/show!)))
