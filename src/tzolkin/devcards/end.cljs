(ns tzolkin.devcards.end
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]]
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
  (testing "temple materials"
    (nod (logic/fd-materials-earned :kuku 5)
         {:wood 2
          :skull 1})
    (nod (logic/fd-materials-earned :kuku 2)
         {:wood 1})
    (nod (logic/fd-materials-earned :kuku 0)
         {})
    (nod (logic/fd-materials-earned :quet 3)
         {:gold 1})))
