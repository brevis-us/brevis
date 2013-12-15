(defproject brevis "0.7.0"
  :description "A Functional Artificial Life Simulator"
  :url "https://github.com/kephale/brevis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx2g" "-Xdock:name=Brevis" "-splash:resources/brevis_splash.gif"]  
  :resource-paths ["resources"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["java"]
  :dependencies [;; Essential
                 [org.clojure/clojure "1.5.1"]                 
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [clj-random "0.1.5"]         
                 
                 ;; Project management
                 ;[leiningen-core "2.3.4"]
                 [leiningen "2.3.4"]
                 
                 ;; Physics packages                 
;                 [kephale/ode4j "0.12.0-j1.4"]
                 ;[kephale/ode4j "20130414_001"]
                 [org.ode4j/core "0.2.7"]
                 [org.ode4j/demo "0.2.7"]
                 
                 ;; Graphics packages
                 [kephale/penumbra "0.6.7"]
                 [kephale/slick-util "1.0.1"]
                 [org.l33tlabs.twl/pngdecoder "1.0"]
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]                 
                 
                 ;; Math packages
                 ;[java3d/vecmath "1.3.1"]
                 ;[com.googlecode.efficient-java-matrix-library/ejml "0.23"]
                 
                 ;; UI packages
                 [seesaw "1.4.4"]
                 [com.fifesoft/rsyntaxtextarea "2.5.0"]
                 ]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [clojure.main brevis.ui.core]
  :main ^:skip-aot brevis.Launcher
  :manifest {"SplashScreen-Image" "brevis_splash.gif"})
