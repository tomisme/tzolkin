(ns tzolkin-dev.core
  (:require
   [devcards.core]
   [tzolkin-dev.actions]
   [tzolkin-dev.art]
   [tzolkin-dev.buildings]
   [tzolkin-dev.decisions]
   [tzolkin-dev.end-game]
   [tzolkin-dev.frame]
   [tzolkin-dev.glossary]
   [tzolkin-dev.rules]
   [tzolkin-dev.utils]
   [tzolkin-dev.workers])
  (:require-macros
   [cljs.test]))

(defn run-all-tests
  []
  (cljs.test/run-tests
    'tzolkin-dev.actions
    'tzolkin-dev.art
    'tzolkin-dev.buildings
    'tzolkin-dev.decisions
    'tzolkin-dev.end-game
    'tzolkin-dev.rules
    'tzolkin-dev.utils
    'tzolkin-dev.workers))


(devcards.core/start-devcard-ui!)
