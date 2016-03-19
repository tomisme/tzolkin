(ns tzolkin.devcards.net-game
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game :as game]
   [tzolkin.db :as db]
   [reagent.core :as rg]
   [tzolkin.utils :refer [log]]
   [matchbox.core :as m])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(def local-state-atom
  (rg/atom {:fb-connected? false}))

(defcard-rg local-state
  (fn [_ _])
  local-state-atom
  {:inspect-data true})

(def fb-test-es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(db/setup-game-listener fb-test-es-atom)

(db/setup-connection-listener local-state-atom)

(defcard-rg networked-game-test
  (fn [es-atom _]
    (game/board es-atom local-state-atom db/save))
  fb-test-es-atom)
