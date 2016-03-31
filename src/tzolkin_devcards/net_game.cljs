(ns tzolkin-devcards.net-game
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

(def test-local-state-atom
  (rg/atom {:fb-connected? false}))

(defcard-rg local-state
  (fn [_ _])
  test-local-state-atom
  {:inspect-data true})

(def test-es-atom
  (rg/atom (logic/gen-es [[:new-game]])))

(db/setup-game-listener test-es-atom)

(db/setup-connection-listener test-local-state-atom)

(defcard-rg networked-game-test
  (fn [es-atom _]
    (game/board es-atom test-local-state-atom db/save))
  test-es-atom)
