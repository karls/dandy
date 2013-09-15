(ns dandy.util-spec
  (require [speclj.core :refer :all]
           [dandy.util  :refer :all]
           [dandy.spec-helper :refer :all])
  (import java.awt.image.BufferedImage))

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
