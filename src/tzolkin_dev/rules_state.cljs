(ns tzolkin-dev.rules-state
  (:require
   [tzolkin.rules :as rules]
   [clojure.test.check.generators]
   [clojure.spec.alpha :as s])
  (:require-macros
   [devcards.core :refer [defcard]]))


(defcard exercised-states
  (map first (s/exercise :tzolkin.rules/state 4)))


(defcard initial-game-state
  (rules/init-game {}))


(defcard starting-jungle
  (rules/setup-jungle {}))


(defcard random-starting-buildings
  (rules/setup-buildings-monuments {}))
