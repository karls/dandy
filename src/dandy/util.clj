(ns dandy.util
  (require [clojure.java.io :as io])
  (require [clojure.string :as str])
  (import javax.imageio.ImageIO))

; TODO: add more extensions!
(defn sanitise-path [path]
  (when (.endsWith (str/lower-case path) ".jpg") path))

(defn path->image [path]
  (-> path sanitise-path io/as-file ImageIO/read))

