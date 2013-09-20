(ns dandy.gui
  (require [seesaw.core :as s]
           [seesaw.pref :as pref]
           [seesaw.chooser :as chooser]))

(def prefs (pref/preference-atom "prefs" {}))
;; (println @prefs)
;; (reset! prefs {:hello "world"})
;; (println @prefs)

(defmulti build-menuitems-for (fn [x] x))
(defmethod build-menuitems-for :file [_]
  [(s/menu-item :text "Add files"
                :listen [:action (fn [_] 
                                   (chooser/choose-file :type :open
                                                        :multi? true
                                                        :filters [["Images" ["png" "jpg" "jpeg"]]]
                                                        :selection-mode :files-only))])])
(defmethod build-menuitems-for :edit [_]
  [(s/menu-item :text "Preferences")])

(defmulti build-menu-for (fn [x] x))
(defmethod build-menu-for :file [_]
  (s/menu :text "File" :items (build-menuitems-for :file)))
(defmethod build-menu-for :edit [_]
  (s/menu :text "Edit" :items (build-menuitems-for :edit)))

(defn build-menubar []
  (s/menubar :items [(build-menu-for :file)
                     (build-menu-for :edit)]))

(defn run []
  (s/native!)
  (def f (s/frame :title "Dandy" :menubar (build-menubar)))
  (def layout (s/grid-panel :rows 2 :columns 1))
  (def buttons
    (let [group (s/button-group)]
      (s/grid-panel :rows 2 :columns 2
                    :items [(s/radio :id :top-left :text "Top left" :group group)
                            (s/radio :id :top-right :text "Top right" :group group)
                            (s/radio :id :bottom-left :text "Bottom left" :group group)
                            (s/radio :id :bottom-right :text "Bottom right" :group group)])))
  (s/add! layout buttons)
  (s/add! layout (s/button :text "Convert files" :enabled? false))
  (s/config! f :content layout)
  (-> f s/pack! s/show!))
