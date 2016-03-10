(ns tzolkin.devcards.game
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game  :as game]
   [tzolkin.art   :as art])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(def s (logic/reduce-events {} [[:new-game]
                                [:add-player {:name "Elisa" :color :red}]
                                [:add-player {:name "Tom" :color :blue}]]))

(defcard-rg game-test
  (fn [state _]
    (game/board state))
  s
  {:inspect-data true :history true})
