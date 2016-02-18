(ns tzolkin.logic
  (:require [tzolkin.spec :refer [spec]]))

(def initial-game-state
  {:turn 0
   :active {:player-id 0
            :worker-option :none
            :placed 0}
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
   :temples {:chac 1
             :quet 1
             :kuku 1}
   :tech {:agriculture 0
          :extraction 0
          :architecture 0
          :theology 0}
   :tiles {:corn 0
           :wood 0}})

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 'seq' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [seq]
  (map vector (iterate inc 0) seq))

(defn first-nil
  "Returns the index of the first instance of nil in 'coll'"
  [coll]
  (first (for [[index element] (indexed coll) :when (= element nil)] index)))

(defn rotate-vec
  "Circularly shifts items in a vector forward 'num' times.

  (rotate-vec [:a :b :c :d :e] 2)  =>  [:d :e :a :b :c]"
  [vec num]
  (let [length (count vec)
        rotations (mod num length)
        break (- length rotations)]
    (into (subvec vec break) (subvec vec 0 break))))

(defn gear-position
  "Returns the current board position of a gear slot after 'turn' spins"
  [gear slot turn]
  (let [teeth (get-in spec [:gears gear :teeth])]
    (mod (+ slot turn) teeth)))

(defn gear-slot
  "Return the gear slot index of a board position after 'turn' spins"
  [gear position turn]
  (let [teeth (get-in spec [:gears gear :teeth])]
    (mod (+ position (- teeth turn)) teeth)))

(defn place-worker
  [state player-id gear]
  (let [worker-option (get-in state [:active :worker-option])
        gear-slots (get-in state [:gears gear])
        max-position (- (get-in spec [:gears gear :teeth]) 2)
        turn (:turn state)
        position (first-nil (rotate-vec gear-slots turn))
        slot (gear-slot gear position turn)
        player (get-in state [:players player-id])
        player-color (:color player)
        remaining-workers (:workers player)
        remaining-corn (get-in state [:players player-id :materials :corn])
        placed (get-in state [:active :placed])
        corn-cost (+ position placed)]
    (if (and (> remaining-workers 0)
             (>= remaining-corn position)
             (< position max-position)
             (or (= :place worker-option) (= :none worker-option)))
      (-> state
        (update-in [:players player-id :workers] dec)
        (update-in [:gears gear] assoc slot player-color)
        (update-in [:players player-id :materials :corn] - corn-cost)
        (update-in [:active :placed] inc)
        (update :active assoc :worker-option :place))
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
    (print ">> give-materials" material-changes player-id)
    (print ">> from" current-materials)
    (print ">> to" updated-materials)
    (-> state
      (assoc-in [:players player-id :materials] updated-materials))))

(defn choose-materials
  [state material-options player-id]
  (-> state
    (assoc-in [:active :decision :type] :gain-materials)
    (assoc-in [:active :decision :options] material-options)))

;; TODO use a conditional threading thing to handle different decision types
;; and then dissoc the decision regardless?
(defn handle-decision
  [state option-index]
  (let [active    (:active state)
        player-id (:player-id active)
        decision  (:decision active)
        type      (:type decision)
        option    (get-in decision [:options option-index])]
    (print ">> handle-decision" type option player-id)
    (case type
      :gain-materials (-> state
                        (give-materials option player-id)
                        (update :active dissoc :decision))
      state)))

(defn handle-action
  [state [k v] player-id]
  (case k
    :gain-materials   (give-materials state v player-id)
    :choose-materials (choose-materials state v player-id)
    state))

(defn remove-worker
  [state player-id gear slot]
  (let [turn (:turn state)
        worker-option (get-in state [:active :worker-option])
        position (gear-position gear slot turn)
        player-color (get-in state [:players player-id :color])
        target-color (get-in state [:gears gear slot])
        action-position (- position 1)
        action (get-in spec [:gears gear :actions action-position])]
    (if (and (= player-color target-color)
             (or (= :pick worker-option) (= :none worker-option)))
      (-> state
        (update-in [:players player-id :workers] inc)
        (update-in [:gears gear] assoc slot nil)
        (update :active assoc :worker-option :pick)
        (handle-action action player-id))
      state)))

(defn end-turn
  [state]
  (let [turn (:turn state)
        max-turn (:total-turns spec)]
    (if (< turn max-turn)
      (-> state
        (update :turn inc)
        (update :active assoc :placed 0)
        (update :active assoc :worker-option :none))
      state)))
