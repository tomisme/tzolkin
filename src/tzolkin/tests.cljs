(ns tzolkin.tests
  (:require
   [reagent.core :as rg])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(defcard "##Tests

  - If the bank does not have enough crystal skulls to reward all the players who should get one, then no one gets a crystal skull.")
