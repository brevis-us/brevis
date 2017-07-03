(defproject brevis "0.10.4"
  :description "A Functional Scientific and Artificial Life Simulator"
  :url "http://brevis.us"
  :license {:name "Apache License v2"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  ;:jvm-opts ^:replace ["-Xmx4g" "-splash:resources/brevis_splash.gif" "-XX:+UseParallelGC"]; "-Xdock:name=Brevis"  
  ;:jvm-opts ^:replace ["-Xmx4g" #_"-XX:+UseParallelGC"]; "-Xdock:name=Brevis"
  ;:jvm-opts ^:replace ["-XX:+UseG1GC" "-Xmx4g" "-XX:-UseGCOverheadLimit"]  
  :jvm-opts ^:replace ["-Dapple.laf.useScreenMenuBar=true"]  
  :resource-paths ["resources" "target/native"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["java"]
  :dependencies [;; Essential
                 [org.clojure/clojure "1.8.0"]                 
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-random "0.1.7"]         
                 
                 ;; Project management & utils
                 [leiningen "2.3.4"]
                 [clj-jgit "0.7.6"]; if UI gets factored out, this can go too
                 [me.raynes/conch "0.8.0"]
                 ;[potemkin "0.4.1"]
                 
                 ;; Images and Physics packages      
                 [funimage/funimage "0.1.97"]
                 [org.ode4j/core "0.2.8"]; 0.3.0 introduced threading issues that are causing problems
                 [org.ode4j/demo "0.2.8"]; 0.2.9 forces java7
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]; funimage can handle this, factor this out          
                 
                 ;; Graphics packages (3D rendering)
                 [kephale/lwjgl "2.9.0"]
                 [kephale/lwjgl-natives "2.9.0"]
                 [kephale/slick-util "1.0.1"]; This can probably be factored out now
                 ;[org.l33tlabs.twl/pngdecoder "1.0"]          
                 
                 ;[net.clearvolume/scenery "1.0-SNAPSHOT"]
                 ;[net.clearvolume/scenery "1.0-SNAPSHOT" :classifier "tests"]
                 ;[net.clearvolume/cleargl "1.0-SNAPSHOT"]
                 ;[scenery/openvr "0.1"]
                 
                 ;; Plotting 
                 [org.jfree/jcommon "1.0.21"]
                 [org.jfree/jfreechart "1.0.17"]
                 
                 ;; Math packages
                 ;[org.ojalgo/ojalgo "37.1"]; 38 requries java8
                 ;[org.ojalgo/ojalgo "38.2"]
                 [net.mikera/core.matrix "0.48.0"]
                 [net.mikera/vectorz-clj "0.39.0"]
                 [org.flatland/ordered "1.5.2"]; are we still using this?
                 
                 ;; UI packages (should we factor out the UI competely to make things lighter?)
                 [seesaw "1.4.4"]
                 [com.fifesoft/rsyntaxtextarea "2.5.3"]
                 ]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  ;:aot [#_clojure.main brevis.ui.core]; brevis.example.swarm]        
  :main ^:skip-aot brevis.Launcher
  ;:manifest {"SplashScreen-Image" "brevis_splash.gif"}
  ;:warn-on-reflection true
  :javadoc-opts {:package-names ["brevis" "brevis.graphics" "brevis.plot"]}
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :profiles {:headless {:jvm-opts ["-DbrevisHeadless=true"]}
             :BrIDE {:main brevis.ui.core
                     :aot [brevis.ui.core]
                     :uberjar-name "BrIDE.jar"}
             :brevis.example.swarm {:main brevis.example.swarm
                                    :aot :all
                                    :uberjar-name "brevis.example.swarm-STANDALONE.jar"}}
  :repositories [["imagej" "http://maven.imagej.net/content/groups/hosted/"]
                 ["imagej-releases" "http://maven.imagej.net/content/repositories/releases/"]
                 ["ome maven" "http://artifacts.openmicroscopy.org/artifactory/maven/"]
                 ["imagej-snapshots" "http://maven.imagej.net/content/repositories/snapshots/"]
                 ["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CI_DEPLOY_USERNAME
                                      :password :env/CI_DEPLOY_PASSWORD
                                      :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]
                 ]
  )
