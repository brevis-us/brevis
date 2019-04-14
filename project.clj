(defproject brevis "0.11.0-SNAPSHOT"
  :description "A Functional Scientific and Artificial Life Simulator"
  :url "http://brevis.us"
  :license {:name "Apache License v2"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  ;:jvm-opts ^:replace ["-Xmx4g" "-splash:resources/brevis_splash.gif" "-XX:+UseParallelGC"]; "-Xdock:name=Brevis"
  ;:jvm-opts ^:replace ["-Xmx4g" #_"-XX:+UseParallelGC"]; "-Xdock:name=Brevis"
  ;:jvm-opts ^:replace ["-XX:+UseG1GC" "-Xmx4g" "-XX:-UseGCOverheadLimit"]
  :resource-paths ["resources" "target/native"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :dependencies [;; Essential
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-random "0.1.7"]
                 [brevis.us/brevis-utils "0.1.0"]

                 ;; Project management & utils
                 [leiningen "2.3.4"]
                 [me.raynes/conch "0.8.0"]
                 ;[potemkin "0.4.1"]

                 ;; Images and Physics packages
		             ;[fun.imagej/fun.imagej "0.3.4-SNAPSHOT"]
                 [com.github.kephale/fun.imagej "2196a37aefab176dca9778270f8096a64a5ee849"]
                 [org.ode4j/core "0.2.8"]; 0.3.0 introduced threading issues that are causing problems
                 ;[org.ode4j/demo "0.2.8"]; 0.2.9 forces java7
                 [com.nitayjoffe.thirdparty.net.robowiki.knn/knn-benchmark "0.1"]; funimage can handle this, factor this out

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
                 ]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  ;:aot [#_clojure.main brevis.ui.core]; brevis.example.swarm]
  ;:manifest {"SplashScreen-Image" "brevis_splash.gif"}
  ;:warn-on-reflection true
  :javadoc-opts {:package-names ["brevis" "brevis.graphics" "brevis.plot"]}
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  ;:profiles {:headless {:jvm-opts ["-DbrevisHeadless=true"]}
  :repositories [["imagej" "https://maven.imagej.net/content/groups/hosted/"]
                 ["imagej-releases" "https://maven.imagej.net/content/repositories/releases/"]
                 ["ome maven" "https://artifacts.openmicroscopy.org/artifactory/maven/"]
                 ["imagej-snapshots" "https://maven.imagej.net/content/repositories/snapshots/"]
                 ["jitpack.io" "https://jitpack.io"]
                 ["saalfeld-lab-maven-repo" "https://saalfeldlab.github.io/maven"]
                 ["boundless" "http://repo.boundlessgeo.com/main/"]
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
  (require 'cemerick.pomegranate.aether)
  (cemerick.pomegranate.aether/register-wagon-factory!
   "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))
