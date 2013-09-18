(ns dandy.layer-spec
  (require [speclj.core :refer :all]
           [dandy.layer :refer :all]
           [dandy.spec-helper :refer :all])
  (import java.awt.image.BufferedImage))

(describe "compute-layer-coords"
          (context "top-left"
                   (let [base (BufferedImage. 1000 700 5)
                         layer (BufferedImage. 130 70 5)]
                     (it "gives coordinates of top left corner of base image"
                         (should= [20 20] (compute-layer-coords base layer :top-left)))))
          (context "top-right"
                   (let [base (BufferedImage. 1000 700 5)
                         layer (BufferedImage. 130 70 5)]
                     (it "gives coordinates of top left corner of base image"
                         (should= [850 20] (compute-layer-coords base layer :top-right)))))
          (context "bottom-left"
                   (let [base (BufferedImage. 1000 700 5)
                         layer (BufferedImage. 130 70 5)]
                     (it "gives coordinates of top left corner of base image"
                         (should= [20 610] (compute-layer-coords base layer :bottom-left)))))
          (context "bottom-right"
                   (let [base (BufferedImage. 1000 700 5)
                         layer (BufferedImage. 130 70 5)]
                     (it "gives coordinates of top left corner of base image"
                         (should= [850 610] (compute-layer-coords base layer :bottom-right))))))
