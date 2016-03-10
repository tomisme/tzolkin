(ns tzolkin.devcards.events
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest event-tests
  (testing "setup"
    (let [events [[:new-game {:players 2}]
                  [:set-player {:pid 0 :name "Elisa" :color :red}]
                  [:set-player {:pid 1 :name "Tom" :color :blue}]]]
      (is (= (logic/reduce-events {} events)
             s))))
  (testing "place and remove"
    (let [events [[]
                  []]]
      (is (= (logic/reduce-events s events)
             s)))))
