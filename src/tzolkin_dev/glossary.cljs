(ns tzolkin-dev.glossary
  (:require-macros
   [devcards.core :refer [defcard-doc]]))

(defcard-doc "
## Dev Glossary
* `seed` is a large map passed into each game that provides static
  information about the board setup.
* `slot` refers to an index in a gear's vector of workers.
  Slots rotate as the gear spins.
* `position` refers to the actual board position of a slot.
  Remain static throughout the game
  (e.g. position 1 on `:yax` is always 1 wood)
* `mat` material
* `amt` amount
* `idx` index
")
