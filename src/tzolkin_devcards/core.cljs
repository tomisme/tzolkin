(ns tzolkin-devcards.core
  (:require
   [devcards.core]
   [tzolkin-devcards.actions]
   [tzolkin-devcards.art]
   [tzolkin-devcards.buildings]
   [tzolkin-devcards.decisions]
   [tzolkin-devcards.end]
   [tzolkin-devcards.frame]
   [tzolkin-devcards.logic]
   [tzolkin-devcards.spec]
   [tzolkin-devcards.utils]
   [tzolkin-devcards.workers])
  (:require-macros
   [cljs.test]
   [devcards.core :refer [defcard-doc]]))

(defn tests
  []
  (cljs.test/run-tests
    'tzolkin-devcards.actions
    'tzolkin-devcards.art
    'tzolkin-devcards.buildings
    'tzolkin-devcards.decisions
    'tzolkin-devcards.end
    'tzolkin-devcards.logic
    'tzolkin-devcards.spec
    'tzolkin-devcards.utils
    'tzolkin-devcards.workers))

(defcard-doc
  "## Dev Glossary
  * `slot` refers to an index in a gear's vector of workers.
    Slots rotate as the gear spins.
  * `position` refers to the actual board position of a slot.
    Remain static throughout the game
    (e.g. position 1 on `:yax` is always 1 wood)")

(devcards.core/start-devcard-ui!)
