(ns tzolkin.devcards.main
  (:require
   [tzolkin.devcards.actions]
   [tzolkin.devcards.art]
   [tzolkin.devcards.buildings]
   [tzolkin.devcards.decisions]
   [tzolkin.devcards.end]
   [tzolkin.devcards.logic]
   [tzolkin.devcards.spec]
   [tzolkin.devcards.utils]
   [tzolkin.devcards.workers]

   [tzolkin.devcards.game :as game :refer [s]]
   [tzolkin.devcards.net-game :as net-game])

  (:require-macros
   [tzolkin.macros :as macros]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defn tests
  []
  (run-tests 'tzolkin.devcards.actions
             'tzolkin.devcards.art
             'tzolkin.devcards.buildings
             'tzolkin.devcards.decisions
             'tzolkin.devcards.end
             'tzolkin.devcards.logic
             'tzolkin.devcards.spec
             'tzolkin.devcards.utils
             'tzolkin.devcards.workers
             'tzolkin.devcards.game
             'tzolkin.devcards.net-game))
