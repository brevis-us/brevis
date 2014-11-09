(defproject brevis "0.9.55"
  :description "A Functional Scientific and Artificial Life Simulator"
  :url "http://brevis.us"
  :license {:name "Apache License v2"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  ;:jvm-opts ^:replace ["-Xmx4g" "-splash:resources/brevis_splash.gif" "-XX:+UseParallelGC"]; "-Xdock:name=Brevis"  
  ;:jvm-opts ^:replace ["-Xmx4g" #_"-XX:+UseParallelGC"]; "-Xdock:name=Brevis"
  ;:jvm-opts ^:replace ["-XX:+UseG1GC" "-Xmx4g" "-XX:-UseGCOverheadLimit"]  
  :resource-paths ["resources" "target/native"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["java"]
  :dependencies [;; Essential
                 [org.clojure/clojure "1.6.0"]                 
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-random "0.1.7"]         
                 
                 ;; Project management & utils
                 [leiningen "2.3.4"]
                 [clj-jgit "0.7.6"]
                 [me.raynes/conch "0.8.0"]
                 
                 ;; Physics packages                 
                 [org.ode4j/core "0.2.9"]; 0.3.0 introduced threading issues that are causing issues
                 [org.ode4j/demo "0.2.9"]
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]                 
                 
                 ;; Graphics packages (3D rendering)
                 [kephale/lwjgl "2.9.0"]
                 [kephale/lwjgl-natives "2.9.0"]
                 [kephale/slick-util "1.0.1"]
                 [org.l33tlabs.twl/pngdecoder "1.0"]          
                 
                 ;; Plotting 
                 [org.jfree/jcommon "1.0.21"]
                 [org.jfree/jfreechart "1.0.17"]
                 
                 ;; Math packages
                 [com.googlecode.efficient-java-matrix-library/ejml "0.24"]; 0.26
                 [org.flatland/ordered "1.5.2"]
                 
                 ;; UI packages
                 [seesaw "1.4.4"]
                 [com.fifesoft/rsyntaxtextarea "2.5.3"]
                 ]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [#_clojure.main brevis.ui.core]; brevis.example.swarm]        
  :main ^:skip-aot brevis.Launcher
  ;:manifest {"SplashScreen-Image" "brevis_splash.gif"}
  :javadoc-opts {:package-names ["brevis" "brevis.graphics" "brevis.plot"]}
  :profiles {:headless {:jvm-opts ["-DbrevisHeadless=true"]}
             :BrIDE {:main brevis.ui.core
                     :aot [brevis.ui.core]
                     :uberjar-name "BrIDE.jar"}
             :brevis.example.swarm {:main brevis.example.swarm
                                    :aot :all
                                    :uberjar-name "brevis.example.swarm-STANDALONE.jar"}}
  )
