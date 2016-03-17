(ns user
  (:use [figwheel-sidecar.repl-api :as ra]))
  ; (:require [cljs.repl] :as repl))

(defn start [] (ra/start-figwheel!))

(defn stop [] (ra/stop-figwheel!))

(defn cljs [] (ra/cljs-repl "dev"))
