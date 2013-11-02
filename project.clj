(defproject brevis "0.5.1-SNAPSHOT"
  :description "A Second-Generation Artificial Life Simulator"
  :url "https://github.com/kephale/brevis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx2g"]  
  :resource-paths ["resources"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["java"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [java3d/vecmath "1.3.1"]
;                 [kephale/ode4j "0.12.0-j1.4"]
                 [kephale/ode4j "20130414_001"]
                 [kephale/penumbra "0.6.7"]
                 [kephale/slick-util "1.0.1"]
                 [org.l33tlabs.twl/pngdecoder "1.0"]
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]
                 [clj-random "0.1.5"]
                 ])
