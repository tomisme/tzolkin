(ns tzolkin.devcards.decisions
  (:require
   [clojure.data :refer [diff]]
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(deftest decisions
  (testing ":gain-materials"
    (is (= (logic/handle-decision
             (logic/handle-action s 0 [:choose-materials [{:corn 1} {:stone 1}]])
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
             (logic/choose-tech s)
             1)
           (-> s
             (update-in [:players 0 :tech :extr] inc)))))
  (testing ":tech-two"
    (is (= (logic/handle-decision
             (logic/choose-tech-two s)
             2)
           (-> s
             (update-in [:players 0 :tech :arch] inc)
             (assoc-in [:active :decision :type] :tech)
             (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))))
