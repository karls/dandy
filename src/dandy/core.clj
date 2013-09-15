(ns dandy.core
  (:gen-class))
(require '[dandy.resize :as imageutil])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))

  (println args))
