(ns tzolkin-dev.actions
  (:require
   [tzolkin.seed :refer [seed]]
   [tzolkin.rules :as rules]
   [tzolkin-dev.test-data :refer [s]]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin-dev.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest action-tests
  "##Yaxchilan"
  (let [gear :yax
        num 0
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:players 0 :materials :wood] + 1))
      (nod (-> s
               (rules/adjust-tech 0 {:extr 1})
               (rules/handle-action 0 action))
           (-> s
               (rules/adjust-tech 0 {:extr 1})
               (update-in [:players 0 :materials :wood] + 2)))))
  (let [gear :yax
        num 1
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :stone] + 1)
               (update-in [:players 0 :materials :corn] + 1)))))
  (let [gear :yax
        num 2
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :gold] + 1)
               (update-in [:players 0 :materials :corn] + 2)))))
  (let [gear :yax
        num 3
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] + 1)))))
  (let [gear :yax
        num 4
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :gold] + 1)
               (update-in [:players 0 :materials :stone] + 1)
               (update-in [:players 0 :materials :corn] + 2)))
      (nod (-> s
               (rules/adjust-tech 0 {:extr 3})
               (rules/handle-action 0 action))
           (-> s
               (rules/adjust-tech 0 {:extr 3})
               (update-in [:players 0 :materials :gold] + 2)
               (update-in [:players 0 :materials :stone] + 2)
               (update-in [:players 0 :materials :corn] + 2)))))
  (let [gear :yax
        num 5
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:yax 0}
                                                             {:yax 1}
                                                             {:yax 2}
                                                             {:yax 3}
                                                             {:yax 4}]}))))
  (let [gear :yax
        num 6
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:yax 0}
                                                             {:yax 1}
                                                             {:yax 2}
                                                             {:yax 3}
                                                             {:yax 4}]}))))
  "##Tikal"
  (let [gear :tik
        num 0
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :tech
                                                   :options [{:agri 1}
                                                             {:extr 1}
                                                             {:arch 1}
                                                             {:theo 1}]}))))
  (let [gear :tik
        num 1
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (let [num (:num-available-buildings seed)
                 buildings (vec (take num (:buildings s)))]
             (update-in s [:active :decisions] conj {:type :build-building
                                                     :options buildings})))))
  (let [gear :tik
        num 2
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:active :decisions] conj {:type :tech
                                                     :options [{:agri 1}
                                                               {:extr 1}
                                                               {:arch 1}
                                                               {:theo 1}]})
               (update-in [:active :decisions] conj {:type :tech
                                                     :options [{:agri 1}
                                                               {:extr 1}
                                                               {:arch 1}
                                                               {:theo 1}]})))))
  ;; TODO
  ; (let [gear :tik
  ;       num 3
  ;       action (get-in seed [:gears gear :actions num])]
  ;   (testing (str gear " " num " " action)
  ;     (nod (rules/handle-action s 0 action)
  ;          false)))
  (let [gear :tik
        num 4
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (rules/add-decision 0 :two-diff-temples {})
               (rules/add-decision 0 :pay-resource {})))))
  (let [gear :tik
        num 5
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}]}))))
  (let [gear :tik
        num 6
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}]}))))
  "##Uxmal"
  (let [gear :uxe
        num 0
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :corn] - 3)
               (update-in [:active :decisions] conj {:type :temple
                                                     :options [{:chac 1} {:quet 1} {:kuku 1}]})))))
  (let [gear :uxe
        num 1
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update s :active assoc :trading? true))))
  (let [gear :uxe
        num 2
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:players 0 :workers] + 1))))
  ;; TODO
  ; (let [gear :uxe
  ;       num 3
  ;       action (get-in seed [:gears gear :actions num])]
  ;   (testing (str gear " " num " " action)
  ;     (nod (rules/handle-action s 0 action)
  ;          false)))
  (let [gear :uxe
        num 4
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :corn] - 1)
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:yax 0} {:yax 1} {:yax 2} {:yax 3} {:yax 4}
                                                               {:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}
                                                               {:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}
                                                               {:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]})))))
  (let [gear :uxe
        num 5
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}]}))))
  (let [gear :uxe
        num 6
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}]}))))
  "##Palenque"
  (let [gear :pal
        num 0
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:players 0 :materials :corn] + 3))
      (nod (-> s
               (rules/adjust-tech 0 {:agri 2})
               (rules/handle-action 0 action))
           (-> s
               (rules/adjust-tech 0 {:agri 2})
               (update-in [:players 0 :materials :corn] + 4)))))
  (let [gear :pal
        num 1
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :corn] + 4)
               (update-in [:jungle 0 :corn-tiles] dec)))
      (nod (-> s
               (rules/adjust-tech 0 {:agri 3})
               (rules/handle-action 0 action))
           (-> s
               (rules/adjust-tech 0 {:agri 3})
               (update-in [:players 0 :materials :corn] + 7)
               (update-in [:jungle 0 :corn-tiles] dec)))))
  (let [gear :pal
        num 2
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :jungle-mats
                                                   :options [{:corn 5} {:wood 2}]
                                                   :jungle-id 1}))
      (nod (-> s
               (update-in [:jungle 1 :wood-tiles] - 4)
               (rules/handle-action 0 action))
           (-> s
               (update-in [:jungle 1 :wood-tiles] - 4)
               (update-in [:players 0 :materials :corn] + 5)
               (update-in [:jungle 1 :corn-tiles] dec)))))
  (let [gear :pal
        num 3
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :jungle-mats
                                                   :options [{:corn 7} {:wood 3}]
                                                   :jungle-id 2}))))
  (let [gear :pal
        num 4
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :jungle-mats
                                                   :options [{:corn 9} {:wood 4}]
                                                   :jungle-id 3}))))
  (let [gear :pal
        num 5
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]}))))
  (let [gear :pal
        num 6
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (update-in s [:active :decisions] conj {:type :action
                                                   :options [{:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]}))))
  "##Chichen Itza"
  (let [gear :chi
        num 0
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 4)
               (update-in [:players 0 :temples :chac] + 1)))))
  (let [gear :chi
        num 1
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 5)
               (update-in [:players 0 :temples :chac] + 1)))))
  (let [gear :chi
        num 2
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 6)
               (update-in [:players 0 :temples :chac] + 1)))))
  (let [gear :chi
        num 3
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 7)
               (update-in [:players 0 :temples :kuku] + 1)))))
  (let [gear :chi
        num 4
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 8)
               (update-in [:players 0 :temples :kuku] + 1)))))
  (let [gear :chi
        num 5
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 8)
               (update-in [:players 0 :temples :kuku] + 1)
               (update-in [:active :decisions] conj {:type :gain-resource
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]})))))
  (let [gear :chi
        num 6
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 10)
               (update-in [:players 0 :temples :quet] + 1)))))
  (let [gear :chi
        num 7
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 11)
               (update-in [:players 0 :temples :quet] + 1)
               (update-in [:active :decisions] conj {:type :gain-resource
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]})))))
  (let [gear :chi
        num 8
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 13)
               (update-in [:players 0 :temples :quet] + 1)
               (update-in [:active :decisions] conj {:type :gain-resource
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]})))))
  (let [gear :chi
        num 9
        action (get-in seed [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (nod (rules/handle-action s 0 action)
           (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:chi 0} {:chi 1} {:chi 2} {:chi 3} {:chi 4} {:chi 5} {:chi 6} {:chi 7} {:chi 8}]}))))))
