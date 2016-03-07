(ns tzolkin.devcards.main
  (:require
   [clojure.data :as data]
   [tzolkin.logic :as logic]
   [tzolkin.spec :refer [spec]]
   [tzolkin.devcards.art]
   [tzolkin.devcards.buildings]
   [tzolkin.devcards.decisions]
   [tzolkin.devcards.game :refer [s]]
   [tzolkin.devcards.gears]
   [tzolkin.devcards.logic]
   [tzolkin.devcards.spec]
   [tzolkin.devcards.temples]
   [tzolkin.devcards.utils])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defn diff
  [a b]
  (drop-last (data/diff a b)))

(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")
