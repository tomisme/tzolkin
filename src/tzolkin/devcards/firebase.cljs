(ns tzolkin.devcards.firebase
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game :as game]
   [tzolkin.db :as db]
   [reagent.core :as rg]
   [tzolkin.utils :refer [log]]
   [matchbox.core :as m])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(m/listen-to db/connected-ref :value #(swap! db/fb-connection assoc :connected (second %)))

(defcard-rg fb-connection-test
  (fn [_ _])
  db/fb-connection
  {:inspect-data true})

(def fb-test-es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(db/setup-game-listener fb-test-es-atom)

(defcard-rg fb-game-test
  (fn [es-atom _]
    (game/board es-atom db/save))
  fb-test-es-atom)
