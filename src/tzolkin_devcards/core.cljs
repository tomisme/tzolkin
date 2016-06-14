(ns tzolkin-devcards.core
  (:require
   [devtools.core :as devtools]
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
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [run-tests]]))

(defn setup-dev!
  []
  (devtools/install! :all)
  (enable-console-print!))

(setup-dev!)

(defn tests
  []
  (run-tests 'tzolkin-devcards.actions
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
