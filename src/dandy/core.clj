(ns dandy.core
  (:gen-class))
(require '[dandy.gui :as gui])

(defn -main
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))

  (gui/run))
