(ns tzolkin.logic
  (:require
   [reagent.core :as rg])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(def game-spec
  {:gears
   {:yax {:name "Yaxchilan"
          :teeth 10
          :location 1}
    :tik {:name "Tikal"
          :teeth 10
          :location 6}
    :uxe {:name "Uxmal"
          :teeth 10
          :location 11}
    :chi {:name "Chichen Itza"
          :teeth 13
          :location 16}
    :pal {:name "Palenque"
          :teeth 10
          :location 22}}})

(defcard-doc
  "## Game Spec

  * `:location` defines the gear's spot around the 26 hour clockface of the calendar
  * `:teeth` also defines the number of worker spaces on the gear: `teeth - 2`
  "
  game-spec)

(defcard first-board
  "Here's our first test board. It's just a button that increments the turn number.

  The game logic is simply:

  ```
  (defn next-state
    [prev-state]
    (-> prev-state
      (update :turn inc)))
  ```

  With the magic of devcards, our game state can be inspected and time travelled!"
  (dc/reagent
   (fn [state _]
     (let [next-state (fn [prev-state]
                        (-> prev-state
                          (update :turn inc)))]
       [:div {:class "ui segment"}
        [:div {:class "ui button"
               :onClick (fn [] (swap! state next-state state))}
         "Current Turn: " (:turn @state)]])))
  {:turn 1}
  {:inspect-data true :history true})
