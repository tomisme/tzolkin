(ns tzolkin.logic
  (:require
    [tzolkin.spec :refer [spec]]
    [tzolkin.utils :refer [log indexed first-nil rotate-vec
                           remove-from-vec change-map negatise-map]]))

(def initial-gears-state
  (into {} (for [[k v] (:gears spec)]
             [k (into [] (repeat (get-in spec [:gears k :teeth]) nil))])))

(def initial-game-state
  {:turn 0
   :active {:pid 0 :worker-option :none :placed 0 :decisions '()}
   :remaining-skulls (:skulls spec)
   :players []
   :buildings (vec (filter #(= 1 (:age %)) (shuffle (:buildings spec))))
   :monuments (vec (take (:num-monuments spec) (shuffle (:monuments spec))))
   :gears initial-gears-state})

(defn add-player
  [state name color]
  (let [p {:starters (take (:num-starters spec) (shuffle (:starters spec)))
           :materials {:corn 0 :wood 0 :stone 0 :gold 0 :skull 0}
           :temples {:chac 1 :quet 1 :kuku 1}
           :tech {:agri 0 :extr 0 :arch 0 :theo 0}
           :buildings []
           :workers 3
           :points 0
           :name name
           :color color}]
    (update state :players conj p)))

(defn adjust-points
  [state pid num]
  (update-in state [:players pid :points] + num))

(defn adjust-workers
  [state pid num]
  (update-in state [:players pid :workers] + num))

(defn player-map-adjustment
  [state pid k changes]
  (let [current (get-in state [:players pid k])
        updated (change-map current + changes)]
    (assoc-in state [:players pid k] updated)))
;; == TODO are these necessary? ==
(defn adjust-temples
  [state pid changes]
  (player-map-adjustment state pid :temples changes))

(defn adjust-materials
  [state pid changes]
  (player-map-adjustment state pid :materials changes))

(defn adjust-tech
  [state pid changes]
  (player-map-adjustment state pid :tech changes))
;; ==============

(defn choose-building
  [state]
  (let [num (:num-available-buildings spec)
        decision {:type :gain-building
                  :options (vec (take num (:buildings state)))}]
    (-> state
      (update-in [:active :decisions] conj decision))))

(defn choose-monument
  [state]
  (let [decision {:type :gain-monument :options (:monuments state)}]
    (-> state
      (update-in [:active :decisions] conj decision))))

(defn choose-materials
  [state material-options]
  (let [decision {:type :gain-materials :options material-options}]
    (-> state
      (update-in [:active :decisions] conj decision))))

(defn choose-resource
  [state]
  (choose-materials state [{:wood 1} {:stone 1} {:gold 1}]))

(def tech-options
  (into [] (for [[k v] (:tech spec)] {k 1})))

(defn choose-tech
  [state]
  (let [decision {:type :tech :options tech-options}]
    (-> state
      (update-in [:active :decisions] conj decision))))

(defn choose-tech-two
  [state]
  (let [decision {:type :tech :options tech-options}]
    (-> state
      (update-in [:active :decisions] conj decision)
      (update-in [:active :decisions] conj decision))))

(def two-different-temple-options
  (into [] (for [t1 (keys (:temples spec)) t2 (keys (:temples spec))
                 :when (not= t1 t2)]
             [t1 t2])))

(defn add-decision
  [state pid type data]
  (let [options (case type
                  :action (if (= :non-chi (:gear data))
                            (into [] (for [g '(:yax :tik :uxe :pal) x (range 5)] {g x}))
                            (let [num (get-in spec [:gears (:gear data) :regular-actions])]
                              (into [] (for [x (range num)] {(:gear data) x}))))
                  :temple (into [] (for [[temple _] (:temples spec)] {temple 1}))
                  :pay-resource [{:wood 1} {:stone 1} {:gold 1}]
                  :resource [{:wood 1} {:stone 1} {:gold 1}]
                  :materials data
                  :two-different-temples two-different-temple-options)]
    (-> state
      (update-in [:active :decisions] conj {:type type :options options}))))

(defn build-builder-building
  [state pid build]
  (case build
    :building (choose-building state)
    :monument (choose-monument state)))

(defn build-tech-building
  [state pid tech]
  (case tech
    :any (choose-tech state)
    :any-two (choose-tech-two state)
    (adjust-tech state pid tech)))

(defn give-building
  [state pid building]
  (let [{:keys [cost tech temples materials #_trade build points
                gain-worker #_free-action-for-corn]} building]
    (-> (cond-> state
          build       (build-builder-building pid build)
          points      (adjust-points pid points)
          temples     (adjust-temples pid temples)
          materials   (adjust-materials pid materials)
          gain-worker (adjust-workers pid 1)
          tech        (build-tech-building pid tech))
      (update-in [:players pid :buildings] conj building)
      (adjust-materials pid (negatise-map cost)))))

(defn skull-action
  [state pid {:keys [resource points temple]}]
  (-> (cond-> state
        resource (choose-resource)
        points   (adjust-points pid points)
        temple   (adjust-temples pid {temple 1}))
    (update-in [:players pid :materials :skull] dec)))

(defn pay-action-cost
  [state pid [action-type action-data]]
  (let [cost (:cost action-data)
        any-resource-cost (:any-resource cost)]
    (if any-resource-cost
      (add-decision state pid :pay-resource {})
      (adjust-materials state pid (negatise-map cost)))))

(defn handle-action
  [state pid [k v]]
  (-> (cond-> state
        (= :skull-action k) (skull-action pid v)
        (= :gain-worker k) (adjust-workers pid 1)
        (= :gain-materials k) (adjust-materials pid v)
        (= :choose-action k) (add-decision pid :action v)
        (and (= :temples k) (= :any (:choose v))) (add-decision pid :temple v)
        (and (= :temples k) (= :two-different (:choose v))) (add-decision pid :two-different-temples v)
        (and (= :tech k) (= 1 (:steps v))) (choose-tech)
        (and (= :tech k) (= 2 (:steps v))) (choose-tech-two)
        (= :choose-materials k) (choose-materials v)
        (and (= :build k) (= :single (:type v))) (choose-building))
    (pay-action-cost pid [k v])))

(defn handle-decision
  [{:keys [active] :as state} option-index]
  (let [pid        (:pid active)
        decision  (first (:decisions active))
        type      (:type decision)
        options   (:options decision)
        choice    (get options option-index)]
    (-> (cond-> state
          (= :gain-materials type) (adjust-materials pid choice)
          (= :gain-building type) (-> (give-building pid choice)
                                    (update :buildings remove-from-vec option-index))
          (= :tech type) (adjust-tech pid choice)
          (= :tech-two type) (adjust-tech pid choice)
          (= :temple type) (adjust-temples pid choice)
          (= :two-different-temples type) (adjust-temples pid {(first choice) 1 (second choice) 1}))
      (update-in [:active :decisions] rest))))

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

;; TODO
(defn cost-payable?
  [pid cost]
  true)

(defn place-worker
  [state pid gear]
  (let [worker-option (get-in state [:active :worker-option])
        gear-slots (get-in state [:gears gear])
        max-position (- (get-in spec [:gears gear :teeth]) 2)
        turn (:turn state)
        position (first-nil (rotate-vec gear-slots turn))
        slot (gear-slot gear position turn)
        player (get-in state [:players pid])
        player-color (:color player)
        remaining-workers (:workers player)
        remaining-corn (get-in state [:players pid :materials :corn])
        placed (get-in state [:active :placed])
        corn-cost (+ position placed)]
    (if (and (> remaining-workers 0)
             (not (get-in state [:active :decision]))
             (>= remaining-corn corn-cost)
             (< position max-position)
             (or (= :place worker-option) (= :none worker-option)))
      (-> state
        (update-in [:players pid :workers] dec)
        (update-in [:gears gear] assoc slot player-color)
        (update-in [:players pid :materials :corn] - corn-cost)
        (update-in [:active :placed] inc)
        (update :active assoc :worker-option :place))
      state)))

(defn remove-worker
  [state pid gear slot]
  (let [turn (:turn state)
        worker-option (get-in state [:active :worker-option])
        position (gear-position gear slot turn)
        player (get-in state [:players pid])
        skulls (get-in player [:materials :skull])
        player-color (:color player)
        target-color (get-in state [:gears gear slot])
        action-position (- position 1)
        action (get-in spec [:gears gear :actions action-position])]
    (if (and (= player-color target-color)
             (empty? (get-in state [:active :decisions]))
             (or (= :remove worker-option) (= :none worker-option))
             (or (not= :chi gear) (> skulls 0))
             (cost-payable? pid (:cost action)))
      (-> state
        (update-in [:players pid :workers] inc)
        (update-in [:gears gear] assoc slot nil)
        (update :active assoc :worker-option :remove)
        (handle-action pid action))
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
