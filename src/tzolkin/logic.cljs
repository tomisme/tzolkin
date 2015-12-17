(ns tzolkin.logic
  (:require
   [reagent.core :as rg])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(def game-spec
  {:gears
    {:yax {:name "Yaxchilan"
           :teeth 10
           :location 1
           :spaces [{:wood 1}
                    {:stone 1
                     :corn 1}
                    {:gold 1
                     :corn 2}
                    {:skull 1}
                    {:gold 1
                     :stone 1
                     :corn 2}]}
     :tik {:name "Tikal"
           :teeth 10
           :location 6
           :spaces []}
     :uxe {:name "Uxmal"
           :teeth 10
           :location 11
           :spaces []}
     :chi {:name "Chichen Itza"
           :teeth 13
           :location 16
           :spaces []}
     :pal {:name "Palenque"
           :teeth 10
           :location 22
           :spaces [{:corn 3}
                    {:corn 4}
                    {:corn 5
                     :wood 2}
                    {:corn 7
                     :wood 3}
                    {:corn 9
                     :wood 4}]}}
   :trades {:wood 2
            :sone 3
            :gold 4}
   :tech {:agriculture {}
          :extraction {}
          :architecture {}
          :theology {}}
   :temples {:chac {:name "Chaac"
                    :bonus {:age1 6
                            :age2 2}
                    :steps [{:points -1}
                            {:points 0}
                            {:points 2
                             :resource :stone}
                            {:points 4}
                            {:points 6
                             :resource :stone}
                            {:points 7}
                            {:points 8}]}
             :quet {:name "Quetzalcoatl"
                    :bonus {:age1 2
                            :age2 6}
                    :steps [{:points -2}
                            {:points 0}
                            {:points 1}
                            {:points 2
                             :resource :gold}
                            {:points 4}
                            {:points 6
                             :resource :gold}
                            {:points 9}
                            {:points 12}
                            {:points 13}]}
             :kuku {:name "Kukulcan"
                    :bonus {:age1 4
                            :age2 4}
                    :steps [{:points -3}
                            {:points 0}
                            {:points 1
                             :resource :wood}
                            {:points 3}
                            {:points 5
                             :resource :wood}
                            {:points 7
                             :resource :skull}
                            {:points 9}
                            {:points 10}]}}})

(def game-state
  {:skulls 13
   :players []
   :gears {:yax []
           :tik []
           :uxe []
           :chi []
           :pal []}})

(def player-state
  {:resources {:corn 0
               :wood 0
               :stone 0
               :gold 0
               :skull 0}
   :workers 3
   :tech {:agriculture 0
          :extraction 0
          :architecture 0
          :theology 0}
   :tiles {:corn 0
           :wood 0}})

(defcard-doc
  "## Game Spec
  Hello
  ###Gears

  * `:location` defines the gear's location on the 26 hour clockface of the calendar
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
