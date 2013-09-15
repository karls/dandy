(ns dandy.resize-spec
  (require [speclj.core :refer :all]
           [dandy.resize :refer :all]
           [dandy.spec-helper :refer :all])
  (import java.awt.image.BufferedImage))

(describe "resize"
          (context "when landscape image is <= 1000px wide"
                   (let [small-image (BufferedImage. 999 666 5)
                         resized-image (resize small-image)]
                     (it "does not resize the image"
                         (should= 999 (.getWidth resized-image))
                         (should= 666 (.getHeight resized-image)))))

          (context "when landscape image is > 1000px wide"
                   (let [large-image (BufferedImage. 3000 2000 5)
                         resized-image (resize large-image)]
                     (it "resizes the image"
                         (should= 1000 (.getWidth resized-image)))))

          (context "when portrait image is <= 1000px tall"
                   (let [small-image (BufferedImage. 333 999 5)
                         resized-image (resize small-image)]
                     (it "does not resize the image"
                         (should= 333 (.getWidth resized-image))
                         (should= 999 (.getHeight resized-image)))))

          (context "when portrait image is > 1000px tall"
                   (let [large-image (BufferedImage. 2000 3000 5)
                         resized-image (resize large-image)]
                     (it "resizes the image"
                         (should= 1000 (.getHeight resized-image))))))

(describe "image-layout"
          (context "small landscape image"
                   (let [image (BufferedImage. 1000 500 5)]
                     (it "is small enough"
                         (should= :small-enough (image-layout image)))))
          (context "small portrait image"
                   (let [image (BufferedImage. 500 1000 5)]
                     (it "is small enough"
                         (should= :small-enough (image-layout image)))))
          (context "large landscape image"
                   (let [image (BufferedImage. 1001 800 5)]
                     (it "is going to scale it"
                         (should= :scale-landscape (image-layout image)))))
          (context "large portrait image"
                   (let [image (BufferedImage. 800 1001 5)]
                     (it "is going to scale it"
                         (should= :scale-portrait (image-layout image))))))

(describe "path->image"
          (context "normal image file"
                   (it "returns an instance of BufferedImage"
                       (should (instance? BufferedImage 
                                          (path->image (images "mountains.jpg"))))))
          (context "non-existent file"
                   (it "throws an exception"
                       (should-throw (path->image (images "non-existent.jpg")))))
          (context "invalid image file"
                   (it "throws an exception"
                       (should-throw (path->image (images "not-an-image.txt"))))))
