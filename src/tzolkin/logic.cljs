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
      (add-decision state pid :pay-resource {})
      (adjust-materials state pid (negatise-map cost)))))

(defn handle-skull-action
  [state pid {:keys [resource points temple]}]
  (-> (cond-> state
              resource (choose-resource)
              points   (adjust-points pid points)
              temple   (adjust-temples pid {temple 1}))
    (update-in [:players pid :materials :skull] dec)))

(defn handle-action
  [state pid [k v]]
  (-> (cond-> state
              (= :skull-action k) (handle-skull-action pid v)
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
  [{:keys [active] :as state} option-index]
  (let [pid       (:pid active)
        decision  (first (:decisions active))
        type      (:type decision)
        options   (:options decision)
        choice    (get options option-index)]
    (-> (cond-> state
                ;;TODO :action
                (= :gain-materials type) (adjust-materials pid choice)
                (= :gain-building type) (-> (gain-building pid choice)
                                            (update :buildings remove-from-vec option-index))
                ;; TODO merge :tech and :tech-two
                (= :tech type) (adjust-tech pid choice)
                (= :tech-two type) (adjust-tech pid choice)
                (= :temple type) (adjust-temples pid choice)
                (= :two-different-temples type) (adjust-temples pid {(first choice) 1 (second choice) 1}))
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
  (cond-> state
          (= :new-game e)      init-game
          (= :end-turn e)      end-turn
          (= :start-game e)    start-game
          (= :add-player e)    (add-player (:name data) (:color data))
          (= :place-worker e)  (place-worker (:gear data))
          (= :remove-worker e) (remove-worker (:gear data) (:slot data))
          (= :choose-option e) (handle-decision (:index data))
          ;; debugging events
          (= :give-stuff e) (player-map-adjustment (:pid data) (:k data) (:changes data))))

;; ================
;; = Event Stream =
;; ================

(defn add-event
  [es event]
  (let [[_ prev-state] (last es)
        new-state (handle-event prev-state event)
        errors (:errors new-state)]
    (if errors
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
