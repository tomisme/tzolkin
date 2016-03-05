(ns tzolkin.logic
  (:require
    [tzolkin.spec :refer [spec]]
    [tzolkin.util :refer [indexed first-nil rotate-vec remove-from-vec
                          apply-changes-to-map]]))

(def initial-gears-state
  (into {} (for [[k v] (:gears spec)]
             [k (into [] (repeat (get-in spec [:gears k :teeth]) nil))])))

(def initial-game-state
  {:turn 0
   :active {:player-id 0 :worker-option :none :placed 0}
   :remaining-skulls (:skulls spec)
   :players []
   :buildings (filter #(= 1 (:age %)) (shuffle (:buildings spec)))
   :monuments (take (:num-monuments spec) (shuffle (:monuments spec)))
   :gears initial-gears-state})

(defn initial-player-state
  [name color]
  {:starters (take (:num-starters spec) (shuffle (:starters spec)))
   :materials {:corn 0 :wood 0 :stone 0 :gold 0 :skull 0}
   :temples {:chac 1 :quet 1 :kuku 1}
   :tech {:agri 0 :extr 0 :arch 0 :theo 0}
   :buildings []
   :workers 3
   :points 0
   :name name
   :color color})

(defn add-player
  [state name color]
  (-> state
    (update-in [:players] conj (initial-player-state name color))))

(defn adjust-points
  [state player-id num]
  (-> state
    (update-in [:players player-id :points] + num)))

(defn adjust-workers
  [state player-id num]
  (-> state
    (update-in [:players player-id :workers] + num)))

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
  (-> state
    (player-map-adjustment player-id :materials changes)))

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
  (-> state
    (choose-materials [{:wood 1} {:stone 1} {:gold 1}])))

(defn build-something
  [state type]
  (case type
    :building (choose-building state)))

(defn give-building
  [state id building]
  (let [{:keys [cost #_tech temples materials #_trade build points
                gain-worker #_free-action-for-corn]} building]
    (-> (cond-> state
          build       (build-something build)
          points      (adjust-points id points)
          temples     (adjust-temples id temples)
          materials   (adjust-materials id materials)
          gain-worker (adjust-workers id 1))
      (update-in [:players id :buildings] conj building)
      (adjust-materials id (apply-changes-to-map cost #(* % -1))))))

(defn skull-action
  [state player-id {:keys [resource points temple]}]
  (-> (cond-> state
        resource (choose-any-resource)
        points   (adjust-points player-id points)
        temple   (adjust-temples player-id {temple 1}))
    (update-in [:players player-id :materials :skull] dec)))

(defn handle-action
  [state player-id [k v]]
  (case k
    :trade             state
    :build             (choose-building state)
    :temples           state
    :tech-step         state
    :gain-worker       (adjust-workers state player-id 1)
    :skull-action      (skull-action state player-id v)
    :choose-action     state
    :gain-materials    (adjust-materials state player-id v)
    :choose-materials  (choose-materials state v)))

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
                        (update :active dissoc :decision)))))

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
             (or (= :remove worker-option) (= :none worker-option))
             (or (not= :chi gear) (> skulls 0)))
      (-> state
        (update-in [:players player-id :workers] inc)
        (update-in [:gears gear] assoc slot nil)
        (update :active assoc :worker-option :remove)
        (handle-action player-id action))
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
