(ns tzolkin-dev.decisions
  (:require
   [tzolkin.seed :refer [seed]]
   [tzolkin.utils :refer [log]]
   [tzolkin.rules :as rules]
   [tzolkin-dev.test-data :refer [s]])
  (:require-macros
   [tzolkin-dev.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest decision-tests
  (testing "starter"
    (nod (-> s
             (rules/add-decision 0 :starters [{} {} {:materials {:corn 3}}])
             (rules/handle-decision 2))
         (-> s
             (update-in [:players 0 :materials :corn] + 3)))
    (nod (-> s
             (rules/add-decision 0 :starters [{} {} {:materials {:corn 2}
                                                     :temple :chac
                                                     :tech :arch
                                                     :tik 1}])
             (rules/handle-decision 2))
         (-> s
             (rules/add-decision 0 :beg?)
             (update-in [:players 0 :materials :corn] + 2)
             (update-in [:players 0 :temples :chac] inc)
             (update-in [:players 0 :tech :arch] inc))))
  (testing "beg for corn"
    (nod (-> s
             (rules/add-decision 0 :beg?)
             (rules/handle-decision 0))
         (-> s
             (update-in [:players 0 :materials :corn] + 3)
             (rules/add-decision 0 :anger-god)))
    (nod (-> s
             (rules/add-decision 0 :beg?)
             (rules/handle-decision 1))
         s))
  (testing "gain materials"
    (nod (-> s
             (rules/add-decision 0 :gain-materials [{:corn 1} {:stone 1}])
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :stone] + 1))))
  (testing "jungle materials"
    (nod (-> s
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 1})
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :wood] + 2)
             (update-in [:jungle 1 :wood-tiles] - 1)))
    (nod (-> s
             (rules/adjust-tech 0 {:extr 1})
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 1})
             (rules/handle-decision 1))
         (-> s
             (rules/adjust-tech 0 {:extr 1})
             (update-in [:players 0 :materials :wood] + 3)
             (update-in [:jungle 1 :wood-tiles] - 1)))
    (nod (-> s
             (rules/adjust-tech 0 {:agri 3})
             (update-in [:jungle 2 :wood-tiles] - 1)
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-tech 0 {:agri 3})
             (update-in [:jungle 2 :wood-tiles] - 1)
             (update-in [:players 0 :materials :corn] + 6)
             (update-in [:jungle 2 :corn-tiles] - 1)))
    (nod (-> s
             (update-in [:jungle 2 :wood-tiles] - 1)
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (rules/handle-decision 0))
         (-> s
             (update-in [:jungle 2 :wood-tiles] - 1)
             (update-in [:players 0 :materials :corn] + 3)
             (update-in [:jungle 2 :corn-tiles] - 1)))
    (nod (-> s
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (rules/handle-decision 0))
         (-> s
             (update-in [:players 0 :materials :corn] + 3)
             (update-in [:jungle 2 :corn-tiles] - 1)
             (update-in [:jungle 2 :wood-tiles] - 1)
             (rules/add-decision 0 :anger-god)))
    (nod (-> s
             (rules/adjust-tech 0 {:agri 2})
             (rules/add-decision 0 :jungle-mats {:options [{:corn 3} {:wood 2}]
                                                 :jungle-id 2})
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-tech 0 {:agri 2})
             (update-in [:players 0 :materials :corn] + 4))))
  (testing "gain resource"
    (nod (-> s
             (rules/add-decision 0 :gain-resource)
             (rules/handle-decision 0))
         (-> s
             (update-in [:players 0 :materials :wood] + 1))))
  (testing "pay resource"
    (nod (-> s
             (update-in [:players 0 :materials :wood] inc)
             (rules/add-decision 0 :pay-resource)
             (rules/handle-decision 0))
         s)
    (nod (-> s
             (rules/add-decision 0 :pay-resource)
             (rules/handle-decision 0))
         (-> s (update :errors conj (str "Can't pay resource cost: {:wood 1}"))
             (rules/add-decision 0 :pay-resource))))
  (testing "build a building"
    (nod (-> s
             (assoc :buildings [{} {:materials {:corn 1}} {}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 1))
         (-> s
             (assoc :buildings [{} {}])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 1)))
    (nod (-> s
             (update-in [:players 0 :materials :corn] + 1)
             (assoc :buildings [{} {:cost {:wood 1 :stone 1}
                                    :materials {:gold 2}} {}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :materials :corn] + 1)
             (assoc :buildings [{}
                                {:cost {:wood 1 :stone 1}
                                 :materials {:gold 2}}
                                {}])
             (rules/add-decision 0 :build-building)
             (update :errors conj (str "Can't afford building: {:cost {:wood 1, :stone 1}, :materials {:gold 2}}"))))
    (nod (-> s
             (rules/adjust-tech 0 {:arch 1})
             (assoc :buildings [{:materials {:corn 1}}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-tech 0 {:arch 1})
             (assoc :buildings [])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 2)))
    (nod (-> s
             (rules/adjust-tech 0 {:arch 2})
             (assoc :buildings [{:materials {:corn 1}}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-tech 0 {:arch 2})
             (assoc :buildings [])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 2)
             (update-in [:players 0 :points] + 2)))
    (nod (-> s
             (rules/adjust-tech 0 {:arch 3})
             (assoc :buildings [{:cost {:wood 1}
                                 :materials {:corn 1}}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-tech 0 {:arch 3})
             (assoc :buildings [])
             (update-in [:players 0 :buildings] conj {:cost {:wood 1}
                                                      :materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 2)
             (update-in [:players 0 :points] + 2)
             (update-in [:active :decisions] conj {:type :pay-discount
                                                   :options [{:wood 1}
                                                             {:stone 1}
                                                             {:gold 1}]
                                                   :cost {:wood 1}})))
    (nod (-> s
             (rules/adjust-materials 0 {:wood 2 :stone 2})
             (rules/adjust-tech 0 {:arch 3})
             (assoc :buildings [{:cost {:wood 2 :stone 1}}])
             (rules/add-decision 0 :build-building)
             (rules/handle-decision 0))
         (-> s
             (rules/adjust-materials 0 {:wood 2 :stone 2})
             (rules/adjust-tech 0 {:arch 3})
             (assoc :buildings [])
             (update-in [:players 0 :materials :corn] + 1)
             (update-in [:players 0 :points] + 2)
             (update-in [:players 0 :buildings] conj {:cost {:wood 2
                                                             :stone 1}})
             (update-in [:active :decisions] conj {:type :pay-discount
                                                   :options [{:wood 1}
                                                             {:stone 1}
                                                             {:gold 1}]
                                                   :cost {:wood 2 :stone 1}}))))
  (testing "pay-discount"
    (nod (-> s
             (rules/adjust-materials 0 {:wood 1 :stone 1})
             (rules/add-decision 0 :pay-discount {:cost {:wood 2 :stone 1}})
             (rules/handle-decision 0))
         s)
    (nod (-> s
             (rules/adjust-materials 0 {:stone 1})
             (rules/add-decision 0 :pay-discount {:cost {:stone 1}})

             (rules/handle-decision 2))
         s))
  (testing "tech"
    (nod (-> s
             (rules/add-decision 0 :tech 1)
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :tech :extr] inc)
             (rules/add-decision 0 :pay-resource))))
  (testing "2x tech"
    (nod (-> s
             (rules/add-decision 0 :tech 2)
             (rules/handle-decision 2))
         (-> s
             (update-in [:players 0 :tech :arch] inc)
             (update-in [:active :decisions] conj {:type :tech
                                                   :options [{:agri 1}
                                                             {:extr 1}
                                                             {:arch 1}
                                                             {:theo 1}]})
             (rules/add-decision 0 :pay-resource))))
  (testing "free-tech"
    (nod (-> s
             (rules/add-decision 0 :free-tech 1)
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :tech :extr] inc))))
  (testing "2x free-tech"
    (nod (-> s
             (rules/add-decision 0 :free-tech 2)
             (rules/handle-decision 1))
         (-> s
             (update-in [:players 0 :tech :extr] inc)
             (update-in [:active :decisions] conj {:type :free-tech
                                                   :options [{:agri 1}
                                                             {:extr 1}
                                                             {:arch 1}
                                                             {:theo 1}]}))))
  (testing "temple"
    (nod (-> s
             (rules/add-decision 0 :temple)
             (rules/handle-decision 1))
         (-> s
             (rules/player-map-adjustment 0 :temples {:quet 1}))))
  (testing "two different temples"
    (nod (-> s
             (rules/add-decision 0 :two-diff-temples {})
             (rules/handle-decision 1))
         (-> s
             (rules/player-map-adjustment 0 :temples {:chac 1 :kuku 1})))))
