(ns tzolkin.devcards.game
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game  :as game]
   [tzolkin.art   :as art])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defn new-test-game
  [{:keys [players]}]
  (cond-> logic/initial-game-state
    (> players 0) (logic/add-player "Elisa" :red)
    (> players 1) (logic/add-player "Tom" :blue)))

(def s (new-test-game {:players 2}))

(defcard-rg game-test
  (fn [state _]
    (game/board state))
  s
  {:inspect-data true :history true})
