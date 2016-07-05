(ns ^:figwheel-always server.core
  (:require
   [cljs.nodejs :as nodejs]
   [figwheel.client :as fw]
   [matchbox.core :as m]
   [tzolkin.logic :refer [add-event]]))
   ; [tzolkin.db :refer [fb-game setup-game-listener]]))

(enable-console-print!)

(defonce express (nodejs/require "express"))
(defonce serve-static (nodejs/require "serve-static"))
(defonce http (nodejs/require "http"))
(defonce bodyParser (nodejs/require "body-parser"))

(def app (express))
(def jsonParser (.json bodyParser))

(def global-es (atom {}))

; (setup-game-listener global-es)

(defn save-event
  [pid {:keys [type data]}]
  (let [event-stream @global-es]))
    ; (clj->js {:es (m/swap-in! fb-game [:a] add-event)})))

(defn handle-game-msg
  [{:keys [type pid event]}]
  (case type
    "event" (save-event pid event)))

;;;;;;;;;;; example payload:
;;{
;;   "type": "event",
;;    "pid": 0,
;;    "event": {
;;        "type": "place-worker",
;;        "data": {
;;            "gear": "uxe"
;;        }
;;    }
;;}

(. app (put
        "/api/game"
        jsonParser
        (fn [req res]
          (let [payload (js->clj (.-body req) :keywordize-keys true)]
            (do (println (str "=== PUT ===" (.-ip req) " :"))
                (println payload)
                (. res (send (handle-game-msg payload))))))))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def -main
  (fn []
    ;; from https://gist.github.com/bhauman/c63123a5c655d77c3e7f
    ;; allows you to change routes and have them hot loaded as you code
    (doto (.createServer http #(app %1 %2))
      (.listen 3000))))

(set! *main-cli-fn* -main)

(fw/start {})
