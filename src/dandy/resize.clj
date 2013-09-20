(ns dandy.resize
  (import java.awt.image.BufferedImage))

(defn scale? [bufimage]
  (let [w (.getWidth bufimage)
        h (.getHeight bufimage)]
    (cond
     (<= w 1000) false
     (> w 1000)  true)))

(defmulti resize scale?)

(defmethod resize false [original-image] original-image)

(defmethod resize true
  [original-image] 
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
