(ns tzolkin.logic)

(def game-spec
  {:gears
    {:yax {:name "Yaxchilan"
           :teeth 10
           :location 1
           :actions [[:gain-resources {:wood 1}]
                     [:gain-resources {:stone 1
                                       :corn 1}]
                     [:gain-resources {:gold 1
                                       :corn 2}]
                     [:gain-resources {:skull 1}]
                     [:gain-resources {:gold 1
                                       :stone 1
                                       :corn 2}]
                     [:choose-action-from :yax]
                     [:choose-action-from :yax]]}

     :tik {:name "Tikal"
           :teeth 10
           :location 6
           :actions [[:tech-step :single]
                     [:build :single]
                     [:tech-step :double]
                     [:build :double]
                     [:god-track {:cost {:any-resource 1}
                                  :track :choose-two-different}]
                     [:choose-action-from :tik]
                     [:choose-action-from :tik]]}

     :uxe {:name "Uxmal"
           :teeth 10
           :location 11
           :actions [[:god-track {:cost {:corn 3}
                                  :track :choose-one}]
                     [:trade nil]
                     [:gain-worker nil]
                     [:build :with-corn]
                     [:choose-action-from-all {:cost {:corn 1}}]
                     [:choose-action-from :uxe]
                     [:choose-action-from :uxe]]}
     :chi {:name "Chichen Itza"
           :teeth 13
           :location 16
           :actions [[:pay-skull {:points 4
                                  :god :chac}]
                     [:pay-skull {:points 5
                                  :god :chac}]
                     [:pay-skull {:points 6
                                  :god :chac}]
                     [:pay-skull {:points 7
                                  :god :kuku}]
                     [:pay-skull {:points 8
                                  :god :kuku}]
                     [:pay-skull {:points 8
                                  :god :kuku
                                  :resource true}]
                     [:pay-skull {:points 10
                                  :god :quet}]
                     [:pay-skull {:points 11
                                  :god :quet
                                  :resource true}]
                     [:pay-skull {:points 13
                                  :god :quet
                                  :resource true}]
                     [:choose-action-from :chi]]}

     :pal {:name "Palenque"
           :teeth 10
           :location 22
           :actions [[:gain-resources {:corn 3}]
                     [:gain-resources {:corn 4}]
                     [:choose-resources [{:corn 5}
                                         {:wood 2}]]
                     [:choose-resources [{:corn 7}
                                         {:wood 3}]]
                     [:choose-resources [{:corn 9}
                                         {:wood 4}]]
                     [:choose-action-from :pal]
                     [:choose-action-from :pal]]}}

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

;; TODO: Generate from game-spec
(def initial-game-state
  {:turn 0
   :skulls 13
   :players []
   :gears {:yax [nil nil nil nil nil nil nil nil nil nil]
           :tik [nil nil nil nil nil nil nil nil nil nil]
           :uxe [nil nil nil nil nil nil nil nil nil nil]
           :chi [nil nil nil nil nil nil nil nil nil nil nil nil nil]
           :pal [nil nil nil nil nil nil nil nil nil nil]}})

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

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 'sequence' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [sequence]
  (map vector (iterate inc 0) sequence))

(defn first-nil
  "Returns the index of the first instance of nil in a collection"
  [collection]
  (first (for [[index element] (indexed collection) :when (= element nil)] index)))

(defn gear-position
  "Returns the current board position of a gear slot"
  [state gear slot]
  (let [turn (get state :turn)
        teeth (get-in game-spec [:gears gear :teeth])]
    (mod (+ slot turn) teeth)))

(defn place-worker
  [state player-id gear]
  (let [gear-slots (get-in state [:gears gear])
        slot (first-nil gear-slots)
        position (gear-position state gear slot)
        max-position (- (get-in game-spec [:gears gear :teeth]) 2)
        player-color (get-in state [:players player-id :color])
        remaining-workers (get-in state [:players player-id :workers])
        remaining-corn (get-in state [:players player-id :resources :corn])]
    (if (and (> remaining-workers 0)
             (>= remaining-corn position)
             (< position max-position))
      (-> state
        (update-in [:players player-id :workers] dec)
        (update-in [:gears gear] assoc position player-color)
        (update-in [:players player-id :resources :corn] - position))
      state)))

(defn apply-to-inventory
  [f inventory changes-map]
  (reduce
    (fn [m [k v]] (update m k #(f % v)))
    inventory
    (for [[k v] changes-map] [k v])))

;; TODO: This should be way simpler
(defn give-resources
  [state resources player-id]
  (let [corn (get resources :corn)
        wood (get resources :wood)
        stone (get resources :stone)
        gold (get resources :gold)
        skull (get resources :skull)]
    (-> state
      (update-in [:players player-id :resources :corn] + corn)
      (update-in [:players player-id :resources :wood] + wood)
      (update-in [:players player-id :resources :stone] + stone)
      (update-in [:players player-id :resources :gold] + gold)
      (update-in [:players player-id :resources :skull] + skull))))

(defn handle-action
  [state [k v] player-id]
  (case k
    :gain-resources (give-resources state v player-id)
    state))

(defn remove-worker
  [state player-id gear gear-location]
  (let [player-color (get-in state [:players player-id :color])
        target-color (get-in state [:gears gear gear-location])
        action-location (- gear-location 1)
        action (get-in game-spec [:gears gear :actions action-location])]
    (if (= player-color target-color)
      (-> state
        (update-in [:players player-id :workers] inc)
        (update-in [:gears gear] assoc gear-location nil)
        (handle-action action player-id))
      state)))

(defn end-turn
  [state]
  (-> state
    (update :turn inc)))
