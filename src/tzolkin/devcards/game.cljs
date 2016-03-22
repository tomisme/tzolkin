(ns tzolkin.devcards.game
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
                  [:start-game {:test true}]]))

(deftest es-tests
  ; TODO fixme
  ; (testing
  ;   (nod (reduce-events s [[:place-worker {:gear :uxe}]
  ;                          [:place-worker {:gear :uxe}]
  ;                          [:place-worker {:gear :uxe}]
  ;                          [:end-turn]
  ;                          [:place-worker {:gear :yax}]
  ;                          [:place-worker {:gear :yax}]
  ;                          [:end-turn]])
  ;        (-> s
  ;            (update :gears assoc :uxe (into [:red :red :red] (repeat 7 :none)))
  ;            (update :gears assoc :yax (into [:blue :blue] (repeat 8 :none)))
  ;            (update :turn inc)
  ;            (update-in [:players 0 :materials :corn] - 6)
  ;            (update-in [:players 1 :materials :corn] - 2)
  ;            (update-in [:players 0 :workers] - 3)
  ;            (update-in [:players 1 :workers] - 2))))
  (testing
    (nod (reduce-events s [[:end-turn {:pid 0}]
                           [:end-turn {:pid 1}]
                           [:end-turn {:pid 0}]
                           [:end-turn {:pid 1}]])
         (-> s
             (update :turn + 2)))))

(def test-es-atom
  (rg/atom (logic/reduce-es {} game/test-events)))

(def test-local-state-atom
  (rg/atom {:fb-connected? false}))

(defcard-rg local-game-test
  (fn [local-state-atom _]
    (game/board test-es-atom local-state-atom nil))
  test-local-state-atom
  {:inspect-data true})
