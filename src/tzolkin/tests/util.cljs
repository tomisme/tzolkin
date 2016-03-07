(ns tzolkin.tests.util
  (:require
   [tzolkin.devcards.game :as dev-game]))

(def s (dev-game/new-test-game {:players 2}))
