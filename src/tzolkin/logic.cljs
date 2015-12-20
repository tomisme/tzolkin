(ns tzolkin.logic
  (:require
   [reagent.core :as rg]
   [tzolkin.art :as art])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]))

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

(def initial-game-state
  {:skulls 13
   :players []
   :gears {:yax [nil nil nil nil nil nil nil nil]
           :tik [nil nil nil nil nil nil nil nil]
           :uxe [nil nil nil nil nil nil nil nil]
           :chi [nil nil nil nil nil nil nil nil nil nil]
           :pal [nil nil nil nil nil nil nil nil]}})

(def new-player-state
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
  * `:teeth` also defines the number of worker spaces on the gear: `teeth - 2`
  "
  game-spec)

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn first-nil
  [coll]
  (first (for [[index element] (indexed coll) :when (= element nil)] index)))

(defn place-worker
  [state player-id gear]
  (let [gear-slots (get-in state [:gears gear])
        gear-location (first-nil gear-slots)
        color (get-in state [:players player-id :color])]
    (-> state
      (update-in [:players player-id :workers] dec)
      (update-in [:gears gear] assoc gear-location color))))

(defcard-rg test-worker-placement
  "Workers are placed on a gear in the first available location."
  (fn [state _]
    [:div
      [:button {:on-click #(swap! state place-worker 0 :yax)}
        "Place a Worker on Yaxchilan"]
      (art/worker-gear-test {:workers (get-in @state [:gears :yax])})])
  (-> (new-test-game {:players 1})
    (update :gears assoc :yax [:blue nil :blue :red nil nil :red nil]))
  {:inspect-data true :history true})

(defcard-rg test-board
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
