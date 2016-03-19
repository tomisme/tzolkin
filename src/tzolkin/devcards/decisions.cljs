(ns tzolkin.devcards.decisions
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest decision-tests
  (testing "gain materials"
    (nod (-> s
             (logic/add-decision :gain-materials [{:corn 1} {:stone 1}])
             (logic/handle-decision 1))
         (-> s
           (update-in [:players 0 :materials :stone] + 1))))
  (testing "gain resource"
    (nod (-> s
             (logic/add-decision :gain-resource)
             (logic/handle-decision 0))
         (-> s
           (update-in [:players 0 :materials :wood] + 1))))
  (testing "pay resource"
    (nod (-> s
             (logic/add-decision :pay-resource)
             (logic/handle-decision 0))
         (-> s
           (update-in [:players 0 :materials :wood] - 1))))
  (testing "build a building"
    (nod (-> s
             (assoc :buildings [{} {:materials {:corn 1}} {}])
             (logic/add-decision :build-building)
             (logic/handle-decision 1))
         (-> s
             (assoc :buildings [{} {}])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 1))))
  (testing "tech"
    (nod (-> s
             (logic/add-decision :tech 1)
             (logic/handle-decision 1))
         (-> s
             (update-in [:players 0 :tech :extr] inc))))
  (testing "2x tech"
    (nod (-> s
             (logic/add-decision :tech 2)
             (logic/handle-decision 2))
         (-> s
             (update-in [:players 0 :tech :arch] inc)
             (update-in [:active :decisions] conj {:type :tech
                                                   :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]}))))
  (testing "temple"
    (nod (-> s
             (logic/add-decision :temple)
             (logic/handle-decision 1))
         (-> s
             (logic/player-map-adjustment 0 :temples {:quet 1}))))
  (testing "two different temples"
    (nod (-> s
             (logic/add-decision :two-different-temples {})
             (logic/handle-decision 1))
         (-> s
             (logic/player-map-adjustment 0 :temples {:chac 1 :kuku 1})))))
