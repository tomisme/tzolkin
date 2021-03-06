(ns tzolkin-dev.workers
  (:require
   [tzolkin.seed :refer [seed]]
   [tzolkin.rules :as rules]
   [tzolkin-dev.test-data :refer [s]]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin-dev.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

;; Soooo... it looks like the gear spins once when the game starts (turn 1)
;; and so position 0 is actually slot 1 on
(deftest worker-tests
  (testing "Place Worker"
    (nod (rules/place-worker s :yax)
         (-> s
             (update-in [:players 0 :workers] dec)
             (update-in [:gears :yax] assoc 9 :red)
             (update-in [:active :placed] inc)
             (update :active assoc :worker-option :place)))))
