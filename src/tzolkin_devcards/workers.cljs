(ns tzolkin-devcards.workers
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin-devcards.game :refer [s]]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

;; Soooo... it looks like the gear spins once when the game starts (turn 1)
;; and so position 0 is actually slot 1 on
(deftest worker-tests
  (testing "Place Worker"
    (nod (logic/place-worker s :yax)
         (-> s
             (update-in [:players 0 :workers] dec)
             (update-in [:gears :yax] assoc 9 :red)
             (update-in [:active :placed] inc)
             (update :active assoc :worker-option :place)))))
