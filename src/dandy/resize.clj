(ns dandy.resize
  (import java.awt.image.BufferedImage)
  (import java.awt.RenderingHints)
  (import dandy.ResizeUtils))

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
  (ResizeUtils/getScaledInstance original-image
                                 1000
                                 RenderingHints/VALUE_INTERPOLATION_BILINEAR))
