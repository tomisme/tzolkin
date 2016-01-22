(ns tzolkin.devcards.logic
  (:require
   [tzolkin.art :as art]
   [tzolkin.logic :as logic :refer [initial-game-state new-player-state
                                    game-spec remove-worker place-worker
                                    end-turn]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]))

(defn new-test-game
  [{:keys [players]}]
  (cond-> initial-game-state
   (> players 0) (update-in [:players] conj (-> new-player-state
                                              (assoc :name "Elisa")
                                              (assoc :color :red)))
   (> players 1) (update-in [:players] conj (-> new-player-state
                                              (assoc :name "Tom")
                                              (assoc :color :blue)))))

(defcard-doc
  "## Game Spec
   ###Gears

    * `:location` defines the gear's location on the 26 hour clockface of the calendar
    * `:teeth` also defines the number of worker spaces on the gear: `teeth - 2`"
  game-spec)

(defcard-rg gear-test
  "Click a worker to remove it."
  (fn [state _]
    (let [corn (get-in @state [:players 0 :resources :corn])
          on-worker-click (fn [slot] (swap! state remove-worker 0 :yax slot))]
      [:div
        [:button {:on-click #(swap! state place-worker 0 :yax)}
          "Place a Worker on Yaxchilan (" corn " corn remaining)"]
        [:button {:on-click #(swap! state end-turn)}
          "End Turn"]
        (art/worker-gear {:workers (get-in @state [:gears :yax])
                          :gear :yax
                          :on-worker-click on-worker-click})]))
  (-> (new-test-game {:players 1})
    (update-in [:gears] assoc :yax [:blue nil nil :blue nil nil :red nil nil nil])
    (update-in [:players 0 :resources] assoc :corn 12))
  {:inspect-data true :history true})

(defcard-rg board-test
  (fn [state-atom _]
    (let [state @state-atom
          players (:players state)]
      [:span "Players: " (map (fn [player]
                                ^{:key (name (:color player))}
                                [:span {:style {:color (name (:color player))}}
                                  (:name player) " "])
                           players)]))
  (new-test-game {:players 2})
  {:inspect-data true :history true})


(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")
