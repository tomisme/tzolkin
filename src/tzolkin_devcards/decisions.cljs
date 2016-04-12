(ns tzolkin-devcards.decisions
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.utils :refer [log]]
   [tzolkin.logic :as logic]
   [tzolkin-devcards.game :refer [s]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest decision-tests
  (testing "starter"
    (nod (-> s
             (logic/add-decision 0 :starters [{} {} {:materials {:corn 2}
                                                     :temple :chac
                                                     :tech :arch
                                                     :tik 1}])
             (logic/handle-decision 2))
         (-> s
             (update-in [:players 0 :materials :corn] + 2)
             (update-in [:players 0 :temples :chac] inc)
             (update-in [:players 0 :tech :arch] inc))))
  (testing "gain materials"
    (nod (-> s
             (logic/add-decision 0 :gain-materials [{:corn 1} {:stone 1}])
             (logic/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :stone] + 1))))
  (testing "jungle materials"
    (nod (-> s
             (logic/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 1})
             (logic/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :wood] + 2)
             (update-in [:jungle 1 :wood-tiles] - 1)))
    (nod (-> s
             (update-in [:jungle 2 :wood-tiles] - 1)
             (logic/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (logic/handle-decision 0))
         (-> s
             (update-in [:jungle 2 :wood-tiles] - 1)
             (update-in [:players 0 :materials :corn] + 3)
             (update-in [:jungle 2 :corn-tiles] - 1)))
    (nod (-> s
             (logic/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (logic/handle-decision 0))
         (-> s
             (update-in [:players 0 :materials :corn] + 3)
             (update-in [:jungle 2 :corn-tiles] - 1)
             (update-in [:jungle 2 :wood-tiles] - 1)
             (logic/add-decision 0 :anger-god))))
  (testing "gain resource"
    (nod (-> s
             (logic/add-decision 0 :gain-resource)
             (logic/handle-decision 0))
         (-> s
             (update-in [:players 0 :materials :wood] + 1))))
  (testing "pay resource"
    (nod (-> s
             (update-in [:players 0 :materials :wood] inc)
             (logic/add-decision 0 :pay-resource)
             (logic/handle-decision 0))
         s)
    (nod (-> s
             (logic/add-decision 0 :pay-resource)
             (logic/handle-decision 0))
         (-> s (update :errors conj (str "Can't pay resource cost: {:wood 1}"))
               (logic/add-decision 0 :pay-resource))))
  (testing "build a building"
    (nod (-> s
             (assoc :buildings [{} {:materials {:corn 1}} {}])
             (logic/add-decision 0 :build-building)
             (logic/handle-decision 1))
         (-> s
             (assoc :buildings [{} {}])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 1)))
    (nod (-> s
             (update-in [:players 0 :materials :corn] + 1)
             (assoc :buildings [{} {:cost {:wood 1 :stone 1}
                                    :materials {:gold 2}} {}])
             (logic/add-decision 0 :build-building)
             (logic/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :corn] + 1)
             (assoc :buildings [{} {:cost {:wood 1 :stone 1}
                                    :materials {:gold 2}} {}])
             (logic/add-decision 0 :build-building)
             (update :errors conj (str "Can't buy building: {:cost {:wood 1, :stone 1}, :materials {:gold 2}}")))))

  (testing "tech"
    (nod (-> s
             (logic/add-decision 0 :tech 1)
             (logic/handle-decision 1))
         (-> s
             (update-in [:players 0 :tech :extr] inc))))
  (testing "2x tech"
    (nod (-> s
             (logic/add-decision 0 :tech 2)
             (logic/handle-decision 2))
         (-> s
             (update-in [:players 0 :tech :arch] inc)
             (update-in [:active :decisions] conj {:type :tech
                                                   :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]}))))
  (testing "temple"
    (nod (-> s
             (logic/add-decision 0 :temple)
             (logic/handle-decision 1))
         (-> s
             (logic/player-map-adjustment 0 :temples {:quet 1}))))
  (testing "two different temples"
    (nod (-> s
             (logic/add-decision 0 :two-diff-temples {})
             (logic/handle-decision 1))
         (-> s
             (logic/player-map-adjustment 0 :temples {:chac 1 :kuku 1})))))
