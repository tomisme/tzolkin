(ns tzolkin.art
  (:require [tzolkin.spec :refer [spec]]))

(defn e->val
  [event]
  (-> event .-target .-value))

(defn transform-str
  [& args]
  (apply str
    (for [[type data] args]
      (case type
        :rotate (str "rotate("
                     (:deg data)
                     (if (and (:x data) (:y data))
                       (str " " (:x data) " " (:y data)))
                     ")")))))

(def color-strings
  {:red "#CC333F"
   :blue "#69D2E7"
   :orange "#EB6841"
   :yellow "#EDC951"
   :yax "#E2DF9A"
   :tik "#F23A65"
   :uxe "#EBE54D"
   :chi "#9061C2"
   :pal "#7FAF1B"})

(def symbols
  {:resource "🎁"
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
   :choose-prev "⏪"})

(defn materials-str
  [materials]
  (apply str (for [[material amount] materials]
               (if (< amount 3)
                 (apply str (repeat amount (get symbols material)))
                 (str amount (get symbols material))))))

(defn temples-str
  [temples]
  (apply str (for [[temple amount] temples]
               (apply str (repeat amount (get symbols temple))))))

(defn tech-str
  [tech]
  (str "› " (name tech)))

(defn food-day-str
  [until-food-day]
  [:span (if (= 0 until-food-day)
           [:b "it's Food Day!"]
           (str until-food-day " spins until Food Day!"))])

(defn points-el
  [num]
  [:div.ui.label num])

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
           :style {:width 124
                   :margin 5}}
      [:div.content {:style {:height 50}}
        [:div {:class (str "ui " (name color) " corner label")}]
        (if choosing?
          [:a {:on-click on-click}
            (materials-str cost)]
          (materials-str cost))]
      [:div.center.aligned.content {:style {:height 110}}
        [:div.description
          (when farm (farm-el farm))
          (when tech [:p (tech-str tech)])
          (when gain-worker [:p "+worker"])
          (when free-action-for-corn [:p (get symbols :corn) ": free!"])
          (when build [:p "build " (name build)])
          (when temples [:p (temples-str temples)])
          (when materials [:p (materials-str materials)])
          (when points [:p (points-el points)])]]]))

(defn available-buildings
  [buildings on-decision choosing?]
  [:div.ui.segment
    [:div.ui.cards
      (map-indexed
        (fn [index building]
          ^{:key building}
          [:div.item (building-card building #(on-decision index) choosing?)])
        (take (:num-available-buildings spec) buildings))]])

(defn decisions-el
  [active on-decision]
  (let [decision (:decision active)
        type (:type decision)
        decision-options (:options decision)]
    (if (= :build-building type)
      [:span " needs to choose a building."]
      [:span
        " needs to choose between "
        (map-indexed
          (fn [index option]
            (case type
              :gain-materials ^{:key index}
                              [:button {:on-click #(on-decision index)}
                                (materials-str option)]))
          decision-options)])))

(defn active-player-status
  [active on-decision]
  (let [decision (:decision active)]
    (if decision
      (decisions-el active on-decision)
      (let [worker-option (:worker-option active)
            placed (:placed active)]
        (case worker-option
          :none " has not yet chosen to pick or place."
          :place (str " has placed " placed " worker(s).")
          :pick " is picking up workers.")))))

(defn player-buildings
  [buildings]
  [:div.ui.cards
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div.item (building-card building nil false)])
      buildings)])

(defn player-stats
  [player-id player]
  (let [{:keys [color materials points workers buildings]} player
        player-name (:name player)]
    ^{:key player-id}
    [:div.ui.segment
      [:p
        [:a {:class (str "ui " (name color) " ribbon label")}
          player-name]
        [:span workers " workers remaining | "]
        [:span (for [[k v] materials]
                 (str v " " (get symbols k) " | "))]
        [:span points " victory points"]]
      (if (seq buildings)
        (player-buildings buildings)
        [:p "No buildings."])]))


(defn status-bar
  [state on-decision]
  (let [turn (:turn state)
        turns (:total-turns spec)
        until-food-day (get-in spec [:until-food-day turn])
        buildings (:buildings state)
        active (:active state)
        choosing-building? (= :build-building (get-in active [:decision :type]))
        active-player-id (:player-id active)
        active-player (get-in state [:players active-player-id])
        active-player-name (:name active-player)]
    [:div
      [:p "Turn " turn "/" turns ", " (food-day-str until-food-day)]
      [:p active-player-name (active-player-status active on-decision)]
      (map-indexed player-stats (:players state))
      (available-buildings buildings on-decision choosing-building?)]))

(defn action-label
  [[k data]]
  (case k
    :gain-materials  (materials-str data)
    :choose-materials (str (materials-str (first data)) "/" (materials-str (second data)))
    :choose-action-from (str (:choose-prev symbols) (get symbols data))
    :choose-action-from-all "any"
    :tech-step "+tech"
    :build "build"
    :temple "+temple"
    :trade "trade"
    :gain-worker "+worker"
    :pay-skull (str (get symbols (:temple data))
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
              color (or (get color-strings worker) "white")]
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
            ;   index]]))
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
  ^{:key gear}
  [:svg {:width 340 :height 340}
    [gear-el {:cx 170
                  :cy 170
                  :r 85
                  :rotation rotation
                  :teeth (get-in spec [:gears gear :teeth])
                  :tooth-height-factor 1.15
                  :tooth-width-factor 0.75
                  :workers workers
                  :gear gear
                  :actions actions
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click}]])

(defn temples-el
  [state]
  [:div.ui.segment
    [:table.ui.very.basic.celled.table
      [:tbody
        (for [[t temple] (:temples spec)]
          ^{:key t}
          [:tr
            [:td (get symbols t)]
            (map-indexed
              (fn [index step]
                ^{:key step}
                [:td
                  (points-el (:points step))
                  (get symbols (:material step))
                  [:br]
                  (map-indexed
                    (fn [player-id player]
                      (let [track (get-in player [:temples t])
                            color (get-in state [:players player-id :color])
                            color-str (name color)]
                        (when (= track index)
                          ^{:key (str track color)}
                          [:i {:class (str "ui " color-str " empty circular label")}])))
                    (:players state))])
              (get-in spec [:temples t :steps]))])]]])
