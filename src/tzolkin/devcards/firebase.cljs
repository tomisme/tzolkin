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
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(m/listen-to db/connected-ref :value #(swap! db/fb-connection assoc :connected (second %)))

(defcard-rg fb-connection-test
  (fn [_ _])
  db/fb-connection
  {:inspect-data true})

#_(rf/register-handler
    :class
    (fn [db [_ command id attribute value]]
      (case command
        :delete (m/dissoc-in! fb-classes [id])
        :update (m/reset-in!  fb-classes [id attribute] value)
        :new    (m/conj!      fb-classes (:class new-default)))
      db))

(def test-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom" :color :blue}]
   [:give-stuff {:pid 0 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:give-stuff {:pid 1 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:start-game]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:end-turn]
   [:place-worker {:pid 1 :gear :yax}]
   [:place-worker {:pid 1 :gear :yax}]
   [:end-turn]])

(def fb-test-es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(db/setup-game-listener fb-test-es-atom)

(db/save (logic/reduce-event-stream {} test-events))

(defcard-rg fb-game-test
  (fn [es-atom _]
    (game/board es-atom))
  fb-test-es-atom)
