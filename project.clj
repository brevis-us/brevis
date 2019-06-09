(defproject us.brevis/brevis "0.11.0-SNAPSHOT"
  :description "A Functional Scientific and Artificial Life Simulator"
  :url "http://brevis.us"
  :license {:name "Apache License v2"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :resource-paths ["resources"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :dependencies [;; Essential
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [com.kephale/clj-random "21e3dd4168"]
                 [brevis.us/brevis-utils "0.1.2"]

                 ;; Project management & utils
                 [me.raynes/conch "0.8.0"]

                 ;; Images and Physics packages
                 [org.joml/joml "1.9.12"]
                 [fun.imagej/fun.imagej "dff7759"]
                 [com.github.tzaeschke/ode4j "a08c3c8b55" :exclusions [com.github.tzaeschke.ode4j/demo]]

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
                 [seesaw "1.4.4"]]
  :jvm-opts ["-Dscenery.Renderer=OpenGLRenderer"]
  ;:javadoc-opts {:package-names ["brevis" "brevis.graphics" "brevis.plot"]}
  :repositories [["scijava.public" "https://maven.scijava.org/content/groups/public"]
                 ["jitpack.io" "https://jitpack.io"]
                 ["saalfeld-lab-maven-repo" "https://saalfeldlab.github.io/maven"]
                 ["boundless" "https://repo.boundlessgeo.com/main/"]
                 ["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CI_DEPLOY_USERNAME
                                      :password :env/CI_DEPLOY_PASSWORD
                                      :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]])
