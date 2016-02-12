(ns tzolkin.logic)

(def game-spec
  {:turn-steps [:beg :place :pickup :spin]
   :gears
    {:yax {:name "Yaxchilan"
           :teeth 10
           :location 1
           :actions [[:gain-materials {:wood 1}]
                     [:gain-materials {:stone 1
                                       :corn 1}]
                     [:gain-materials {:gold 1
                                       :corn 2}]
                     [:gain-materials {:skull 1}]
                     [:gain-materials {:gold 1
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
           :actions [[:gain-materials {:corn 3}]
                     [:gain-materials {:corn 4}]
                     [:choose-materials [{:corn 5}
                                         {:wood 2}]]
                     [:choose-materials [{:corn 7}
                                         {:wood 3}]]
                     [:choose-materials [{:corn 9}
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
                             :material :stone}
                            {:points 4}
                            {:points 6
                             :material :stone}
                            {:points 7}
                            {:points 8}]}
             :quet {:name "Quetzalcoatl"
                    :bonus {:age1 2
                            :age2 6}
                    :steps [{:points -2}
                            {:points 0}
                            {:points 1}
                            {:points 2
                             :material :gold}
                            {:points 4}
                            {:points 6
                             :material :gold}
                            {:points 9}
                            {:points 12}
                            {:points 13}]}
             :kuku {:name "Kukulcan"
                    :bonus {:age1 4
                            :age2 4}
                    :steps [{:points -3}
                            {:points 0}
                            {:points 1
                             :material :wood}
                            {:points 3}
                            {:points 5
                             :material :wood}
                            {:points 7
                             :material :skull}
                            {:points 9}
                            {:points 10}]}}})

;; TODO: Generate from game-spec
(def initial-game-state
  {:turn 0
   :active {:player-id 0
            :worker-option :none}
   :skulls 13
   :players []
   :gears {:yax [nil nil nil nil nil nil nil nil nil nil]
           :tik [nil nil nil nil nil nil nil nil nil nil]
           :uxe [nil nil nil nil nil nil nil nil nil nil]
           :chi [nil nil nil nil nil nil nil nil nil nil nil nil nil]
           :pal [nil nil nil nil nil nil nil nil nil nil]}})

(def new-player-state
  {:materials {:corn 0
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
  [gear slot turn]
  (let [teeth (get-in game-spec [:gears gear :teeth])]
    (mod (+ slot turn) teeth)))

(defn gear-slot
  "Return the gear slot index of a board position after 'turn' spins"
  [gear position turn]
  (let [teeth (get-in game-spec [:gears gear :teeth])]
    (mod (+ position (- teeth turn)) teeth)))

(defn rotate-vec
  "Circularly shifts items in a vector forward 'num' times.

  (rotate-vec ['a 'b 'c 'd 'e] 2)  =>  ['d 'e 'a 'b 'c]"
  [vec num]
  (let [break (- (count vec) num)]
    (into (subvec vec break) (subvec vec 0 break))))

(defn place-worker
  [state player-id gear]
  (let [worker-option (get-in state [:active :worker-option])
        gear-slots (get-in state [:gears gear])
        turn (:turn state)
        position (first-nil (rotate-vec gear-slots turn))
        slot (gear-slot gear position turn)
        max-position (- (get-in game-spec [:gears gear :teeth]) 2)
        player-color (get-in state [:players player-id :color])
        remaining-workers (get-in state [:players player-id :workers])
        remaining-corn (get-in state [:players player-id :materials :corn])]
    ; (.log js.console "Gear Slots:")
    ; (.log js.console (clj->js gear-slots))
    ; (.log js.console "Rotated Gear Slots:")
    ; (.log js.console (clj->js (rotate-vec gear-slots turn)))
    ; (.log js/console "placing in position" position ", slot " slot)
    ; (.log js.console "------------------------")
    (if (and (> remaining-workers 0)
             (>= remaining-corn position)
             (< position max-position))
      (-> state
        (update-in [:players player-id :workers] dec)
        (update-in [:gears gear] assoc slot player-color)
        (update-in [:players player-id :materials :corn] - position))
      state)))

(defn apply-to-inventory
  [f inventory changes-map]
  (reduce
    (fn [m [k v]] (update m k #(f % v)))
    inventory
    (for [[k v] changes-map] [k v])))

(defn give-materials
  [state material-changes player-id]
  (let [current-materials (get-in state [:players player-id :materials])
        updated-materials (apply-to-inventory + current-materials material-changes)]
    (-> state
      (assoc-in [:players player-id :materials] updated-materials))))

(defn handle-action
  [state [k v] player-id]
  (case k
    :gain-materials (give-materials state v player-id)
    state))

(defn remove-worker
  [state player-id gear slot]
  (let [turn (:turn state)
        position (gear-position gear slot turn)
        player-color (get-in state [:players player-id :color])
        target-color (get-in state [:gears gear slot])
        action-position (- position 1)
        action (get-in game-spec [:gears gear :actions action-position])]
    ; (.log js/console "picking from position" position ", slot " slot)
    ; (.log js.console "------------------------")
    (if (= player-color target-color)
      (-> state
        (update-in [:players player-id :workers] inc)
        (update-in [:gears gear] assoc slot nil)
        (handle-action action player-id))
      state)))

(defn end-turn
  [state]
  (-> state
    (update :turn inc)))
