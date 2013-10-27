(ns dandy.prefs
  (require [seesaw.pref :as prefs]))

(def prefs (prefs/preference-atom "prefs" {}))
