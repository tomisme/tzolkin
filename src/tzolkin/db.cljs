(ns tzolkin.db
  (:require
   [reagent.core :as rg]
   [matchbox.core :as m]
   [tzolkin.utils :refer [log]]))

(def root
  (m/connect "https://bgames.firebaseio.com"))

(def connected-ref
  (m/connect "https://bgames.firebaseio.com/.info/connected"))

(def fb-connection (rg/atom {:connected false}))

(def fb-game (m/get-in root [:tzolkin]))

(defn setup-game-listener
  [es-atom]
  (m/listen-to fb-game :value (fn [[_ v]]
                                (reset! es-atom (:a v)))))

(defn setup-connection-listener
  []
  (m/listen-to connected-ref :value #(swap! fb-connection assoc :connected (second %))))

(defn save
  [es]
  (m/reset-in! fb-game [:a] es))
