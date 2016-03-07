(defproject tzolkin "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [binaryage/devtools "0.5.2"]
                 [devcards "0.2.0-8"]
                 [timothypratley/reanimated "0.1.1"]
                 [reagent "0.5.1"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {:builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel {:devcards true}
                        :compiler {:main       "tzolkin.dev-core"
                                   :asset-path "js/compiled/devcards_out"
                                   :output-to  "resources/public/js/compiled/tzolkin_devcards.js"
                                   :output-dir "resources/public/js/compiled/devcards_out"
                                   :source-map-timestamp true}}
                       {:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main       "tzolkin.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/tzolkin.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:main       "tzolkin.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/tzolkin.js"
                                   :optimizations :advanced}}]}

  :figwheel {:css-dirs ["resources/public/css"]})
