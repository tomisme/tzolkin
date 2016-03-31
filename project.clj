(defproject tzolkin "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [binaryage/devtools "0.5.2"]
                 [devcards "0.2.1-6"]
                 [timothypratley/reanimated "0.1.4"]
                 [matchbox "0.0.8-SNAPSHOT"]
                 [reagent "0.5.1"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src/tzolkin"]

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [proto-repl "0.1.2"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :source-paths ["src" "dev"]}}

  :cljsbuild {:builds [{:id "devcards"
                        :source-paths ["src/tzolkin" "src/tzolkin_devcards"]
                        :figwheel {:devcards true}
                        :compiler {:main       "tzolkin-devcards.core"
                                   :asset-path "js/compiled/devcards_out"
                                   :output-to  "resources/public/js/compiled/tzolkin_devcards.js"
                                   :output-dir "resources/public/js/compiled/devcards_out"
                                   :source-map-timestamp true}}
                       {:id "dev"
                        :figwheel {:on-jsload tzolkin.core/render-app}
                        :source-paths ["src/tzolkin"]
                        :compiler {:main       "tzolkin.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/tzolkin.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id "prod"
                        :source-paths ["src/tzolkin"]
                        :compiler {:main       "tzolkin.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/tzolkin.js"
                                   :optimizations :advanced}}]}

  :figwheel {:css-dirs ["resources/public/css"]})
