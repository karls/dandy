(ns dandy.gui.behaviour
  (require [seesaw.core :as s]
           [seesaw.bind :as bind]))

(def directory-notifier (bind/notify-later))

(defn add-behaviours [root]
  (bind/bind directory-notifier
             (bind/property (s/select root [:#directory-field]) :text))

  root)
