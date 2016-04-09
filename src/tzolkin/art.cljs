(ns tzolkin.art
  (:require
   [reagent.core :as rg]
   [tzolkin.spec :refer [spec]]
   [tzolkin.utils :refer [log]]))

(def color-strings
  {:red "#CC333F"
   :blue "#69D2E7"
   :orange "#EB6841"
   :yellow "#EDC951"
   :water "#73BDF8"
   :yax "#E2DF9A"
   :tik "#F23A65"
   :uxe "#EBE54D"
   :chi "#9061C2"
   :pal "#7FAF1B"})

(def symbols
  {:resource "🎁"
   :worker "👤"
   :wood "🌲"
   :stone "🗿"
   :gold "🌕"
   :corn "🌽"
   :skull "💀"
   :chac "🐎"
   :quet "🐢"
   :kuku "🐒"
   :yax "🍈"
   :tik "🍓"
   :uxe "🍋"
   :chi "🍇"
   :pal "🍏"
   :agri "agri"
   :extr "extr"
   :arch "arch"
   :theo "theo"
   :choose-prev "⏪"})

(defn e->val
  [event]
  (-> event .-target .-value))

(defn transform-str
  "Takes any number of (supported) svg transform definitions
  and returns a string for use as an svg element's `transform` attribute"
  [& args]
  (apply str
    (for [[type data] args]
      (case type
        :rotate (str "rotate("
                     (:deg data)
                     (if (and (:x data) (:y data))
                       (str " " (:x data) " " (:y data)))
                     ")")))))

(defn symbols-str
  "Takes a map of materials and the amount of each and returns a string of
  symbols. Amounts larger than two represented by a number and a single symbol."
  [materials]
  (apply str (for [[material amount] materials]
               (if (< amount 3)
                 (apply str (repeat amount (get symbols material)))
                 (str amount (get symbols material))))))

(defn map-str
  [m]
  (apply str (for [[symbol amount] m]
               (apply str (repeat amount (get symbols symbol))))))

(defn temples-str
  [temples]
  (apply str (for [[temple amount] temples]
               (apply str (repeat amount (get symbols temple))))))

(defn tech-str
  [tech]
  (case tech
        :any "any tech"
        :any-two "2x any tech"
        (symbols-str tech)))

(defn food-day-str
  [until-food-day]
  [:span (if (= 0 until-food-day)
           [:b "it's Food Day!"]
           (str until-food-day " spins until Food Day!"))])

(defn points-el
  [points]
  [:div.ui.label {:style {:padding ".3rem .51rem"}}
   points])

(defn farm-el
  [farms]
  (if (= :all farms)
    [:p "(🌽✔)"]
    (map-indexed (fn [index _] ^{:key index} [:p "(🌽🌽✔)"]) (range farms))))

;; TODO share rendering with action-label
(defn building-card
  [building on-click choosing?]
  (let [{:keys [cost color materials points tech farm temples gain-worker
                free-action-for-corn build]} building]
    [:div {:class (str (name color) " ui card")
           :style {:width "7rem"
                   :margin "0.3rem"
                   :font-size "0.9rem"}}
      [:div.content {:style {:height "2rem"
                             :padding-top "0.5rem"
                             :z-index 1}}
        [:div {:class (str "ui " (name color) " corner label")
               :style {:z-index -1}}]
        (if choosing?
          [:a {:on-click on-click}
            (symbols-str cost)]
          (symbols-str cost))]
      [:div.center.aligned.content {:style {:height "6.1rem"}}
        [:div.description
          (when farm (farm-el farm))
          (when tech [:p (tech-str tech)])
          (when gain-worker [:p (:worker symbols)])
          (when free-action-for-corn [:p (get symbols :corn) ": action"])
          (when build [:p (name build)])
          (when temples [:p (temples-str temples)])
          (when materials [:p (symbols-str materials)])
          (when points (points-el points))]]]))

(defn available-buildings
  [buildings on-decision choosing?]
  [:div.ui.segment
    [:div.ui.top.left.attached.label "Available Buildings"]
    (into [:div.ui.cards]
      (map-indexed
        (fn [index building]
          [:div.item (building-card building #(on-decision index) choosing?)])
        (take (:num-available-buildings spec) buildings)))])

(defn starter-card
  [{:keys [materials tech farm temple gain-worker]} on-select]
  [:div.ui.button.card {:style {:width "7rem"
                                :margin "0.3rem"
                                :font-size "0.9rem"
                                :font-weight "inherit"}
                        :on-click on-select}
   [:div.center.aligned.content
    [:div.description
      (when materials [:p (symbols-str materials)])
      (when tech [:p (get symbols tech)])
      (when farm (farm-el farm))
      (when gain-worker [:p (:worker symbols)])
      (when temple [:p (get symbols temple)])]]])

(defn decisions-el
  [active active-player on-decision]
  (let [decision (first (:decisions active))
        type (:type decision)
        decision-options (:options decision)
        color-str (if (:color active-player)
                    (name (:color active-player))
                    "teal")
        msg (str (:name active-player)
                 " needs to choose "
                 (case type
                   :anger-god "which god to anger."
                   :beg? "whether to beg for corn."
                   :starters "which starter tile to take."
                   :action "which action to take."
                   :gain-materials "which materials to gain."
                   :gain-resource "which resource to gain."
                   :pay-resource "which resource to pay."
                   :build-building "which building to build."
                   :build-monument "which monument to build."
                   :tech "which tech track to increase."
                   :temple "which temples to move up on."
                   :two-diff-temples "which two temples to move up on."))]
      [:div
       [:div {:class (str "ui inverted segment " color-str)}
        msg]
       (into [:div {:class (when (= :starters type) "ui cards")}
              [:div.basic.ui.segment
               [:i.large.chevron.right.icon]]]
             (map-indexed
               (fn [index option]
                 (if (= :starters type)
                   [:div (starter-card option #(on-decision index decision))]
                   [:button.ui.button {:on-click #(on-decision index decision)}
                     (case type
                       :anger-god (symbols-str option)
                       :beg? (if option "Beg for corn" "Don't beg")
                       :action (str option)
                       :gain-materials (symbols-str option)
                       :gain-resource (symbols-str option)
                       :pay-resource (symbols-str option)
                       :build-building index
                       :build-monument index
                       :tech (symbols-str option)
                       :temple (symbols-str option)
                       :two-diff-temples (symbols-str option))]))
               decision-options))]))

(defn active-player-status
  [active active-player]
  (let [worker-option (:worker-option active)
        name (:name active-player)
        placed (:placed active)]
    (str
     name
     (case worker-option
      :none " has not yet chosen to remove or place workers."
      :place (str " has placed " placed " worker(s).")
      :remove " is removing workers."
      "ERROR"))))

(defn player-buildings-el
  [buildings]
  [:div.ui.cards
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div.item (building-card building nil false)])
      buildings)])

(defn player-stats-el
  [pid player]
  (let [{:keys [color materials points workers buildings]} player
        player-name (:name player)]
    ^{:key pid}
    [:div.ui.segment
      [:p
        [:a {:class (str "ui " (name color) " ribbon label")}
          player-name]
        [:span (str workers (:worker symbols) " | ")]
        [:span (for [[k v] materials]
                 (str v (get symbols k) " | "))]
        [:span points " VP"]]
      (if (seq buildings)
        (player-buildings-el buildings)
        [:p "No buildings or monuments."])]))

(defn turn-status-el
  [current-turn]
  (into [:p] (map-indexed
                (fn [index turn]
                  (if (>= index current-turn)
                    (case (:type turn)
                      :normal [:i.circle.thin.icon]
                      :points-food-day [:i.add.circle.icon]
                      :mats-food-day [:i.remove.circle.icon])
                    [:i.circle.icon]))
                (:turns spec))))

;; TODO
(defn new-player-form-el
  [on-add-player]
  (let [on-submit-clicked #(on-add-player "Greg" :green)]
    [:div.ui.form
     [:div.inline.fields
      [:div.five.wide.field
       [:input {:type "text" :placeholder "Name.."}]]
      [:div.ui.submit.button {:on-click on-submit-clicked} "Add Player"]]]))

(defn trade-window-el
  [player on-trade on-stop-trading]
  (let [materials (:materials player)
        resources (filter (fn [[m _]]
                            (contains? (set (:resources spec)) m))
                          materials)
        corn (:corn materials)]
    [:div
     [:div.ui.center.aligned.compact.segment
      [:div.ui.top.attached.label "Trade"]
      (into [:div
             [:p {:style {:margin-bottom 5}}
              corn (:corn symbols)]]
        (map
         (fn [[k v]]
           [:div {:style {:padding 3}}
            [:button.ui.icon.button {:on-click #(on-trade [:buy k])}
             [:i.plus.icon]]
            [:span {:style {:padding 4}}
             v (get symbols k)
             " (" (get-in spec [:trade-values k]) (:corn symbols) "ea.)"]
            [:button.ui.icon.button {:on-click #(on-trade [:sell k])}
             [:i.minus.icon]]])
         resources))]
     [:button.ui.button {:on-click on-stop-trading}
      "Finish Trading"]]))

(defn active-player-command-bar-el
  [active active-player on-decision on-trade on-stop-trading color-str]
  (if (:trading? active)
    [:div
     [:i.large.chevron.right.icon]
     (trade-window-el active-player on-trade on-stop-trading)]
    (let [decision (first (:decisions active))]
      (if decision
        (decisions-el active active-player on-decision)
        [:div {:class (str "ui inverted segment " color-str)}
         (active-player-status active active-player)]))))

(defn status-bar-el
  [state on-decision on-trade on-stop-trading on-end-turn on-start-game on-add-player]
  (let [turn (:turn state)
        started? (> turn 0)
        num-players (count (:players state))
        until-food-day (get-in spec [:until-food-day turn])
        buildings (:buildings state)
        active (:active state)
        choosing-building? (= :build-building (get-in active [:decision :type]))
        active-pid (:pid active)
        active-player (get-in state [:players active-pid])
        color-str (if (:color active-player)
                    (name (:color active-player))
                    "teal")]
    [:div
     (if started?
       [:p
        [:button.ui.button {:on-click on-end-turn}
         "End Turn"]
        [:span {:style {:margin-left 15}}
         "Turn " turn "/" (count (:turns spec)) ", " (food-day-str until-food-day)]]
       [:p
        [:button.ui.button {:class (str "ui button "
                                        (when-not (> num-players 0) "disabled"))
                            :on-click on-start-game}
         "Start Game"]])
     (turn-status-el turn)
     [:div
      (if started?
        (active-player-command-bar-el active active-player on-decision on-trade on-stop-trading color-str)
        (new-player-form-el on-add-player))
      (map-indexed player-stats-el (:players state))
      (available-buildings buildings on-decision choosing-building?)]]))

(defn action-label
  [[k data]]
  (case k
    :gain-materials  (symbols-str data)
    :choose-mats (str (symbols-str (first data)) "/" (symbols-str (second data)))
    :choose-action (if (= (:gear data) :non-chi)
                     (str (:corn symbols) ": action")
                     (str (:choose-prev symbols) (get symbols (:gear data))))
    :choose-any-action "any"
    :tech (case (:steps data)
                1 "1x tech"
                2 "2x tech")
    :build (case (:type data)
                 :single "build"
                 :double-or-monument "bld 2/mon"
                 :with-corn (str "bld w/" (:corn symbols)))
    :temples (case (:choose data)
                   :any (str (symbols-str (:cost data)) ": tmpl")
                   :two-different (str (:resource symbols) ": tmpls"))
    :trade "trade"
    :gain-worker (:worker symbols)
    :skull-action (str (get symbols (:temple data))
                    (:points data) "p"
                    (when (:resource data) (:resource symbols)))
    "WHAT?"))

(defn corn-cost-labels
  [cx cy r teeth]
  (for [index (range 0 (- teeth 2))]
    (let [spacing (/ 360 teeth)
          deg (* index spacing)
          offset (/ spacing 2)
          text-x cx
          text-y (+ cy (* r 1.15))
          transform (transform-str [:rotate {:deg (+ deg offset)
                                             :x cx
                                             :y cy}]
                                   [:rotate {:deg 180
                                             :x text-x
                                             :y text-y}])]
      ^{:key (str "corn-cost" index)}
      [:text {:x text-x
              :y text-y
              :style {:stroke "white"
                      :stroke-width 4
                      :paint-order "stroke"
                      :fill "black"}
              :font-size 14
              :text-anchor "middle"
              :transform transform}
       (str index "🌽")])))

(defn action-labels
  [cx cy r teeth actions gear]
  [:g
   [:circle {:style {:fill (get color-strings gear)}
             :r (* r 1.85)
             :cx cx
             :cy cy}]
   [:circle {:style {:fill "white"}
             :r (* r 1.4)
             :cx cx
             :cy cy}]
   ;; Separators
   (for [tooth (range teeth)
         :let [width (* r 0.1)
               deg (* tooth (/ 360 teeth))]]
     ^{:key tooth}
     [:rect {:x (- cx (/ width 2))
             :y (+ cy (/ r 3))
             :width width
             :height (* r 1.6)
             :style {:fill "white"}
             :transform (transform-str [:rotate {:deg deg :x cx :y cy}])}])
   ;; Cover the final three labels with lots of ugly little white rectangles
   (for [tooth (range 3)
         :let [width (* r 0.085)
               space (/ 360 teeth)
               deg (- (* tooth space) (* 2 space))]]
     (for [block-num (range (- 27 teeth))]
       ^{:key (str tooth "-" block-num)}
       [:rect {:x (- cx width)
               :y (+ cy (/ r 3))
               :width width
               :height (* r 1.6)
               :style {:fill "white"}
               :transform (transform-str
                           [:rotate {:deg (+ deg (* block-num 2))
                                     :x cx
                                     :y cy}])}]))
   ;; Water?
   (if (= :pal gear)
     (for [water-line (range 41)
           :let [width (* r 0.03)
                 deg (+ 38 (* water-line (/ 8 teeth)))]]
       ^{:key water-line}
       [:rect {:x (- cx (/ width 2))
               :y (+ cy (* r 1.4))
               :width width
               :height (* r 0.45)
               :style {:fill (:water color-strings)}
               :transform (transform-str [:rotate {:deg deg :x cx :y cy}])}]))
   ;; Labels
   (map-indexed (fn [index action]
                  (let [x cx
                        y (+ cy (* r 1.56))
                        spacing (/ 360 teeth)
                        offset (/ spacing 2)
                        deg (+ (* (+ index 1) spacing) offset)
                        transform (transform-str
                                   [:rotate {:deg deg :x cx :y cy}]
                                   [:rotate {:deg 180 :x x :y y}])]
                    ^{:key index}
                    [:text {:x x
                            :y y
                            :font-size 16
                            :text-anchor "middle"
                            :transform transform}
                     (action-label action)]))
                actions)])

(defn worker-slots
  [cx cy r teeth workers on-click]
  [:g
   (map-indexed
    (fn [index worker]
      (let [spacing (/ 360 teeth)
            deg (* index spacing)
            offset (/ spacing 2)
            transform (transform-str [:rotate {:deg (+ deg offset)
                                               :x cx
                                               :y cy}])
            color (if (= :none worker) "white" (get color-strings worker))]
        ^{:key index}
        [:g
         [:circle {:style {:fill color}
                   :on-click #(on-click index)
                   :r (/ r 5)
                   :cx (+ cx 0)
                   :cy (+ cy (* r 0.75))
                   :transform transform}]]))
    ;; slot indexes for testing
    ; [:text {:style {:pointer-events "none"}
    ;         :x cx :y (+ cy (* r 0.8)) :text-anchor "middle" :transform transform}
    ;   index]
    workers)])

(defn gear-el
  [{:keys [cx cy r teeth rotation workers on-worker-click on-center-click
           tooth-height-factor tooth-width-factor gear actions]}]
  [:g
   (action-labels cx cy r teeth actions gear)
   (corn-cost-labels cx cy r teeth)
   [:g {:transform (transform-str [:rotate {:deg (if rotation rotation 0) :x cx :y cy}])}
    [:circle {:cx cx :cy cy :r r}]
    (for [tooth (range teeth)
          :let [width (* r 0.35 tooth-width-factor)
                deg (* tooth (/ 360 teeth))]]
      ^{:key tooth}
      [:rect {:x (- cx (/ width 2))
              :y cy
              :rx (/ width 4)
              :ry (/ width 4)
              :width width
              :height (+ (/ r 3) (* r 0.7 tooth-height-factor))
              :transform (transform-str [:rotate {:deg deg :x cx :y cy}])}])
    (if workers
      (worker-slots cx cy r teeth workers on-worker-click))]
   [:circle {:style {:fill (get color-strings gear)}
             :cx cx
             :cy cy
             :r (/ r 2.1)
             :on-click on-center-click}]
   [:text {:style {:pointer-events "none"}
           :x cx
           :y (* cy 1.11)
           :font-size (* r 0.65)
           :text-anchor "middle"}
    (get symbols gear)]])

(defn worker-gear
  [{:keys [gear workers on-worker-click on-center-click actions rotation]}]
  (let [x 340]
    [:svg {:width x :height (* x 0.9)}
     [gear-el {:cx (/ x 2)
               :cy (/ x 2)
               :r (/ x 4)
               :rotation rotation
               :teeth (get-in spec [:gears gear :teeth])
               :tooth-height-factor 1.15
               :tooth-width-factor 0.75
               :workers workers
               :gear gear
               :actions actions
               :on-center-click on-center-click
               :on-worker-click on-worker-click}]]))

(defn player-circle-el
  [color]
  [:i {:key color :class (str (name color) " circle icon")}])

(defn temples-el
  [{:keys [players]}]
  [:div.ui.equal.width.grid
   (for [[t temple] (:temples spec)]
     ^{:key t}
     [:div.bottom.aligned.column {:style {:padding "0.7rem"}}
      [:div.ui.center.aligned.segment {:style {:padding "0.5rem"}}
       [:div {:style {:margin "1rem"}}
        [:span {:style {:font-size "3rem"}}
         (get symbols t)]]
       (reverse
        (map-indexed
         (fn [step-index {:keys [points material]}]
           (let [color (when (= step-index 1) "secondary ")]
             [:div {:key (str t step-index)
                    :class (str color "ui center aligned segment")
                    :style {:height 30
                            :padding "0.8em"
                            :padding-left 0
                            :padding-right 0
                            :padding-top "0.34em"
                            :margin "0.5rem"
                            :z-index 1}}
              [:span
               (map-indexed
                (fn [pid {:keys [temples color]}]
                  (when (= (get temples t) step-index)
                    (player-circle-el color)))
                players)]
              [:div.ui.top.left.attached.label {:style {:z-index -1
                                                        :width "3rem"}}
               points]
               ;(points-el points)]
              (if material
                [:div.ui.top.right.attached.label {:style {:z-index -1}}
                 (get symbols material)])]))
         (:steps temple)))]])])

(defn tech-label
  [gear & content]
  [:div {:style {:background-color (get color-strings gear)
                 :border-color (get color-strings gear)
                 :display "inline-block"
                 :padding ".3rem .4rem"
                 :border-radius ".28rem"}}
   content])

(defn tech-tracks-el
  [{:keys [players]}]
  [:div.ui.grid {:style {:margin "0.15rem"}}
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.1rem" :position "relative" :z-index 1}}
       (:resource symbols)]]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.1rem" :position "relative" :z-index 1}}
       (:resource symbols) (:resource symbols)]]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.1rem" :position "relative" :z-index 1}}
       (:resource symbols) (:resource symbols) (:resource symbols)]]
    [:div.two.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.1rem" :position "relative" :z-index 1}}
       (:resource symbols)]]]
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:div.top.attached.ui.label "agri"]]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :pal
        [:i.plus.icon] (:corn symbols))]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :water
        [:i.plus.icon] (:corn symbols))
       [:br]
       "no burn"]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :pal
        [:i.plus.icon] (:corn symbols) (:corn symbols))]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:chac symbols) "/" (:quet symbols) "/" (:kuku symbols)]]]
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:div.top.attached.ui.label "extr"]]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax
        [:i.plus.icon] (:wood symbols))
       (tech-label :pal
        [:i.plus.icon] (:wood symbols))]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax
        [:i.plus.icon] (:stone symbols))]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax
        [:i.plus.icon] (:gold symbols))]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:resource symbols) (:resource symbols)]]]
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:div.top.attached.ui.label "arch"]]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:corn symbols) [:br] "w/ build"]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (points-el 2) [:br] "w/ build"]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:i.minus.icon] (:resource symbols) [:br] "w/ build"]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (points-el 3)]]]
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:div.top.attached.ui.label "theo"]]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:chi symbols) [:i.chevron.right.icon]]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:resource symbols) ":" [:br]
       (:chac symbols) "/" (:quet symbols) "/" (:kuku symbols)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:i.plus.icon] (:skull symbols)]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (:skull symbols)]]]])

(defn event-player-el
  [player]
  (if player
    [:span (player-circle-el (:color player)) (:name player)]
    [:i.cross.icon]))

(defn event-summary-choice
  [{:keys [index decision]}]
  (let [{:keys [options type]} decision
        choice (get options index)]
    (case type
          :anger-god (str " chose to anger " (symbols-str choice))
          :beg? (str " chose " (when-not choice "not ") "to beg for corn")
          :starters " chose a starting tile"
          :action " chose an action..."
          :temple (str " chose to gain favour with " (symbols-str choice))
          :pay-resource (str " chose to pay " (symbols-str choice))
          :gain-resource (str " chose to gain " (symbols-str choice))
          :gain-materials (str " chose to gain " (symbols-str choice))
          :two-diff-temples (str " chose to gain favour with "
                                 (symbols-str choice))
          :tech (str " chose to go up on " (symbols-str choice))
          :build-monument " built a monument"
          :build-building " built a building"
          (str "ERROR: no matching choice found for key: " type))))

(defn trade-description
  [{:keys [trade]}]
  (let [[type resource] trade]
    (str (case type
           :buy " bought a "
           :sell " sold a ")
         (get symbols resource)
         " for "
         (get-in spec [:trade-values resource])
         (:corn symbols))))

(defn event-summary-el
  [[type data] state on-es-reset es-index]
  (let [active-pid (get-in state [:active :pid])
        active-player (get-in state [:players active-pid])
        turn (:turn state)
        player (get-in state [:players (:pid data)])
        dev? (or (= :give-stuff type))]
   [:div.summary {:style {:font-weight 400}}
    [:i.caret.right.link.icon]
    (when dev? [:div.ui.label "dev"])
    (when player [:span (player-circle-el (:color player)) (:name player)])
    (case type
      :new-game      [:span (:corn symbols) "New vanilla tzolkin game!"]
      :start-game    [:span (event-player-el active-player) "'s turn " turn]
      :give-stuff    [:span " + " (symbols-str (:changes data))]
      :add-player    [:span [:i {:class (str (name (:color data)) " circle icon")}] (:name data) " joined the game"]
      :place-worker  [:span (event-player-el active-player) " placed a worker on " (get symbols (:gear data))]
      :remove-worker [:span (event-player-el active-player) " removed a worker from " (get symbols (:gear data))]
      :end-turn      [:span (event-player-el active-player) "'s turn " turn]
      :choose-option [:span (event-player-el active-player) (event-summary-choice data)]
      :make-trade    [:span (event-player-el active-player) (trade-description data)]
      :stop-trading  [:span (event-player-el active-player) " finished trading"]
      (str "ERROR: no matching event for key: " type))
    [:div.date
     [:a {:on-click #(log [[type data] state])} "inspect state"]
     " | "
     [:a {:on-click #(on-es-reset es-index)} "reset here"]]]))

(defn game-log-el
  [{:keys [stream on-es-reset]}]
  [:div.ui.segment {:id "game-log"
                    :style {:overflow-y "scroll"
                            :height 250}}
   (into [:div.ui.feed]
         (map-indexed
          (fn [es-index [event state]]
            (let [[type data] event]
              [:div.event
               [:div.content
                (event-summary-el event state on-es-reset es-index)]])))
         stream)])

(defn scroll-log-down!
 []
 (set! (.-scrollTop (.getElementById js/document "game-log")) 99999))

(defn fb-conn-indicator-el
  [connected?]
  (if connected?
    [:p "Connected to server " [:i.green.check.icon]]
    [:p "Connecting to server..."]))
