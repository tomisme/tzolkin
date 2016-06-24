(ns ^:figwheel-always server.core
  (:require
   [cljs.nodejs :as nodejs]
   [figwheel.client :as fw]))

(enable-console-print!)

(defonce express (nodejs/require "express"))
(defonce serve-static (nodejs/require "serve-static"))
(defonce http (nodejs/require "http"))

(def app (express))

(. app (get "/hello"
         (fn [req res] (. res (send "Hello San Fran")))))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def -main
  (fn []
    ;; from https://gist.github.com/bhauman/c63123a5c655d77c3e7f
    ;; "allows you to change routes and have them hot loaded as you code"
    (doto (.createServer http #(app %1 %2))
      (.listen 3000))))

(set! *main-cli-fn* -main)

(fw/start {})
