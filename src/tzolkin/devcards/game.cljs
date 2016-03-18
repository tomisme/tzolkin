(ns tzolkin.devcards.game
  (:require
   [reagent.core :as rg]
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.game  :as game]
   [tzolkin.art   :as art]
   [tzolkin.utils :refer [log diff]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(def rev logic/reduce-events)

(def s (rev {} [[:new-game]
                [:add-player {:name "Elisa" :color :red}]
                [:add-player {:name "Tom" :color :blue}]
                [:give-stuff {:pid 0 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
                [:give-stuff {:pid 1 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]]))

; (deftest game-tests
;   (testing
;     (is (= (rev s [[:place-worker {:pid 0 :gear :uxe}]
;                    [:place-worker {:pid 0 :gear :uxe}]
;                    [:place-worker {:pid 0 :gear :uxe}]
;                    [:end-turn {:pid 0}]
;                    [:place-worker {:pid 1 :gear :yax}]
;                    [:place-worker {:pid 1 :gear :yax}]
;                    [:end-turn {:pid 1}]])
;            (-> s
;                (update :gears assoc :uxe (into [:red :red :red] (repeat 7 :none)))
;                (update :gears assoc :yax (into [:blue :blue] (repeat 8 :none)))
;                (update :turn inc)
;                (update-in [:players 0 :materials :corn] - 6)
;                (update-in [:players 1 :materials :corn] - 2)
;                (update-in [:players 0 :workers] - 3)
;                (update-in [:players 1 :workers] - 2)))))
;   (testing
;     (is (= (rev s [[:end-turn {:pid 0}]
;                    [:end-turn {:pid 1}]
;                    [:end-turn {:pid 0}]
;                    [:end-turn {:pid 1}]])
;            (-> s
;                (update :turn + 2))))))
;
(def test-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom" :color :blue}]
   [:give-stuff {:pid 0 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:give-stuff {:pid 1 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:start-game]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:end-turn]
   [:place-worker {:pid 1 :gear :yax}]
   [:place-worker {:pid 1 :gear :yax}]
   [:end-turn]])

(def test-event-stream-atom
  (rg/atom (logic/reduce-event-stream {} test-events)))

(defcard-rg game-test
  (fn [es-atom _]
    (game/board es-atom #()))
  test-event-stream-atom)
