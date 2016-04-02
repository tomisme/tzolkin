(ns tzolkin.logic
  (:require
    [tzolkin.spec :refer [spec]]
    [tzolkin.utils :refer [log indexed first-val rotate-vec
                           remove-from-vec change-map negatise-map]]))

;; ============
;; =  Helpers =
;; ============

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

(defn player-map-adjustment
  [state pid k changes]
  (let [current (get-in state [:players pid k])
        updated (change-map current + changes)]
    (assoc-in state [:players pid k] updated)))

(defn adjust-points
  [state pid num]
  (update-in state [:players pid :points] + num))

(defn adjust-workers
  [state pid num]
  (update-in state [:players pid :workers] + num))

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
;; =  Decisions =
;; ==============

(def resource-options
  (into [] (for [k (:resources spec)] {k 1})))

(def tech-options
  (into [] (for [k (keys (:tech spec))] {k 1})))

(def temple-options
  (into [] (for [k (keys (:temples spec))] {k 1})))

(def two-different-temple-options
  (into [] (for [t1 (keys (:temples spec)) t2 (keys (:temples spec))
                 :when (not= t1 t2)]
             {t1 1 t2 1})))

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
  ([state pid type data]
   (let [pid (-> state :active :pid)
         num (if (= :tech type) data 1)
         options (case type
                   :anger-god temple-options
                   :beg? '(true false)
                   :starters data
                   :action (action-options (:gear data))
                   :temple temple-options
                   :two-diff-temples two-different-temple-options
                   :pay-resource resource-options
                   :gain-resource resource-options
                   :gain-materials data
                   :build-monument (:monuments state)
                   :build-building (building-options state)
                   :tech tech-options)]
     (update-in state [:active :decisions] into (repeat num {:type type
                                                             :options options}))))
  ([state pid type]
   (add-decision state pid type {})))

;; ==========
;; =  Costs =
;; ==========

(defn cost-payable?
  [state pid cost]
  (if (-> cost (contains? :any-resource))
    (boolean (some #(> (get-in state [:players pid :materials %]) 0)
                   (:resources spec)))
    (every?
     (fn [[resource held-amount]]
       (>= held-amount (get cost resource)))
     (get-in state [:players pid :materials]))))

(defn pay-cost
  [state pid cost]
  (if (:any-resource cost)
    (add-decision state pid :pay-resource {})
    (adjust-materials state pid (negatise-map cost))))

;; ============
;; =  Trading =
;; ============

(defn start-trading
  [state pid]
  (update state :active assoc :trading? true))

;; ==============
;; =  Buildings =
;; ==============

(defn build-builder-building
  [state pid build]
  (case build
    :building (add-decision state pid :build-building)
    :monument (add-decision state pid :build-monument)))

(defn build-tech-building
  [state pid tech]
  (cond-> state
    (= :any tech)     (add-decision pid :tech 1)
    (= :any-two tech) (add-decision pid :tech 2)
    (map? tech)       (adjust-tech pid tech)))

(defn gain-building
  [state pid building]
  (let [{:keys [cost tech temples materials trade build points
                gain-worker #_free-action-for-corn]} building]
    (-> (cond-> state
          build       (build-builder-building pid build)
          trade       (start-trading pid)
          points      (adjust-points pid points)
          temples     (adjust-temples pid temples)
          materials   (adjust-materials pid materials)
          gain-worker (adjust-workers pid 1)
          tech        (build-tech-building pid tech))
        (update-in [:players pid :buildings] conj building)
        (pay-cost pid cost))))

;; ============
;; =  Actions =
;; ============

(defn handle-skull-action
  [state pid {:keys [resource points temple]}]
  (-> (cond-> state
        resource (add-decision pid :gain-resource)
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
          (= :choose-action k)  (add-decision pid :action v)
          (= :tech k)           (add-decision pid :tech (:steps v))
          (= :choose-mats k)    (add-decision pid :gain-materials v)
          build-building?       (add-decision pid :build-building)
          choose-a-temple?      (add-decision pid :temple v)
          choose-two-temples?   (add-decision pid :two-diff-temples v)
          (= :trade k)          (start-trading pid)

          (:cost v)             (pay-cost pid (:cost v))))))

;; ==============
;; = Game Start =
;; ==============

(def initial-gears-state
  (into {} (for [k (keys (:gears spec))]
             [k (into [] (repeat (get-in spec [:gears k :teeth]) :none))])))

(defn setup-buildings-monuments
  [state]
  (-> state
      (assoc :buildings (vec (filter #(= 1 (:age %)) (shuffle (:buildings spec)))))
      (assoc :monuments (vec (take (+ 2 (count (:players state)))
                                   (shuffle (:monuments spec)))))))

(defn choose-starter-tiles
  [state pid]
  (let [tiles #(vec (take (:num-starters spec)
                          (shuffle (:starters spec))))]
    (add-decision state pid :starters (tiles))))

(defn gain-starter
  [state pid {:keys [materials tech #_farm temple gain-worker]}]
  (cond-> state
    temple      (adjust-temples pid {temple 1})
    tech        (adjust-tech pid {tech 1})
    materials   (adjust-materials pid materials)
    gain-worker (adjust-workers pid 1)))

;; ========================
;; =  Food Day / Game End =
;; ========================

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

(defn fd-points
  "Takes a player, returns the number of points earned for temple positons"
  [player]
  (apply +
         (map
          (fn [[temple step]]
            (get-in spec [:temples temple :steps step :points]))
          (:temples player))))

(defn fd-mats
  "Takes a player, returns a map of materials earned for temple postions"
  [player]
  (apply merge
    (for [[temple step] (:temples player)]
      (frequencies
        (reduce
         (fn [materials step]
           (if-let [material (:material step)]
             (conj materials material)
             materials))
         '()
         (take (inc step) (get-in spec [:temples temple :steps])))))))

;; TODO points for highest positioms, farms and lost vp for starving workers
(defn food-day
  [state]
  (let [turn-details (get-in spec [:turns (dec (:turn state))])
        turn-type (:type turn-details)
        corn-cost (fn [p] (* 2 (:workers p)))
        mats? (= :mats-food-day turn-type)
        points? (= :points-food-day turn-type)]
    (update state
            :players
            (fn [players]
              (mapv
                (fn [p]
                  (cond-> (update-in p [:materials :corn] - (corn-cost p))
                    mats? (update :materials change-map + (fd-mats p))
                    points? (update :points + (fd-points p))))
                players)))))

(defn finish-game
  [state]
  (do (.alert js/window "Game Over!") state))

;; ================
;; = Beg for Corn =
;; ================

(defn possibly-beg-for-corn
  [state]
  (let [pid (-> state :active :pid)
        corn (-> state :players (nth pid) :materials :corn)]
    (if (< corn 3)
      (add-decision state pid :beg?)
      state)))

(defn beg-for-corn
  [state]
  (let [pid (-> state :active :pid)
        corn (-> state :players (nth pid) :materials :corn)]
    (-> state
        (adjust-materials pid {:corn (- 3 corn)})
        (add-decision pid :anger-god))))

;; ==================
;; = Event Handlers =
;; ==================

(defn handle-decision
  "Decisions are handled last in first out"
  [{:keys [active] :as state} index]
  (let [pid      (:pid active)
        decision (first (:decisions active))
        type     (:type decision)
        options  (:options decision)
        choice   (get options index)
        pop-dec  #(update-in % [:active :decisions] rest)]
    (case type
      :beg?             (if choice (beg-for-corn (pop-dec state)) (pop-dec state))
      :starters         (if (= (:num-starters spec) (count options))
                          (-> (gain-starter state pid choice)
                              pop-dec
                              (add-decision pid :starters (remove-from-vec options index)))
                          (-> (gain-starter state pid choice)
                              pop-dec))
      :gain-materials   (-> (adjust-materials state pid choice)
                            pop-dec)
      :gain-resource    (-> (adjust-materials state pid choice)
                            pop-dec)
      :pay-resource     (if (cost-payable? state pid choice)
                          (-> (adjust-materials state pid (negatise-map choice))
                              pop-dec)
                          (update state :errors conj (str "Can't pay resource cost: " choice)))
      :build-building   (if (cost-payable? state pid (:cost choice))
                          (-> (gain-building state pid choice)
                              (update :buildings remove-from-vec index)
                              pop-dec)
                          (update state :errors conj (str "Can't buy building: " choice)))
      :tech             (-> (adjust-tech state pid choice)
                            pop-dec)
      :temple           (-> (adjust-temples state pid choice)
                            pop-dec)
      :anger-god        (let [temple (first (keys choice))]
                          (if (= 0 (get-in state [:players pid :temples temple]))
                            (update state :errors conj (str "Can't anger " temple))
                            (-> (adjust-temples state pid (negatise-map choice))
                              pop-dec)))
      :two-diff-temples (-> (adjust-temples state pid choice)
                            pop-dec))))

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
        action (get-in spec [:gears gear :actions action-position])
        [action-type action-data] action]
    (if (and ;; (log action)
             ;; Not implemented yet! ===================
             (not= :choose-action action-type)
             (not (and (= :build action-type) (contains? #{:with-corn
                                                           :double-or-monument}
                                                         (:type action-data))))
             ;; ========================================
             (= player-color target-color)
             (empty? (get-in state [:active :decisions]))
             (or (= :remove worker-option) (= :none worker-option))
             (or (not= :chi gear) (> skulls 0))
             (cost-payable? state pid (:cost action-data)))
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
        test? (:test? state)
        pid (-> state :active :pid)
        last-player? (= (dec (count (:players state))) pid)
        turn-details (get-in spec [:turns (dec turn)])
        turn-type (:type turn-details)
        food-day? (contains? #{:mats-food-day :points-food-day} turn-type)
        decision (get-in state [:active :decisions])]
    (if (not (empty? decision))
      (update state :errors conj (str "Can't end turn - There's still a decision to be made: " decision))
      (if (and last-player? (= turn max-turn))
        (finish-game state)
        (if (and (> turn 0)
                 (<= turn max-turn))
          (-> (cond-> state
                      (and last-player? food-day?) food-day
                      last-player? (update :turn inc)
                      last-player? (update :active assoc :pid 0)
                      (not last-player?) (update-in [:active :pid] inc)
                      (not test?) (possibly-beg-for-corn)
                      (and (= turn 1) (not test?) (not last-player?)) (choose-starter-tiles pid))
              (update :active assoc :placed 0)
              (update :active assoc :worker-option :none))
          (update state :errors conj "Can't end turn"))))))

(defn init-game
  [state]
  (conj state {:turn 0
               :active {:pid 0 :worker-option :none :placed 0 :decisions '() :trading? false}
               :remaining-skulls (:skulls spec)
               :players []
               :gears initial-gears-state}))

(defn start-game
  ([state test?]
   (if (> (:turn state) 0)
     (-> state
         (update :errors conj "Can't start game - game has already started"))
     (-> (cond-> state
           (not test?) (possibly-beg-for-corn)
           (not test?) (choose-starter-tiles 0)
           test? (assoc :test? true))
         (update :turn inc)
         setup-buildings-monuments))))

(defn add-player
  [state name color]
  (update state :players conj (-> (:player-starting-stuff spec)
                                  (assoc :name name)
                                  (assoc :color color))))

(defn make-trade
  [state [type resource]]
  (let [pid (-> state :active :pid)
        price (get-in spec [:trade-values resource])
        corn-amount (case type
                      :buy (* -1 price)
                      :sell price)]
    (-> state
        (adjust-materials pid {resource (case type :buy 1 :sell -1)})
        (adjust-materials pid {:corn corn-amount}))))

(defn stop-trading
  [state]
  (update state :active assoc :trading? false))

(defn handle-event
  [state [e data]]
  (when (:errors state) (log (:errors state)))
  (let [started? (> (:turn state) 0)]
   (cond-> state
     (and (= :new-game e)   (not started?)) init-game
     (and (= :start-game e) (not started?)) (start-game (:test? data))
     (and (= :add-player e) (not started?)) (add-player (:name data) (:color data))
     (and (= :make-trade e)       started?) (make-trade (:trade data))
     (and (= :stop-trading e)     started?) stop-trading
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

(defn gen-es
  [events]
  (:stream
   (reduce
    (fn [prev event]
      (let [state (handle-event (:state prev) event)]
        {:stream (conj (:stream prev) [event state])
         :state state}))
    {:stream []
     :state {}}
    events)))
