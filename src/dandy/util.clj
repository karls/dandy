(ns dandy.util
  (require [clojure.java.io :as io])
  (require [clojure.string :as str])
  (import javax.imageio.ImageIO))

(defn sanitise-path [path]
  (when
      (some
       (fn [ext]
         (.endsWith (str/lower-case path) ext))
       [".png" ".jpg" ".jpeg"])))

(defn path->image [path]
  (-> path sanitise-path io/as-file ImageIO/read))

