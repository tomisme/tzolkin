(ns tzolkin.devcards.main
  (:require
   [tzolkin.art   :as art]
   [tzolkin.game  :as game]
   [tzolkin.logic :as logic]
   [tzolkin.spec  :as spec  :refer [spec]]
   [tzolkin.utils :as utils :refer [log dif]]

   [tzolkin.devcards.actions]
   [tzolkin.devcards.art]
   [tzolkin.devcards.buildings]
   [tzolkin.devcards.decisions]
   [tzolkin.devcards.end]
   [tzolkin.devcards.logic]
   [tzolkin.devcards.spec]
   [tzolkin.devcards.utils]
   [tzolkin.devcards.workers]

   [tzolkin.devcards.game :refer [s]]
   [tzolkin.devcards.net-game])

  (:require-macros
   [tzolkin.macros :as macros]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))
