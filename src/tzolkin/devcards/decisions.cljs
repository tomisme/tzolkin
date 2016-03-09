(ns tzolkin.devcards.decisions
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest decisions
  (testing ":gain-materials"
    (is (= (logic/handle-decision
             (-> s
               (logic/handle-action 0 [:choose-materials [{:corn 1} {:stone 1}]]))
             1)
           (-> s
             (update-in [:players 0 :materials :stone] + 1)))))
  (testing ":gain-building"
    (is (= (logic/handle-decision
             (-> s
               (assoc :buildings [{} {:materials {:corn 1}} {}])
               (logic/choose-building))
             1)
           (-> s
             (assoc :buildings [{} {}])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 1)))))
  (testing ":tech"
    (is (= (logic/handle-decision
             (-> s
               (logic/choose-tech))
             1)
           (-> s
             (update-in [:players 0 :tech :extr] inc)))))
  (testing ":tech-two"
    (is (= (logic/handle-decision
             (-> s
               (logic/choose-tech-two))
             2)
           (-> s
             (update-in [:players 0 :tech :arch] inc)
             (update-in [:active :decisions] conj {:type :tech
                                                   :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]})))))
  (testing ":two-different-temples"
    (is (= (logic/handle-decision
             (-> s
               (logic/add-decision 0 :two-different-temples {}))
             1)
           (-> s
             (logic/player-map-adjustment 0 :temples {:chac 1 :kuku 1}))))))
