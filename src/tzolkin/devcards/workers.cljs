(ns tzolkin.devcards.workers
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest worker-tests
  (testing "Place Worker"
    (is (= (logic/place-worker s 0 :yax)
           (-> s
             (update-in [:players 0 :workers] dec)
             (update-in [:gears :yax] assoc 0 :red)
             (update-in [:active :placed] inc)
             (update :active assoc :worker-option :place))))))