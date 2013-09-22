(ns dandy.gui
  (require [seesaw.core :as s]
           [dandy.prefs :as prefs])
  (use [seesaw.chooser :only (choose-file)]))

(def status-text (s/label :text "Add files to convert"
                          :foreground "#444"
                          :font :monospaced
                          :v-text-position :bottom
                          :border (seesaw.border/line-border :top 1 :color "#BBB")))

(def convert-button (s/button :text "Convert files" :enabled? false))

(defn files-selected [fc, files]
  (s/text! status-text (str (count files) " files ready to be converted"))
  (s/config! convert-button :enabled? true))

(defmulti build-menuitems-for (fn [x] x))
(defmethod build-menuitems-for :file [_]
  [(s/menu-item :text "Add files"
                :listen [:action (fn [e] 
                                   (choose-file :type :open
                                                :multi? true
                                                :filters [["Images" ["png" "jpg" "jpeg"]]]
                                                :selection-mode :files-only
                                                :dir (get @prefs/prefs :dir)
                                                :success-fn files-selected))])])
(defmethod build-menuitems-for :edit [_]
  [(s/menu-item :text "Preferences"
                :listen [:action (fn [_] (prefs/make-prefs-dialog))])])

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
  (def f (s/frame :title "Dandy" :menubar (build-menubar) :on-close :exit))
  (def layout (s/border-panel :border 5
                              :hgap 5
                              :vgap 5))
  (def buttons
    (let [group (s/button-group)]
      (s/grid-panel :rows 2 :columns 2
                    :items [(s/radio :id :top-left :text "Top left" :group group)
                            (s/radio :id :top-right :text "Top right" :group group)
                            (s/radio :id :bottom-left :text "Bottom left" :group group)
                            (s/radio :id :bottom-right :text "Bottom right" :group group)])))
  (s/add! layout [buttons :center])
  (s/add! layout [convert-button :east])
  (s/add! layout [status-text :south])
  (s/config! f :content layout)
  (-> f s/pack! s/show!))
