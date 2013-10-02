(ns dandy.util
  (require [clojure.java.io :as io])
  (require [clojure.string :as str])
  (import javax.imageio.ImageIO))

(def image-exts [".png" ".jpg" ".jpeg"])

(defn ends-with-ext? [path ext]
  (.endsWith (str/lower-case path) ext))

(defn sanitise-path [path]
  (when
      (some true? (map (ends-with-ext? path) image-exts)) path))

(defn path->image [path]
  (-> path sanitise-path io/as-file ImageIO/read))
