(ns dandy.resize
  (import java.awt.image.BufferedImage))

(defn image-layout [bufimage]
  (let [w (.getWidth bufimage)
        h (.getHeight bufimage)]
    (cond
     (and (<= w 1000) (<= h 1000)) :small-enough
     (and (>= w h) (> w 1000)) :scale-landscape
     (and (<  w h) (> h 1000)) :scale-portrait)))

(defmulti resize image-layout)

(defmethod resize :small-enough [original-image] original-image)

(defmethod resize :scale-landscape [original-image]
  (let [ratio (/ (.getWidth original-image) 1000)
        new-width 1000
        new-height (int (/ (.getHeight original-image) ratio))
        type (.getType original-image)
        resized-image (BufferedImage. new-width new-height type)
        graphics (.createGraphics resized-image)]
    (doto graphics
      (.drawImage original-image 0 0 new-width new-height nil)
      (.dispose))
    resized-image))

(defmethod resize :scale-portrait [original-image]
  (let [ratio (/ (.getHeight original-image) 1000)
        new-height 1000
        new-width (int (/ (.getWidth original-image) ratio))
        type (.getType original-image)
        resized-image (BufferedImage. new-width new-height type)
        graphics (.getGraphics resized-image)]
    (doto graphics
      (.drawImage original-image 0 0 new-width new-height nil)
      (.dispose))
    resized-image))
