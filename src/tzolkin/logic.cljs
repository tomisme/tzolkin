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
  ([state pid changes]
   (player-map-adjustment state pid :materials changes))
  ([state pid changes source]
   (let [player (get-in state [:players pid])
         agri   (-> player :tech :agri)
         extr   (-> player :tech :extr)
         corn?  (contains? changes :corn)
         wood?  (contains? changes :wood)
         stone? (contains? changes :stone)
         gold?  (contains? changes :gold)]
     (cond-> (adjust-materials state pid changes)
       (and (>= agri 1) (= :pal source)   corn?)  (adjust-materials pid {:corn 1})
       (and (>= agri 2) (= :water source) corn?)  (adjust-materials pid {:corn 1})
       (and (>= agri 3) (= :pal source)   corn?)  (adjust-materials pid {:corn 2})
       (and (>= extr 1) (= :yax source)   wood?)  (adjust-materials pid {:wood 1})
       (and (>= extr 1) (= :pal source)   wood?)  (adjust-materials pid {:wood 1})
       (and (>= extr 2) (= :yax source)   stone?) (adjust-materials pid {:stone 1})
       (and (>= extr 3) (= :yax source)   gold?)  (adjust-materials pid {:gold 1})))))

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
                   :pay-discount resource-options
                   :gain-resource resource-options
                   :gain-materials data
                   :jungle-mats (:options data)
                   :build-monument (:monuments state)
                   :build-building (building-options state)
                   :tech tech-options)
         decision (cond-> {:type type :options options}
                    (= :jungle-mats type)  (conj [:jungle-id (:jungle-id data)])
                    (= :pay-discount type) (conj [:cost (:cost data)]))]
     (update-in state [:active :decisions] into (repeat num decision))))
  ([state pid type]
   (add-decision state pid type {})))

(defn check-for-bad-beg
  [state]
  (let [pid (-> state :active :pid)
        begging-decision? (= :beg? (:type (first (get-in state [:active :decisions]))))
        too-much-corn? (< 2 (get-in state [:players pid :materials :corn]))]
    (if (and begging-decision? too-much-corn?)
      (update-in state [:active :decisions] rest)
      state)))

(defn next-dec
  [state]
  (-> state
      (update-in [:active :decisions] rest)
      check-for-bad-beg))

;; ==========
;; =  Costs =
;; ==========

(defn is-resource?
  [material]
  (contains? #{:wood :stone :gold} material))

(defn count-resources
  [materials]
  (reduce
   (fn [count [k v]]
     (if (is-resource? k)
       (+ count v)
       count))
   0
   materials))

(defn cost-payable?
  ([state pid cost discount]
   (if (-> cost (contains? :any-resource))
     (boolean (some #(> (get-in state [:players pid :materials %]) 0)
                    (:resources spec)))
     (let [player-materials (get-in state [:players pid :materials])]
       (and
        (>= (count-resources player-materials) (dec (count-resources cost)))
        (every?
         (fn [[material held-amount]]
           (let [discounted-amount (if (and (= :resource discount)
                                            (is-resource? material))
                                     (inc held-amount)
                                     held-amount)]
             (>= discounted-amount (get cost material))))
         player-materials)))))
  ([state pid cost]
   (cost-payable? state pid cost nil)))

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

(defn gain-builder-building
  [state pid build]
  (case build
    :building (add-decision state pid :build-building)
    :monument (add-decision state pid :build-monument)))

(defn gain-tech-building
  [state pid tech]
  (cond-> state
    (= :any tech)     (add-decision pid :tech 1)
    (= :any-two tech) (add-decision pid :tech 2)
    (map? tech)       (adjust-tech pid tech)))

(defn gain-building
  [state pid building]
  (let [{:keys [tech temples materials trade build points
                gain-worker #_free-action-for-corn]} building]
    (cond-> (update-in state [:players pid :buildings] conj building)
      tech        (gain-tech-building pid tech)
      build       (gain-builder-building pid build)
      trade       (start-trading pid)
      points      (adjust-points pid points)
      temples     (adjust-temples pid temples)
      materials   (adjust-materials pid materials)
      gain-worker (adjust-workers pid 1))))

(defn build-building
  ([state pid building]
   (let [arch (get-in state [:players pid :tech :arch])
         cost (:cost building)]
     (cond-> (gain-building state pid building)
       (>= arch 1) (adjust-materials pid {:corn 1})
       (>= arch 2) (adjust-points 0 2)
       (<= arch 2) (pay-cost pid cost)
       (= arch 3)  (add-decision pid :pay-discount {:cost cost})))))

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

(defn setup-jungle
  [state]
  (let [num (count (:players state))]
    (assoc state :jungle [{:corn-tiles num}
                          {:corn-tiles num :wood-tiles num}
                          {:corn-tiles num :wood-tiles num}
                          {:corn-tiles num :wood-tiles num}])))

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

(defn handle-jungle-action
  [state pid v]
  (let [player (get-in state [:players :pid])
        agri (-> player :tech :agri)
        extr (-> player :tech :extr)
        corn (:corn v)
        wood (:wood v)
        id (:jungle-id v)
        corn-tiles (get-in state [:jungle id :corn-tiles])
        wood-tiles (get-in state [:jungle id :wood-tiles])
        corn-tile? (> corn-tiles 0)
        wood-tile? (> wood-tiles 0)
        options (cond-> []
                  (or corn-tile? (>= agri 2)) (conj {:corn corn})
                  wood-tile? (conj {:wood wood}))]
    (if (= 1 (count options))
      (if corn-tile?
        (-> (adjust-materials state pid {:corn corn} :pal)
            (update-in [:jungle id :corn-tiles] dec))
        (if (>= agri 2)
          (adjust-materials state pid {:corn corn} :pal)
          state))
      (if (or wood-tile? (>= agri 2))
        (add-decision state pid :jungle-mats {:options options :jungle-id id})
        state))))

(defn handle-action
  [state pid [k v]]
  (let [build-building?     (and (= :build k) (= :single (:type v)))
        choose-a-temple?    (and (= :temples k) (= :any (:choose v)))
        choose-two-temples? (and (= :temples k) (= :two-different (:choose v)))]
    (-> (cond-> state
          (= :skull-action k)   (handle-skull-action pid v)
          (= :gain-worker k)    (adjust-workers pid 1)
          (= :gain-materials k) (adjust-materials pid (:mats v) (:source v))
          (= :choose-action k)  (add-decision pid :action v)
          (= :tech k)           (add-decision pid :tech (:steps v))
          (= :jungle-mats k)    (handle-jungle-action pid v)

          build-building?       (add-decision pid :build-building)
          choose-a-temple?      (add-decision pid :temple v)
          choose-two-temples?   (add-decision pid :two-diff-temples v)
          (= :trade k)          (start-trading pid)

          (:cost v)             (pay-cost pid (:cost v))))))

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
        choice   (get options index)]
    (case type
      :beg?
        (if choice (beg-for-corn (next-dec state)) (next-dec state))

      :starters
        (if (= (:num-starters spec) (count options))
          (-> (next-dec state)
              (gain-starter pid choice)
              (add-decision pid :starters (remove-from-vec options index)))
          (-> (next-dec state)
              (gain-starter pid choice)))

      :gain-materials
        (-> (next-dec state) (adjust-materials pid choice))

      :gain-resource
        (-> (next-dec state) (adjust-materials pid choice))

      :jungle-mats
        (let [agri (get-in state [:players pid :tech :agri])
              id (:jungle-id decision)
              corn-tiles (get-in state [:jungle id :corn-tiles])
              wood-tiles (get-in state [:jungle id :wood-tiles])
              corn-tile? (and (> corn-tiles 0) (< wood-tiles corn-tiles))
              wood-tile? (> wood-tiles 0)]
          (if (and (not corn-tile?) (not wood-tile?) (< agri 2))
            (update state :errors conj "There are no jungle tiles left")
            (if (contains? choice :corn)
              (if corn-tile?
                (-> (next-dec state)
                    (adjust-materials pid choice :pal)
                    (update-in [:jungle id :corn-tiles] dec))
                (if (>= agri 2)
                  (-> (next-dec state)
                      (adjust-materials pid choice :pal))
                  (if wood-tile?
                    (-> (next-dec state)
                        (adjust-materials pid choice :pal)
                        (update-in [:jungle id :corn-tiles] dec)
                        (update-in [:jungle id :wood-tiles] dec)
                        (add-decision pid :anger-god))
                    (update state :errors conj "There are no corn tiles left"))))
              (if wood-tile?
                (-> (next-dec state)
                    (adjust-materials pid choice :pal)
                    (update-in [:jungle id :wood-tiles] dec))
                (update state :errors conj "There are no wood tiles left")))))

      :pay-resource
        (if (cost-payable? state pid choice)
          (-> (next-dec state)
              (adjust-materials pid (negatise-map choice)))
          (update state :errors conj (str "Can't pay resource cost: " choice)))

      :pay-discount
        (let [cost (:cost decision)
              choice-key (first (keys choice))
              discounted-cost (if (contains? cost choice-key)
                                (change-map cost - {choice-key 1})
                                cost)]
          (if (cost-payable? state pid discounted-cost)
            (-> (next-dec state)
                (adjust-materials pid (negatise-map discounted-cost)))
            (update state :errors conj (str "Can't afford cost: " discounted-cost))))

      :build-building
        (let [arch (get-in state [:players pid :tech :arch])
              payable? (if (= arch 3)
                         (cost-payable? state pid (:cost choice) :resource)
                         (cost-payable? state pid (:cost choice)))]
          (if payable?
            (-> (next-dec state)
                (build-building pid choice)
                (update :buildings remove-from-vec index))
            (update state :errors conj (str "Can't afford building: " choice))))

      :tech
        (-> (next-dec state) (adjust-tech pid choice))

      :temple
        (-> (next-dec state) (adjust-temples pid choice))

      :anger-god
        (let [temple (first (keys choice))]
          (if (= 0 (get-in state [:players pid :temples temple]))
            (update state :errors conj (str "Can't anger " temple))
            (-> (next-dec state)
                (adjust-temples pid (negatise-map choice)))))

      :two-diff-temples
        (-> (next-dec state) (adjust-temples pid choice)))))

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
             (empty? (get-in state [:active :decisions]))
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
         setup-buildings-monuments
         setup-jungle))))

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
