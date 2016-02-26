(ns tzolkin.logic
  (:require [tzolkin.spec :refer [spec]]))

(def initial-game-state
  {:turn 0
   :active {:player-id 0
            :worker-option :none
            :placed 0}
   :skulls 13
   :players []
   ;; TODO filter only age 1 buildings at start
   :buildings (shuffle (:buildings spec))
   :monuments (shuffle (:monuments spec))
   :gears {:yax [nil nil nil nil nil nil nil nil nil nil]
           :tik [nil nil nil nil nil nil nil nil nil nil]
           :uxe [nil nil nil nil nil nil nil nil nil nil]
           :chi [nil nil nil nil nil nil nil nil nil nil nil nil nil]
           :pal [nil nil nil nil nil nil nil nil nil nil]}})

(def new-player-state
  {:materials {:corn 0 :wood 0 :stone 0 :gold 0 :skull 0}
   :workers 3
   :points 0
   :buildings []
   :temples {:chac 1 :quet 1 :kuku 1}
   :tech {:agri 0 :extr 0 :arch 0 :theo 0}
   :tiles {:corn 0 :wood 0}
   :starters (take 3 (shuffle (:starters spec)))})

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

(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1  =>  [:a :c])"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn apply-changes-to-map
  "Applies a function 'f' to each value in  'original-map' that has a
  corresponding key in 'changes', supplying the value of that key as the first
  argument to the function.

  If a map of changes is not supplied, applies 'f' to every value.

  (apply-changes-to-map {:a 1 :b 1} + {:a 2})  =>  {:a 3 :b 1}
  (apply-changes-to-map {:a 1 :b 1} inc)  =>  {:a 2 :b 2}"
  ([original-map f]
   (into {} (for [[k v] original-map] [k (f v)])))
  ([original-map f changes]
   (reduce
     (fn [m [k v]] (update m k #(f % v)))
     original-map
     (for [[k v] changes] [k v]))))

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

(defn give-points
  [state player-id points]
  (-> state
    (update-in [:players player-id :points] + points)))

(defn move-temple
  [state player-id temple amount]
  (-> state
    (update-in [:players player-id :temples temple] + amount)))

(defn player-map-adjustment
  [state player-id k changes]
  (let [current (get-in state [:players player-id k])
        updated (apply-changes-to-map current + changes)]
    (-> state
      (assoc-in [:players player-id k] updated))))

(defn adjust-temples
  [state player-id changes]
  (player-map-adjustment state player-id :temples changes))

(defn adjust-materials
  [state player-id changes]
  (player-map-adjustment state player-id :materials changes))

(defn choose-building
  [state]
  (let [num (:num-available-buildings spec)
        buildings (vec (take num (:buildings state)))]
    (-> state
      (assoc-in [:active :decision :type] :build-building)
      (assoc-in [:active :decision :options] buildings))))

(defn choose-materials
  [state material-options]
  (-> state
    (assoc-in [:active :decision :type] :gain-materials)
    (assoc-in [:active :decision :options] material-options)))

(defn choose-any-resource
  [state]
  (choose-materials state [{:wood 1} {:stone 1} {:gold 1}]))

;; IN PROGRESS working on handling buying a building
(defn give-building
  [state id building]
  (let [{:keys [cost #_tech temples materials #_trade build points
                #_gain-worker #_free-action-for-corn]} building]
    (-> (cond-> state
          temples (adjust-temples id temples)
          materials (adjust-materials id materials))
      (update-in [:players id :buildings] conj building)
      (adjust-materials id (apply-changes-to-map cost #(* % -1))))))

(defn pay-skull
  [state player-id details]
  (let [resource (:resource details)
        points (:points details)
        temple (:temple details)]
    (-> (cond-> state
          resource (choose-any-resource)
          points   (give-points player-id points)
          temple   (move-temple player-id temple 1))
      (update-in [:players player-id :materials :skull] dec))))

(defn handle-action
  [state [k v] player-id]
  (case k
    :gain-materials   (adjust-materials state player-id v)
    :choose-materials (choose-materials state v)
    :pay-skull        (pay-skull state player-id v)
    :build            (choose-building state)
    state))

(defn handle-decision
  [state option-index]
  (let [active    (:active state)
        player-id (:player-id active)
        decision  (:decision active)
        type      (:type decision)
        options   (:options decision)
        option    (get options option-index)]
    (case type
      :gain-materials (-> state
                        (adjust-materials player-id option)
                        (update :active dissoc :decision))
      :build-building (-> state
                        (give-building player-id option)
                        (update :buildings remove-from-vec option-index)
                        (update :active dissoc :decision))
      state)))

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
             (not (get-in state [:active :decision]))
             (>= remaining-corn corn-cost)
             (< position max-position)
             (or (= :place worker-option) (= :none worker-option)))
      (-> state
        (update-in [:players player-id :workers] dec)
        (update-in [:gears gear] assoc slot player-color)
        (update-in [:players player-id :materials :corn] - corn-cost)
        (update-in [:active :placed] inc)
        (update :active assoc :worker-option :place))
      state)))

(defn remove-worker
  [state player-id gear slot]
  (let [turn (:turn state)
        worker-option (get-in state [:active :worker-option])
        position (gear-position gear slot turn)
        player (get-in state [:players player-id])
        skulls (get-in player [:materials :skull])
        player-color (:color player)
        target-color (get-in state [:gears gear slot])
        action-position (- position 1)
        action (get-in spec [:gears gear :actions action-position])]
    (if (and (= player-color target-color)
             (not (get-in state [:active :decision]))
             (or (= :pick worker-option) (= :none worker-option))
             (or (not= :chi gear) (> skulls 0)))
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
