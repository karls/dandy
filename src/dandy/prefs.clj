(ns dandy.prefs
  (require [seesaw.pref :as pref]))

(def prefs (pref/preference-atom "prefs" {}))
