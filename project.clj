(defproject dandy "1.0.0"
  :description "Watermarking tool"
  :url "https://github.com/karls/dandy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.3"]
                 [org.clojure/core.async "0.1.222.0-83d0c2-alpha"]
                 [com.github.insubstantial/substance "7.1"]]
  :profiles {:dev {:dependencies [[speclj "2.7.5"]]}}
  :plugins [[speclj "2.7.5"]]
  :test-paths ["spec/"]
  :java-source-paths ["src/java"]
  :resource-paths ["assets"]
  :jvm-opts ["-Xdock:name=Dandy" "-Xdock:icon=assets/dandy-icon.png"]
  :main dandy.core
  :aot :all)
