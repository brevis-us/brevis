(defproject brevis "0.8.2"
  :description "A Functional Scientific and Artificial Life Simulator"
  :url "https://github.com/kephale/brevis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx4g" "-Xdock:name=Brevis" "-splash:resources/brevis_splash.gif"]  
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
                 [clj-jgit "0.3.9"]
                 
                 ;; Physics packages                 
;                 [kephale/ode4j "0.12.0-j1.4"]
                 ;[kephale/ode4j "20130414_001"]
                 [org.ode4j/core "0.2.7"]
                 [org.ode4j/demo "0.2.7"]
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]                 
                 
                 ;; Graphics packages (3D rendering)
                 ;[kephale/penumbra "0.6.7"]
                 [kephale/lwjgl "2.9.0"]
                 [kephale/lwjgl-natives "2.9.0"]
                 ;[org.lwjgl.lwjgl/lwjgl-platform "2.9.1" :classifier "natives-osx" :native-prefix ""]
                 ;[org.lwjgl.lwjgl/lwjgl "2.9.1"]
                 [kephale/slick-util "1.0.1"]
                 [org.l33tlabs.twl/pngdecoder "1.0"]          
                 
                 ;; Plotting 
                 [org.jfree/jcommon "1.0.21"]
                 [org.jfree/jfreechart "1.0.17"]
                 
                 ;; Math packages
                 ;[java3d/vecmath "1.3.1"]
                 [com.googlecode.efficient-java-matrix-library/ejml "0.24"]
                 [org.flatland/ordered "1.5.2"]
                 [kephale/jblas "1.2.0"]; should use ejml instead
                 
                 ;; UI packages
                 [seesaw "1.4.4"]
                 [com.fifesoft/rsyntaxtextarea "2.5.0"]
                 ]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [clojure.main brevis.ui.core brevis.plot.Plotter]
  :main ^:skip-aot brevis.Launcher
  :manifest {"SplashScreen-Image" "brevis_splash.gif"}
  )
