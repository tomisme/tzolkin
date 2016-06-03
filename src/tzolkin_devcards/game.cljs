(ns tzolkin-devcards.game
  (:require
   [reagent.core :as rg]
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game  :as game]
   [tzolkin.art   :as art]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defn reduce-events
  [prev-state events]
  (reduce logic/handle-event prev-state events))

(def s
  (reduce-events {}
                 [[:new-game]
                  [:add-player {:name "Elisa" :color :red}]
                  [:add-player {:name "Tom"   :color :blue}]
                  [:add-player {:name "Aaron" :color :orange}]
                  [:add-player {:name "Jess"  :color :yellow}]
                  [:start-game {:test? true}]]))

(def test-es-atom
  (rg/atom (logic/gen-es game/test-events)))

(def test-local-state-atom
  (rg/atom {:fb-connected? false}))

(defcard-rg local-game-test
  (fn [local-state-atom _]
    (game/board test-es-atom local-state-atom nil))
  test-local-state-atom
  {:inspect-data true})
