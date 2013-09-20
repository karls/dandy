(ns dandy.core
  (:gen-class))
(require '[dandy.gui :as gui])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))

  (gui/run))
