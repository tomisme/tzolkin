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
  (testing ":points-food-day"
    (nod (-> s
             (assoc :turn 14)
             (logic/adjust-temples 0 {:chac 4})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day))
         (-> s
             (assoc :turn 14)
             (logic/adjust-temples 0 {:chac 4})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-points 0 13)
             (logic/adjust-points 1 8)))
    (nod (-> s
             (assoc :turn 27)
             (logic/adjust-temples 0 {:chac 4 :kuku 1})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-materials 0 {:corn 6})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day))
         (-> s
             (assoc :turn 27)
             (logic/adjust-temples 0 {:chac 4 :kuku 1})
             (logic/adjust-temples 1 {:quet 1 :kuku 1})
             (logic/adjust-points 0 12)
             (logic/adjust-points 1 10))))
  (testing ":mats-food-day"
    (nod (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:corn 10})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day))
         (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:skull 1 :wood 2 :gold 1 :corn 4})
             (logic/adjust-materials 1 {:stone 2})))
    (nod (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:corn 10})
             (logic/adjust-materials 1 {:corn 6})
             (logic/adjust-materials 2 {:corn 6})
             (logic/adjust-materials 3 {:corn 6})
             (logic/food-day))
         (-> s
             (assoc :turn 8)
             (logic/adjust-temples 0 {:kuku 4 :quet 2})
             (logic/adjust-temples 1 {:chac 3})
             (logic/adjust-materials 0 {:skull 1 :wood 2 :gold 1 :corn 4})
             (logic/adjust-materials 1 {:stone 2})))))
