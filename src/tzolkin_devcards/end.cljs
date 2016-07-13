(ns tzolkin-devcards.end
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin-devcards.game :refer [s]]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defcard-doc
  "##Food Days
- feed workers: each costs 2 corn, check farms, lose 3 vp per unfed worker
- switch to age 2 buildings on 2nd food day
- give rewards
  - 1st and 3rd food days, give materials depending on temples
  - 2nd and 4th foods days, give points depending on temples
  ")

(deftest food-day-tests
  (testing "starving workers"
    (nod (-> s
             (assoc :turn 14)
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 5})
             (logic/adjust-materials 2 {:corn 4})
             (logic/adjust-materials 3 {:corn 3})
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 14)
             (logic/adjust-points 0 6)
             (logic/adjust-points 1 3)
             (logic/adjust-materials 1 {:corn 1})
             (logic/adjust-points 2 3)
             (logic/adjust-points 3 0)
             (logic/adjust-materials 3 {:corn 1})
             (assoc :buildings []))))
  (testing "farms"
    (nod (-> s
             (assoc :turn 14)
             (update-in [:players 0 :buildings] conj {:farm 1})
             (update-in [:players 1 :buildings] conj {:farm 3})
             (update-in [:players 2 :buildings] conj {:farm :all})
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/adjust-points 0 -6)
             (logic/adjust-points 1 -6)
             (logic/adjust-points 2 -6)
             (logic/adjust-points 3 -6)
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 14)
             (update-in [:players 0 :buildings] conj {:farm 1})
             (update-in [:players 1 :buildings] conj {:farm 3})
             (update-in [:players 2 :buildings] conj {:farm :all})
             (logic/adjust-materials 0 {:corn 2})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 3})
             (assoc :buildings []))))
  (testing "points food day"
    (nod (-> s
             (assoc :turn 14)
             (logic/adjust-temples 0 {:chac 4})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 14)
             (logic/adjust-temples 0 {:chac 4})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-points 0 13)
             (logic/adjust-points 1 8)
             (assoc :buildings [])))
    (nod (-> s
             (assoc :turn 27)
             (logic/adjust-temples 0 {:chac 4 :kuku 1})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 27)
             (logic/adjust-temples 0 {:chac 4 :kuku 1})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-points 0 12)
             (logic/adjust-points 1 10)
             (assoc :buildings []))))
  (testing "mats food day"
    (nod (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:corn 10})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:skull 1 :wood 2 :gold 1 :corn 4})
             (logic/adjust-materials 1 {:stone 2})
             (assoc :buildings [])))
    (nod (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:corn 10})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day)
             (assoc :buildings []))
         (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:skull 1 :wood 2 :gold 1 :corn 4})
             (logic/adjust-materials 1 {:stone 2})
             (assoc :buildings []))))
  (testing "age 2 buildings after second food day"
    (is (= 2
           (-> s
               (assoc :turn 14)
               (logic/food-day)
               :buildings
               (get 0) ;;should probably check *all* the buildings....
               :age)))
    (is (= (-> s
               (assoc :turn 8)
               (logic/food-day)
               :buildings)
           (:buildings s)))))

(deftest starting-player-tests
  (testing "current starting player takes starting player"
    (nod (-> s
             (logic/take-starting-player))
         (-> s
             (update-in [:players 0 :workers] dec)
             (update-in [:active :placed] inc)
             (update :active assoc :worker-option :place)
             (assoc :starting-player-space 0)
             (assoc :new-player-order [1 2 3 0]))))
  (testing "different player takes starting player"
    (nod (-> s
             (update :active assoc :pid 1)
             (logic/take-starting-player))
         (-> s
             (update :active assoc :pid 1)
             (update-in [:players 1 :workers] dec)
             (update-in [:active :placed] inc)
             (update :active assoc :worker-option :place)
             (assoc :starting-player-space 1)
             (assoc :new-player-order [1 0 2 3]))))
  (testing "corn buildup"
    (nod (-> s
             (update :active assoc :pid 3)
             (logic/end-turn)
             (update :active assoc :pid 3)
             (logic/end-turn))
         (-> s
             (assoc :turn 3)
             (assoc :starting-player-corn 2))))
  (testing "taking starting player after corn buildup"
    (nod (-> s
             (update :active assoc :pid 3)
             (assoc :starting-player-corn 6)
             (logic/take-starting-player)
             (logic/end-turn)
             (logic/handle-decision 1)) ;; say no to double spin
         (-> s
             (update :active assoc :pid 3)
             (update :turn inc)
             (assoc :player-order [3 0 1 2])
             (update-in [:players 3 :materials :corn] + 6)))))

(deftest double-spin-tests
  (testing "double spin"
    (nod (-> s
             (logic/take-starting-player)
             (assoc-in [:active :pid] 3)
             (logic/end-turn)
             (logic/handle-decision 0)) ;; say yes to double spin
         (-> s
             (assoc :turn 3)
             (assoc-in [:players 0 :double-spin?] false)
             (assoc :player-order [1 2 3 0])
             (assoc-in [:active :pid] 1))))
  (testing "double spin doesn't skip food day"
    (nod (-> s
             (assoc :turn 7)
             (assoc-in [:active :pid] 3)
             (logic/take-starting-player)
             (logic/end-turn)
             (logic/handle-decision 0)) ;; say yes to double spin
         (-> s
             (assoc :turn 9)
             (assoc-in [:players 3 :double-spin?] false)
             (assoc :player-order [3 0 1 2])
             (assoc-in [:active :pid] 3)
             (logic/adjust-points 0 -9)
             (logic/adjust-points 1 -9)
             (logic/adjust-points 2 -9)
             (logic/adjust-points 3 -9)))))
