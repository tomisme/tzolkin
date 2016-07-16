(ns tzolkin-devcards.logic
  (:require
   [tzolkin.logic :as logic]
   [tzolkin-devcards.game :refer [s]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [deftest defcard-doc]]
   [cljs.test :refer [testing is]]))

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
             (logic/cost-payable? 0 {:any-resource 1}))))
    (is (true?
         (-> s
             (logic/player-map-adjustment 0 :materials {:corn 3 :wood 2 :stone 1})
             (logic/cost-payable? 0 {:corn 3 :stone 2} :resource))))
    (is (false?
         (-> s
             (logic/player-map-adjustment 0 :materials {:wood 1 :gold 1})
             (logic/cost-payable? 0 {:wood 2 :gold 2} :resource)))))

  (testing "adjust-points"
    (nod (logic/adjust-points s 0 5)
         (update-in s [:players 0 :points] + 5)))

  (testing "adjust-workers"
    (nod (logic/adjust-workers s 0 1)
         (update-in s [:players 0 :workers] inc)))

  (testing "adjust-materials"
    (nod (logic/adjust-materials s 0 {:stone 2 :gold 1})
         (-> s
           (update-in [:players 0 :materials :stone] + 2)
           (update-in [:players 0 :materials :gold] inc))))

  (testing "adjust-temples"
    (nod (logic/adjust-temples s 0 {:chac 2 :quet 1})
         (-> s
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] inc))))
  (testing "adjust-temples: can't go higher than top"
    (nod (-> s
             (logic/adjust-temples 0 {:chac 5})
             (logic/adjust-temples 0 {:chac 1}))
         (-> s
             (logic/adjust-temples 0 {:chac 5}))))
  (testing "adjust-temples: top resets double spin"
    (nod (-> s
             (logic/double-spin)
             (logic/adjust-temples 0 {:chac 5}))
         (-> s
             (update :turn inc)
             (logic/adjust-temples 0 {:chac 5}))))

  (testing "adjust-tech"
    (nod (logic/adjust-tech s 0 {:agri 2 :arch 1})
         (-> s
             (update-in [:players 0 :tech :agri] + 2)
             (update-in [:players 0 :tech :arch] + 1)))
    (nod (logic/adjust-tech s 0 {:agri 4})
         (-> s
             (update-in [:players 0 :tech :agri] + 3)
             (logic/add-decision 0 :temple)))
    (nod (logic/adjust-tech s 0 {:extr 4})
         (-> s
             (update-in [:players 0 :tech :extr] + 3)
             (logic/add-decision 0 :gain-resource)
             (logic/add-decision 0 :gain-resource)))
    (nod (logic/adjust-tech s 0 {:arch 4})
         (-> s
             (update-in [:players 0 :tech :arch] + 3)
             (logic/adjust-points 0 3)))
    (nod (logic/adjust-tech s 0 {:theo 4})
         (-> s
             (update-in [:players 0 :tech :theo] + 3)
             (logic/adjust-materials 0 {:skull 1}))))
  (testing "buy-tech"
    (nod (logic/buy-tech s 0 :arch)
         (-> s
             (update-in [:players 0 :tech :arch] + 1)
             (logic/add-decision 0 :pay-resource)))
    (nod (-> s
             (logic/adjust-tech 0 {:arch 1})
             (logic/buy-tech 0 :arch))
         (-> s
             (update-in [:players 0 :tech :arch] + 2)
             (logic/add-decision 0 :pay-resource)
             (logic/add-decision 0 :pay-resource)))
    (nod (-> s
             (logic/adjust-tech 0 {:arch 2})
             (logic/buy-tech 0 :arch))
         (-> s
             (update-in [:players 0 :tech :arch] + 3)
             (logic/add-decision 0 :pay-resource)
             (logic/add-decision 0 :pay-resource)
             (logic/add-decision 0 :pay-resource)))
    (nod (-> s
             (logic/adjust-tech 0 {:arch 3})
             (logic/buy-tech 0 :arch))
         (-> s
             (update-in [:players 0 :tech :arch] + 3)
             (update-in [:players 0 :points] + 3)
             (logic/add-decision 0 :pay-resource)))))
