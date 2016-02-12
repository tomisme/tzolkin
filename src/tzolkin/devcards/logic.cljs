(ns tzolkin.devcards.logic
  (:require
   [tzolkin.art :as art]
   [tzolkin.logic :as logic])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(defcard-doc
  "## Terminology
   * `slot` refers to an index in a gear's vector of workers.
             Slots rotate as the gear spins.
   * `position` refers to the actual board position of a slot. Remain static
     throughout the game (e.g. position 1 on `:yax` is always 1 wood)

   ## Game Spec
   ###Gears
    * `:location` defines the gear's location on the 26 hour clockface of the calendar
    * `:teeth` also defines the number of worker spaces on the gear: `teeth - 2`"
 logic/game-spec)

(deftest inventory-changes
  (testing
    (is (= (logic/apply-to-inventory + {:wood 1 :gold 2 :skull 1} {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (logic/apply-to-inventory - {:stone 1 :gold 1 :corn 9} {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))))

(defn new-test-game
  [{:keys [players]}]
  (cond-> logic/initial-game-state
   (> players 0) (update-in [:players] conj (-> logic/new-player-state
                                              (assoc :name "Elisa")
                                              (assoc :color :red)))
   (> players 1) (update-in [:players] conj (-> logic/new-player-state
                                              (assoc :name "Tom")
                                              (assoc :color :blue)))))

(defn worker-gear
  [{:keys [gear workers on-worker-click on-center-click actions rotation]}]
  ^{:key gear}
  [:svg {:width 300 :height 300}
    [art/gear-el {:cx 150
                  :cy 150
                  :r 75
                  :rotation rotation
                  :teeth (get-in logic/game-spec [:gears gear :teeth])
                  :tooth-height-factor 1.15
                  :tooth-width-factor 0.75
                  :workers workers
                  :gear gear
                  :actions actions
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click}]])

(defn worker-gear-wrapper
  [state gear]
  (let [player-id (get-in @state [:active :player-id])
        on-worker-click (fn [slot]
                          (swap! state logic/remove-worker player-id gear slot))
        on-center-click (fn []
                          (swap! state logic/place-worker player-id gear))
        teeth (get-in logic/game-spec [:gears gear :teeth])]
    (worker-gear {:workers (get-in @state [:gears gear])
                  :gear gear
                  :rotation (* (/ 360 teeth) (:turn @state))
                  :actions (get-in logic/game-spec [:gears gear :actions])
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click})))

(defcard-rg game-test
  (fn [state _]
    [:div
      [art/status-bar @state]
      [:p
        [:button {:on-click #(swap! state logic/end-turn)}
          "End Turn"]]
      (for [[gear _] (get logic/game-spec :gears)]
        (worker-gear-wrapper state gear))])
  (-> (new-test-game {:players 1})
    (update-in [:gears] assoc :yax [:blue :blue nil :blue nil nil :red :red nil nil])
    (update-in [:players 0 :materials] assoc :corn 50)
    (update-in [:players 0] assoc :workers 10))
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
