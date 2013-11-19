(ns dandy.layer
  (:import java.awt.RenderingHints))

(defmulti compute-layer-coords (fn [_ _ pos] pos))

(defmethod compute-layer-coords :top-left [base layer position]
  [20, 20])

(defmethod compute-layer-coords :top-right [base layer position]
  [(- (.getWidth base) (.getWidth layer) 20), 20])

(defmethod compute-layer-coords :bottom-left [base layer position]
  [20, (- (.getHeight base) (.getHeight layer) 20)])

(defmethod compute-layer-coords :bottom-right [base layer position]
  [(- (.getWidth base) (.getWidth layer) 20),
   (- (.getHeight base) (.getHeight layer) 20)])

(defn apply-layer
  [base layer position]
  (let [coords (compute-layer-coords base layer position)
        graphics (.createGraphics base)]
    (doto graphics
      (.setRenderingHint RenderingHints/KEY_RENDERING
                         RenderingHints/VALUE_RENDER_QUALITY)
      (.setRenderingHint RenderingHints/KEY_COLOR_RENDERING
                         RenderingHints/VALUE_COLOR_RENDER_QUALITY)
      (.drawImage layer (first coords) (second coords) nil)
      (.dispose))
    base))
