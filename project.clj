(defproject dandy "0.1.0-SNAPSHOT"
  :description "Watermarking tool"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.3"]]
  :profiles {:dev {:dependencies [[speclj "2.7.5"]]}}
  :plugins [[speclj "2.7.5"]]
  :test-paths ["spec/"]
  :main dandy.core)
