(ns tzolkin.devcards.logic
  (:require
   [tzolkin.art :as art]
   [tzolkin.logic :as logic :refer [initial-game-state new-player-state
                                    game-spec remove-worker place-worker
                                    end-turn]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(defcard-doc
  "## Game Spec
   ###Gears

    * `:location` defines the gear's location on the 26 hour clockface of the calendar
    * `:teeth` also defines the number of worker spaces on the gear: `teeth - 2`"
 game-spec)

(deftest inventory-changes
  (testing
    (is (= (logic/apply-to-inventory + {:wood 1 :gold 2 :skull 1} {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (logic/apply-to-inventory - {:stone 1 :gold 1 :corn 9} {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))))

(defn new-test-game
  [{:keys [players]}]
  (cond-> initial-game-state
   (> players 0) (update-in [:players] conj (-> new-player-state
                                              (assoc :name "Elisa")
                                              (assoc :color :red)))
   (> players 1) (update-in [:players] conj (-> new-player-state
                                              (assoc :name "Tom")
                                              (assoc :color :blue)))))

(defn worker-gear
  [{:keys [gear workers on-worker-click on-center-click actions]}]
  [:svg {:width 300 :height 300}
    [art/gear-el {:cx 150
                  :cy 150
                  :r 75
                  :teeth 10
                  :tooth-height-factor 1.15
                  :tooth-width-factor 0.75
                  :workers workers
                  :gear gear
                  :actions actions
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click}]])

(defn worker-gear-wrapper
  [state gear]
  (let [on-worker-click (fn [slot] (swap! state remove-worker 0 gear slot))
        on-center-click #(swap! state place-worker 0 gear)]
    (worker-gear {:workers (get-in @state [:gears gear])
                  :gear gear
                  :actions (get-in logic/game-spec [:gears gear :actions])
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click})))

(defcard-rg gear-test
  "Click a worker to remove it, click on a fruit to place a worker on
   a specific gear."
  (fn [state _]
    (let [corn (get-in @state [:players 0 :resources :corn])
          remaining-workers (get-in @state [:players 0 :workers])
          resources (get-in @state [:players 0 :resources])]
      [:div
        [:span remaining-workers " workers remaining | "]
        [:span (for [[k v] resources]
                 (str v " " (get art/symbols k)))]
        [:button {:on-click #(swap! state end-turn)}
          "End Turn"]
        (for [[k v] (get logic/game-spec :gears)]
          (worker-gear-wrapper state k))]))
  (-> (new-test-game {:players 1})
    #_(update-in [:gears] assoc :yax [:blue nil nil :blue nil nil :red nil nil nil])
    (update-in [:players 0 :resources] assoc :corn 50)
    (update-in [:players 0] assoc :workers 10))
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

(defcard-doc
  "##Ideas
   Users construct a turn, made up of a sequence of moves, that is published to firebase.

   Other users confirm that the move is valid on their clients.

   Tournaments could involve a third party bot in an umpire slot.

   ```
   [[:place :yax]
    [:place :yax]]

   [[:pick [:yax 1]]
    [:choose :agri]]
    ```")
