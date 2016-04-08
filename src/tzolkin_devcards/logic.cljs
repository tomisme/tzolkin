(ns tzolkin-devcards.logic
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.utils :as utils]
   [tzolkin-devcards.game :refer [s]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest helper-tests
  (testing "gear-position"
    (is (= (logic/gear-position :yax 2 0) 2))
    (is (= (logic/gear-position :yax 2 2) 4))
    (is (= (logic/gear-position :yax 4 13) 7))
    (is (= (logic/gear-position :chi 4 14) 5)))
  (testing "gear-slot"
    (is (= (logic/gear-slot :yax 2 0) 2))
    (is (= (logic/gear-slot :yax 4 2) 2))
    (is (= (logic/gear-slot :yax 7 13) 4))
    (is (= (logic/gear-slot :chi 5 14) 4)))
  (testing "cost-payable?"
    (is (true?
         (-> s
             (logic/player-map-adjustment 0 :materials {:corn 3 :wood 2 :stone 1})
             (logic/cost-payable? 0 {:corn 3 :stone 1}))))
    (is (false?
         (-> s
             (logic/player-map-adjustment 0 :materials {:corn 3 :wood 2 :stone 1})
             (logic/cost-payable? 0 {:corn 2 :stone 2}))))
    (is (true?
         (-> s
             (logic/player-map-adjustment 0 :materials {:corn 3 :wood 2 :stone 1})
             (logic/cost-payable? 0 {:any-resource 1}))))
    (is (false?
         (-> s
             (logic/player-map-adjustment 0 :materials {:corn 3 :skull 1})
             (logic/cost-payable? 0 {:any-resource 1})))))
  (testing "adjust-points"
    (is (= (logic/adjust-points s 0 5)
           (-> s
             (update-in [:players 0 :points] + 5)))))
  (testing "adjust-workers"
    (is (= (logic/adjust-workers s 0 1)
           (-> s
             (update-in [:players 0 :workers] inc)))))
  (testing "adjust-materials"
    (is (= (logic/adjust-materials s 0 {:stone 2 :gold 1})
           (-> s
             (update-in [:players 0 :materials :stone] + 2)
             (update-in [:players 0 :materials :gold] inc)))))
  (testing "adjust-temples"
    (is (= (logic/adjust-temples s 0 {:chac 2 :quet 1})
           (-> s
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] inc)))))
  (testing "adjust-tech"
    (is (= (logic/adjust-tech s 0 {:agri 2 :arch 1})
           (-> s
             (update-in [:players 0 :tech :agri] + 2)
             (update-in [:players 0 :tech :arch] inc))))))