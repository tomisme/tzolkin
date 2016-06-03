(defproject tzolkin "0.2.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha3"]
                 [org.clojure/clojurescript "1.9.14"]
                 [binaryage/devtools "0.6.1"]
                 [devcards "0.2.1-7"]
                 [cljsjs/react-dom "0.14.3-1"] ;; fixes (devcards/#106)
                 [timothypratley/reanimated "0.2.0"]
                 [clj-tagsoup "0.3.0"]
                 [reagent "0.6.0-alpha"]
                 [reagent-forms "0.5.24"]
                 [matchbox "0.0.9"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.3-2"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :cljsbuild {:builds [{:id "devcards"
                        :figwheel {:devcards true}
                        :source-paths ["src/tzolkin" "src/tzolkin_devcards"]
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
