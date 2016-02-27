(ns tzolkin.devcards.game
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game  :as game]
   [tzolkin.art   :as art])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(defn new-test-game
  [{:keys [players]}]
  (cond-> logic/initial-game-state
    (> players 0) (update-in [:players] conj (-> logic/new-player-state
                                               (assoc :name "Elisa")
                                               (assoc :color :red)))
    (> players 1) (update-in [:players] conj (-> logic/new-player-state
                                               (assoc :name "Tom")
                                               (assoc :color :blue)))))

(defcard-rg game-test
  (fn [state _]
    (game/board state))
  (new-test-game {:players 2})
  {:inspect-data true :history true})
