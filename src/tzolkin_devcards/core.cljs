(ns tzolkin-devcards.core
  (:require
   [devcards.core]
   [tzolkin.logic :as logic]
   [tzolkin.utils :as utils :refer [log]]
   [tzolkin-devcards.actions]
   [tzolkin-devcards.art]
   [tzolkin-devcards.buildings]
   [tzolkin-devcards.decisions]
   [tzolkin-devcards.end]
   [tzolkin-devcards.logic]
   [tzolkin-devcards.spec]
   [tzolkin-devcards.utils]
   [tzolkin-devcards.workers]
   [tzolkin-devcards.game :refer [s]]
   [tzolkin-devcards.net-game])
  (:require-macros
   [cljs.test]))

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
    'tzolkin-devcards.workers
    'tzolkin-devcards.game
    'tzolkin-devcards.net-game))

(devcards.core/start-devcard-ui!)
