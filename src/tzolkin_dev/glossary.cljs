(ns tzolkin-dev.glossary
  (:require-macros
   [devcards.core :refer [defcard-doc]]))

(defcard-doc "
## Dev Glossary
* `slot` refers to an index in a gear's vector of workers.
  Slots rotate as the gear spins.
* `position` refers to the actual board position of a slot.
  Remain static throughout the game
  (e.g. position 1 on `:yax` is always 1 wood)
")
