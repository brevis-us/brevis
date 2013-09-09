(defproject brevis "0.3.1-SNAPSHOT"
  :description "A Second-Generation Artificial Life Simulator"
  :url "https://github.com/kephale/brevis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
;  :resource-paths ["resources"]
  :plugins [[lein-marginalia "0.7.1"]]
  :java-source-paths ["java"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.2"]
;                 [kephale/ode4j "0.12.0-j1.4"]
                 [kephale/ode4j "20130414_001"]
                 [kephale/cantor "0.4.1"]
                 [kephale/penumbra "0.6.5-SNAPSHOT"]
                 ])
