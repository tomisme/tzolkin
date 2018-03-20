(ns tzolkin.db
  (:require
   [matchbox.core]))
   ; [tzolkin.utils :refer [log]]))

(def base-ref
  (matchbox.core/connect "https://playtzolkin.firebaseio.com/"))


(def connection-ref
  (matchbox.core/connect "https://bgames.firebaseio.com/.info/connected"))


(def games-ref
  (matchbox.core/get-in base-ref [:tzolkin]))


(defn setup-game-listener
  [es-atom]
  (let [handler (fn [[_ new-val]]
                 (reset! es-atom (:a new-val)))]
   (matchbox.core/listen-to games-ref :value handler)))


(defn setup-connection-listener
  [local-state-atom]
  (let [handler (fn [[_ new-val]]
                 (swap! local-state-atom assoc :fb-connected? new-val))]
   (matchbox.core/listen-to connection-ref :value handler)))


(defn save
  [event-stream]
  (matchbox.core/reset-in! games-ref [:a] event-stream))
