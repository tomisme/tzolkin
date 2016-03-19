(ns tzolkin.logic
  (:require
    [tzolkin.spec :refer [spec]]
    [tzolkin.utils :refer [log indexed first-val rotate-vec
                           remove-from-vec change-map negatise-map]]))

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

(defn adjust-temples
  [state pid changes]
  (player-map-adjustment state pid :temples changes))

(defn adjust-materials
  [state pid changes]
  (player-map-adjustment state pid :materials changes))

(defn adjust-tech
  [state pid changes]
  (player-map-adjustment state pid :tech changes))

(def resource-options
  (into [] (for [k (:resources spec)] {k 1})))

(def tech-options
  (into [] (for [k (keys (:tech spec))] {k 1})))

(def temple-options
  (into [] (for [k (keys (:temples spec))] {k 1})))

(def two-different-temple-options
  (into [] (for [t1 (keys (:temples spec)) t2 (keys (:temples spec))
                 :when (not= t1 t2)]
             [t1 t2])))

(defn action-options
  [gear]
  (if (= :non-chi gear)
    (into [] (for [g '(:yax :tik :uxe :pal) x (range 5)] {g x}))
    (let [num (get-in spec [:gears gear :regular-actions])]
      (into [] (for [x (range num)] {gear x})))))

(defn building-options
  [state]
  (vec (take (:num-available-buildings spec) (:buildings state))))

(defn add-decision
  ([state type data]
   (let [pid (-> state :active :pid)
         num (if (= :tech type) data 1)
         options (case type
                   :action (action-options (:gear data))
                   :temple temple-options
                   :two-different-temples two-different-temple-options
                   :pay-resource resource-options
                   :gain-resource resource-options
                   :gain-materials data
                   :build-monument (:monuments state)
                   :build-building (building-options state)
                   :tech tech-options)]
     (update-in state [:active :decisions] into (repeat num {:type type
                                                             :options options}))))
  ([state type]
   (add-decision state type {})))

(defn build-builder-building
  [state pid build]
  (case build
    :building (add-decision state :build-building)
    :monument (add-decision state :build-monument)))

(defn build-tech-building
  [state pid tech]
  (cond-> state
    (= :any tech)     (add-decision :tech 1)
    (= :any-two tech) (add-decision :tech 2)
    (map? tech)       (adjust-tech pid tech)))

(defn gain-building
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

(defn pay-action-cost
  [state pid [action-type action-data]]
  (let [cost (:cost action-data)
        any-resource-cost (:any-resource cost)]
    (if any-resource-cost
      (add-decision state :pay-resource {})
      (adjust-materials state pid (negatise-map cost)))))

(defn handle-skull-action
  [state pid {:keys [resource points temple]}]
  (-> (cond-> state
        resource (add-decision :gain-resource)
        points   (adjust-points pid points)
        temple   (adjust-temples pid {temple 1}))
    (update-in [:players pid :materials :skull] dec)))

(defn handle-action
  [state pid [k v]]
  (let [build-building?     (and (= :build k) (= :single (:type v)))
        choose-a-temple?    (and (= :temples k) (= :any (:choose v)))
        choose-two-temples? (and (= :temples k) (= :two-different (:choose v)))]
    (-> (cond-> state
          (= :skull-action k)   (handle-skull-action pid v)
          (= :gain-worker k)    (adjust-workers pid 1)
          (= :gain-materials k) (adjust-materials pid v)
          (= :choose-action k)  (add-decision :action v)
          (= :tech k)           (add-decision :tech (:steps v))
          (= :choose-mats k)    (add-decision :gain-materials v)
          build-building?       (add-decision :build-building)
          choose-a-temple?      (add-decision :temple v)
          choose-two-temples?   (add-decision :two-different-temples v))
        (pay-action-cost pid [k v]))))

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

(def initial-gears-state
  (into {} (for [k (keys (:gears spec))]
             [k (into [] (repeat (get-in spec [:gears k :teeth]) :none))])))

(defn setup-buildings-monuments
  [state]
  (-> state
      (assoc :buildings (vec (filter #(= 1 (:age %)) (shuffle (:buildings spec)))))
      (assoc :monuments (vec (take (+ 2 (count (:players state)))
                                   (shuffle (:monuments spec)))))))

;; TODO don't give them the tiles... just add a new decision
(defn give-starter-tiles
  [state]
  (update state
          :players
          #(vec (for [p %]
                  (assoc p :starters (vec (take (:num-starters spec)
                                                (shuffle (:starters spec)))))))))

(defn apply-temple-rewards
  [state type age]
  {:pre [(contains? #{:points :materials} type)
         (contains? #{1 2} age)]}
  (update state
          :players
          (reduce
           (fn [blob p]
             ())
           {})))

          ; #(vec (for [p %]
          ;         (let [step (log (get-in p [:temples :chac]))
          ;               points (log (get-in spec [:temples :chac :steps step :points]))]
          ;           (update p :points + points))))))

#_(def pl [{:points 5
            :temples {:chac 0, :quet 1, :kuku 1}}
           {:points 5
            :temples {:chac 1, :quet 1, :kuku 1}}])

#_(def s {:players pl})

#_(log (apply-temple-rewards s :points 2))

#_(reduce
   (fn [m p]
     (let [player-step (get-in p [:temples :chac])
           highest-step (get-in m [:winner :chac :step])
           winner? (or (not highest-step) (> player-step highest-step))]
       (update :players conj (cond-> p))))
   {:winner {:chac {:player nil :step nil}
             :quet {:player nil :step nil}
             :kuku {:player nil :step nil}}
    :players []}
   pl)

(defn fd-materials-earned
  "Returns the materials earned by a player for ending 1st/3rd food day at
   position 'pos' on 'temple'"
  [temple pos]
  (frequencies
    (reduce
     (fn [materials step]
       (if-let [material (:material step)]
         (conj materials material)
         materials))
     []
     (take (inc pos) (get-in spec [:temples temple :steps])))))

(defn finish-game
  [state]
  (.alert js/window "Game Over!"))

;; ==================
;; = Event Handlers =
;; ==================

(defn handle-decision
  [{:keys [active] :as state} index]
  (let [pid       (:pid active)
        decision  (first (:decisions active))
        type      (:type decision)
        options   (:options decision)
        choice    (get options index)]
    (-> (cond-> state
          ;;TODO :action
          (= :gain-materials type) (adjust-materials pid choice)
          (= :gain-resource type) (adjust-materials pid choice)
          (= :pay-resource type) (adjust-materials pid (negatise-map choice))
          (= :build-building type) (-> (gain-building pid choice)
                                       (update :buildings remove-from-vec index))
          (= :tech type) (adjust-tech pid choice)
          (= :temple type) (adjust-temples pid choice)
          (= :two-different-temples type) (adjust-temples pid
                                                          {(first choice) 1
                                                           (second choice) 1}))
        (update-in [:active :decisions] rest))))

(defn place-worker
  [state gear]
  (let [pid (-> state :active :pid)
        worker-option (get-in state [:active :worker-option])
        gear-slots (get-in state [:gears gear])
        max-position (- (get-in spec [:gears gear :teeth]) 2)
        turn (:turn state)
        position (first-val (rotate-vec gear-slots turn) :none)
        slot (gear-slot gear position turn)
        player (get-in state [:players pid])
        player-color (:color player)
        remaining-workers (:workers player)
        remaining-corn (-> player :materials :corn)
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
      (update state :errors conj "Can't place worker"))))

(defn remove-worker
  [state gear slot]
  (let [pid (-> state :active :pid)
        turn (:turn state)
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
          (update-in [:gears gear] assoc slot :none)
          (update :active assoc :worker-option :remove)
          (handle-action pid action))
      (update state :errors conj "Can't remove worker"))))

(defn end-turn
  [state]
  (let [turn (:turn state)
        max-turn (:total-turns spec)
        pid (-> state :active :pid)
        last-player? (= (dec (count (:players state))) pid)]
    (if (and last-player? (= turn max-turn))
      (finish-game state)
      (if (and (> turn 0)
               (<= turn max-turn))
        (-> (cond-> state
                    last-player? (update :turn inc)
                    last-player? (update :active assoc :pid 0)
                    (not last-player?) (update-in [:active :pid] inc))
            (update :active assoc :placed 0)
            (update :active assoc :worker-option :none))
        (update state :errors conj "Can't end turn")))))

(defn init-game
  [state]
  (conj state {:turn 0
               :active {:pid 0 :worker-option :none :placed 0 :decisions '()}
               :remaining-skulls (:skulls spec)
               :players []
               :gears initial-gears-state}))

(defn start-game
  [state]
  (if (> (:turn state) 0)
    (-> state
        (update :errors conj "Can't start game - game has already started"))
    (-> state
        (update :turn inc)
        give-starter-tiles
        setup-buildings-monuments)))

(defn add-player
  [state name color]
  (update state :players conj (-> (:player-starting-stuff spec)
                                  (assoc :name name)
                                  (assoc :color color))))

(defn handle-event
  [state [e data]]
  (when (:errors state) (log (:errors state)))
  (let [started? (> (:turn state) 0)]
   (cond-> state
     (and (= :new-game e)   (not started?)) init-game
     (and (= :start-game e) (not started?)) start-game
     (and (= :add-player e) (not started?)) (add-player (:name data) (:color data))
     (and (= :place-worker e)     started?) (place-worker (:gear data))
     (and (= :remove-worker e)    started?) (remove-worker (:gear data) (:slot data))
     (and (= :choose-option e)    started?) (handle-decision (:index data))
     (and (= :end-turn e)         started?) end-turn
     ;; dev events
     (= :give-stuff e) (player-map-adjustment (:pid data) (:k data) (:changes data)))))

;; ================
;; = Event Stream =
;; ================

(defn add-event
  [es event]
  (let [[_ prev-state] (last es)
        new-state (handle-event prev-state event)
        errors (:errors new-state)]
    (if (or errors (= prev-state new-state))
      (do (log errors) es)
      (conj es [event new-state]))))

(defn current-state
  [es]
  (let [[_ state] (last es)]
    state))

(defn reset-es
  [es index]
  (let [pos (inc index)]
    (if (< pos (count es))
      (vec (take pos es))
      es)))

(defn reduce-events
  [prev-state events]
  (reduce handle-event prev-state events))

(defn reduce-event-stream
  [initial-state events]
  (:stream
   (reduce
    (fn [prev event]
      (let [state (handle-event (:state prev) event)]
        {:stream (conj (:stream prev) [event state])
         :state state}))
    {:stream []
     :state initial-state}
    events)))
