(ns tzolkin.art
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.utils :refer [log sin cos pi]]
   [reagent.core :as rg]
   [reagent-forms.core :refer [bind-fields]])
  (:require-macros
   [tzolkin.macros :refer [embed-svg]]))

(def el-titles
  {:resource "resource"
   :worker "worker"
   :wood "wood"
   :stone "stone"
   :gold "gold"
   :corn "corn"
   :skull "skull"
   :chac "Chaac"
   :quet "Quetzalcoatl"
   :kuku "Kukulcan"
   :yax "Yaxchilan"
   :tik "Tikal"
   :uxe "Uxmal"
   :chi "Chichen Itza"
   :pal "Palenque"
   :agri "agriculture"
   :extr "extraction"
   :arch "architecture"
   :theo "theology"
   :choose-prev "choose a previous action"
   :water "Palenque fishing"
   :points "points"
   :double-spin-ok "player can double spin"})

(def color-strings
  {:red "#CC333F"
   :blue "#69D2E7"
   :orange "#EB6841"
   :yellow "#EDC951"
   :water "#73BDF8"
   :wood "#00995A"
   :corn "#D9FB19"
   :jungle "#EDDEA7"
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

(def color-set
  #{:red :blue :orange :yellow})

(defn e->val
  [event]
  (-> event .-target .-value))

(defn transform-str
  "Takes any number of (supported) svg transform definitions
  and returns a string for use as an svg element's `transform` attribute"
  [& args]
  (clojure.string/join
    (for [[type data] args]
      (case type
        :rotate (str "rotate("
                     (:deg data)
                     (if (and (:x data) (:y data))
                       (str " " (:x data) " " (:y data)))
                     ")")))))

(defn img-path
  [k]
  (case k
    :corn  "emoji/1f33d"
    :smile "emoji/1f603"
    ; :wood  "emoji/1f332"
    :wood  "svg/tree"
    :stone "svg/rock"
    :gold  "svg/ingot"
    :skull "svg/gem"
    ; :chac  "emoji/1f40c"
    :chac  "emoji/1f430"
    ; :quet  "emoji/1f40a"
    :quet  "emoji/1f42e"
    ; :kuku  "emoji/1f406"
    :kuku  "emoji/1f981"
    :yax   "emoji/1f351"
    :tik   "emoji/1f353"
    :uxe   "emoji/1f34b"
    :chi   "emoji/1f347"
    :pal   "emoji/1f34f"
    :agri "emoji/1f342"
    :extr "svg/pick"
    ; :arch "emoji/1f58c"
    :arch "emoji/2696"
    :theo "emoji/1f4d6"
    :resource "svg/cube"
    :spin "svg/spin"
    :points "emoji/2b50"
    :worker "emoji/1f464"
    :plus-yax "svg/plus-yax"
    :plus-chi "svg/plus-chi"
    :plus-pal "svg/plus-pal"
    :plus-water "svg/plus-water"
    :resource-day "svg/cube-question"
    :spinner "svg/spinner"
    :hat "svg/hat"
    :food-day-points "svg/food-day-points"
    :food-day-mats "svg/food-day-mats"
    :age1-spot "svg/age1-spot"
    :age2-spot "svg/age2-spot"
    :building "svg/building-symbol"
    :double-spin-ok "svg/double-spin-ok"
    "emoji/2753"))

(defn svg-icon-el
  [k]
  (let [name (img-path k)]
    [:div.svg-icon {:style {:background-image (str "url(images/" name ".svg)")
                            ; :background-repeat "no-repeat"
                            :background-size "contain"
                            :display "inline-block"
                            :width "1.4em"
                            :height "1.4em"}
                            ; :line-height "normal"}
                    :title (get el-titles k)}]))

(defn temple-icon
  [temple]
  [:div {:class "hover-grower"
         :style {:cursor "pointer"}}
   (svg-icon-el temple)])

(defn inner-svg
  [k x y size]
  (let [href (str "images/" (img-path k) ".svg")]
    [:g {:dangerouslySetInnerHTML
         {:__html (str "<image x=\""
                       x
                       "\" y=\""
                       y
                       "\" width=\""
                       size
                       "\" height=\""
                       size
                       "\" xlink:href=\""
                       href
                       "\"/>")}}]))

(defn amount-num-el
  [amount]
  [:div {:style {:padding-left "0.4em"
                 :padding-top "0.25em"
                 :float "left"
                 :width "0.7em"
                 :position "relative"
                 :word-wrap "normal"
                 :text-shadow "-2px 0 white, 0 2px white, 2px 0 white, 0 -2px white"}}
   amount])

(defn svg-icon-group-el
  [icons]
  (into [:span]
   (map
    (fn [[icon amount]]
     (if (> amount 2)
       [:div {:style {:display "inline-block"}}
        (amount-num-el amount) (svg-icon-el icon)]
       (into [:span]
         (repeat amount (svg-icon-el icon)))))
    icons)))

(defn symbols-str
  "Takes a map of materials and the amount of each and returns a string of
  symbols. Amounts larger than two represented by a number and a single symbol."
  [materials]
  (apply str (for [[material amount] materials]
               (if (< amount 2)
                 (apply str (repeat amount (get symbols material)))
                 (str amount (get symbols material))))))

(defn tech-str
  [tech]
  (case tech
    :any "any tech"
    :any-two "2x any tech"
    (symbols-str tech)))

(defn points-el
  [points]
  [:div.ui.label {:style {:padding ".3rem .51rem"}}
   points])

(defn farm-el
  [farms]
  (if (= :all farms)
    [:p "(🌽✔)"]
    (map-indexed (fn [index _] ^{:key index} [:p "(🌽🌽✔)"]) (range farms))))

(defn building-card
  [building on-click choosing?]
  (let [{:keys [cost color materials points tech farm temples gain-worker
                free-action-for-corn build]} building]
    [:div {:class (str (name color) " ui card")
           :style {:width "7rem"
                   :margin "0.3rem"
                   :font-size "0.9rem"}}
      [:div.content {:style {:height "2.5rem"
                             :padding-top "0.5rem"
                             :padding-left "0.3rem"
                             :z-index 1}}
        [:div {:class (str "ui " (name color) " corner label")
               :style {:z-index -1}}]
        (if choosing?
          [:a {:on-click on-click}
            (svg-icon-group-el cost)]
          (svg-icon-group-el cost))]
      [:div.center.aligned.content {:style {:height "6.1rem"}}
        [:div.description
          (when farm (farm-el farm))
          (when tech
            (if (map? tech)
              [:div (svg-icon-group-el tech)]
              [:p (tech-str tech)]))
          (when gain-worker (svg-icon-el :worker))
          (when free-action-for-corn [:div (svg-icon-el :corn) ": action"])
          (when build [:p (name build)])
          (when temples
            (if (contains? temples :any)
              [:p "any temple"]
              (svg-icon-group-el temples)))
          (when materials (svg-icon-group-el materials))
          (when points [:div (points-el points)])]]]))

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
  [:div.ui.button.card {:style {:width "6rem"
                                :margin "0.3rem"
                                :font-size "0.9rem"
                                :font-weight "inherit"}
                        :on-click on-select}
   [:div.center.aligned.content {:style {:padding "0.8rem 0"}}
    [:div.description
      (when materials [:div (svg-icon-group-el materials)])
      (when tech [:div (svg-icon-el tech)])
      (when farm (farm-el farm))
      (when gain-worker [:div (svg-icon-el :worker)])
      (when temple [:div (svg-icon-el temple)])]]])

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
                   :double-spin? "whether to spin the wheel again."
                   :pay-discount "which resource to not require."
                   :anger-god "which god to anger."
                   :beg? "whether to beg for corn."
                   :starters "which starter tile to take."
                   :action "which action to take."
                   :gain-materials "which materials to gain."
                   :jungle-mats "which materials to gain."
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
       (into [:div {:class (when (= :starters type) "ui cards")
                    :style {:margin-top "0.5rem"
                            :margin-bottom "0.5rem"}}
              [:div.basic.ui.segment {:style {:display "inline"}}
               [:i.large.chevron.right.icon]]]
         (map-indexed
           (fn [index option]
             (if (= :starters type)
               [:div (starter-card option #(on-decision index decision))]
               [:button.ui.button {:on-click #(on-decision index decision)}
                 (case type
                   :double-spin? (if option "Spin twice" "Don't spin twice")
                   :beg? (if option "Beg for corn" "Don't beg")
                   :action (str option)
                   :build-building index
                   :build-monument index
                   (svg-icon-group-el option))]))
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
  [:div.ui.cards {:style {:margin-top "0.5rem"
                          :padding-bottom "0.3rem"}}
                          ; :margin-bottom "0"}}
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div.item (building-card building nil false)])
      buildings)])

(defn player-circle-el
  [color]
  [:i {:key color
       :class (str (name color) " circle icon")}])

(def new-player-form-template
 [:span
  [:div.ui.input {:style {:margin-right "1em"}}
   [:input.form-control {:id :new-player.name
                         :field :text
                         :type :text}]]])

(defn new-player-form-el
  [on-add-player current-players]
  (let [doc (rg/atom {:new-player {:color :red}})
        next-color (fn [color]
                    (rand-nth (seq (disj color-set color))))
        on-next-color (fn [color]
                        (swap! doc assoc-in [:new-player :color] (next-color color)))]
    (fn []
      (let [color (-> @doc :new-player :color)]
        [:div
         [:span {:style {:font-size "2em"}
                 :on-click #(on-next-color color)}
          (player-circle-el color)]
         [bind-fields new-player-form-template doc]
         [:div.ui.submit.button {:on-click #(on-add-player (:new-player @doc))}
          "Add Player"]]))))

;; TODO fix padding to use rem
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
             [:div {:style {:display "inline-block"
                            :margin-bottom "0.2rem"}}
              (amount-num-el corn) (svg-icon-el :corn)]]
        (map
         (fn [[k v]]
           [:div {:style {:padding 3}}
            [:button.ui.icon.button {:on-click #(on-trade [:buy k])}
             [:i.plus.icon]]
            [:span {:style {:padding 4}}
             [:div {:style {:display "inline-block"}}
              (amount-num-el v) (svg-icon-el k)]
             " ("
             [:div {:style {:display "inline-block"}}
              (amount-num-el (get-in spec [:trade-values k])) (svg-icon-el :corn) "ea.)"]]
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

(defn player-stats-el
  [pid player active?]
  (let [{:keys [color double-spin? materials points workers buildings]} player
        player-name (:name player)
        box-shadow (str "0 1px 10px 0 " (get color-strings color))]
    ^{:key pid}
    [:div.ui.segment (when active? {:style {:box-shadow box-shadow}})
     [:div
      [:span
       [:a {:class (str "ui " (name color) " label")
            :style {:font-size "1rem"}}
         player-name]]
      [:div {:style {:display "inline-flex"
                     :position "absolute"
                     :left "6.5rem"
                     :top "1.4rem"
                     :font-size "1.2rem"}}
       (amount-num-el workers)
       [:span {:style {:padding-top "0.2em"}}
        (player-circle-el color)]
       (for [[k v] materials]
         [:div {:key k}
          (amount-num-el v)
          (svg-icon-el k)])
       (amount-num-el points)
       (svg-icon-el :points)
       (when double-spin?
         [:span {:style {:margin-left "0.2em"}}
          (svg-icon-el :double-spin-ok)])]
      (when (seq buildings)
        (player-buildings-el buildings))]]))

(defn status-bar-el
  [state on-decision on-trade on-stop-trading on-end-turn on-start-game on-add-player]
  (let [turn (:turn state)
        started? (pos? turn)
        players (:players state)
        num-players (count players)
        buildings (:buildings state)
        active (:active state)
        choosing-building? (= :build-building (get-in active [:decision :type]))
        active-pid (:pid active)
        active-player (get-in state [:players active-pid])
        color-str (if (:color active-player)
                    (name (:color active-player))
                    "teal")]
    [:div
     (when-not started?
       [:p
        [:button.ui.button {:class (str "ui button "
                                        (when-not (> num-players 0) "disabled"))
                            :on-click on-start-game}
         "Start Game"]])
     [:div
      (if started?
        (active-player-command-bar-el active active-player on-decision on-trade on-stop-trading color-str)
        [new-player-form-el on-add-player players])
      (available-buildings buildings on-decision choosing-building?)
      (map-indexed
       (fn [pid player]
         (player-stats-el pid player (= active-pid pid)))
       (:players state))]]))

(defn action-label
  [[k data]]
  (case k
    :gain-materials  (symbols-str (:mats data))
    :jungle-mats (if (pos? (:jungle-id data))
                   (str (symbols-str {:corn (:corn data)})
                        "/"
                        (symbols-str {:wood (:wood data)}))
                   (symbols-str {:corn (:corn data)}))
    :choose-action (if (= (:gear data) :non-chi)
                     (str (:corn symbols) ": action")
                     (get symbols (:gear data)))
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
          size (/ r 4)
          text-x cx
          text-y (+ cy (* r 1.15))
          transform (transform-str [:rotate {:deg (+ deg offset)
                                             :x cx
                                             :y cy}]
                                   [:rotate {:deg 180
                                             :x text-x
                                             :y text-y}])]
      ^{:key (str "corn-cost" index)}
      [:g {:transform transform}
       (inner-svg :corn
                  (- text-x (/ size 2))
                  (- text-y (/ size 1.5))
                  size)
       [:text {:x text-x
               :y text-y
               :style {:stroke "white"
                       :stroke-width 4
                       :paint-order "stroke"
                       :fill "black"}
               :font-size 14
               :text-anchor "middle"}
        index]])))

(defn action-labels
  [cx cy r teeth actions gear]
  [:g
   [:circle {:style {:fill (get color-strings gear)}
             :r (* r (if (= :chi gear) 1.79 1.85))
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
             :y (+ cy (/ r 3.6))
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
               :y (+ cy (/ r 3.6))
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
                        deg (+ (* (inc index) spacing) offset)
                        transform (transform-str
                                   [:rotate {:deg deg :x cx :y cy}]
                                   [:rotate {:deg 180 :x x :y y}])]
                    ^{:key index}
                    [:text {:x x
                            :y y
                            :font-size "1rem"
                            :text-anchor "middle"
                            :transform transform}
                     (action-label action)]))
                actions)])

(defn worker-slots
  [cx cy r teeth workers on-click gear]
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
         [:circle {:class "hover-opacity"
                   :style {:fill color
                           :cursor "pointer"}
                   :on-click #(on-click index)
                   :r (/ r (if (= :chi gear) 6.2 5))
                   :cx (+ cx 0)
                   :cy (+ cy (* r (if (= :chi gear) 0.78 0.75)))
                   :transform transform}]]))
         ; ; slot indexes for testing
         ; [:text {:style {:pointer-events "none"}
         ;         :x cx :y (+ cy (* r 0.8)) :text-anchor "middle" :transform transform}
         ;   index]]))
    workers)])

(defn tile-svg
  [x y size r type]
  [:g
   [:rect {:x x
           :y y
           :rx (/ r 15)
           :ry (/ r 15)
           :fill (case type
                   :corn (:corn color-strings)
                   :wood (:wood color-strings))
           :width (/ size 2.2)
           :height (/ size 2.2)}]
   (inner-svg type
              (+ x (/ size 16))
              (+ y (/ size 20))
              (/ size 3))])

(defn jungle-svg
  [cx cy r teeth jungle]
  (let [size (/ r 1.5)
        step (/ 360 teeth)]
    [:g
     (map-indexed
      (fn [index box]
        (let [corn (:corn-tiles box)
              wood (:wood-tiles box)]
          [:g {:key index
               :transform (transform-str [:rotate {:deg (- (* index step)
                                                           (* 2.5 step))
                                                   :x cx
                                                   :y cy}])}
           [:rect {:stroke (get color-strings :jungle)
                   :stroke-width (/ size 8)
                   :fill (get color-strings :jungle)
                   :x (- cx (/ size 2))
                   :y (- (* r -0.15) (/ size 2))
                   :rx (/ r 10)
                   :ry (/ r 10)
                   :width size
                   :height size}]
           (when (> corn 0)
             (tile-svg (- cx (/ size 2.1)) (- (* r -0.15) (/ size 2.1)) size r :corn))
           (when (> corn 1)
             (tile-svg cx (- (* r -0.15) (/ size 2.1)) size r :corn))
           (when (> corn 2)
             (tile-svg (- cx (/ size 2.1)) (* r -0.15) size r :corn))
           (when (> corn 3)
             (tile-svg  cx (* r -0.15) size r :corn))
           (when (> wood 0)
             (tile-svg (- cx (/ size 2.1)) (- (* r -0.15) (/ size 2.1)) size r :wood))
           (when (> wood 1)
             (tile-svg cx (- (* r -0.15) (/ size 2.1)) size r :wood))
           (when (> wood 2)
             (tile-svg (- cx (/ size 2.1)) (* r -0.15) size r :wood))
           (when (> wood 3)
             (tile-svg  cx (* r -0.15) size r :wood))]))
      jungle)]))

(defn gear-svg
  [{:keys [cx cy r teeth rotation workers on-worker-click on-center-click
           tooth-height-factor tooth-width-factor gear actions jungle]}]
  [:g {:transform (transform-str [:rotate {:deg (/ 360 teeth)
                                           :x cx
                                           :y cy}])}
   (action-labels cx cy r teeth actions gear)
   (corn-cost-labels cx cy r teeth)
   [:g {:transform (transform-str [:rotate {:deg (if rotation rotation 0) :x cx :y cy}])}
    [:circle {:cx cx :cy cy :r r}]
    (for [tooth (range teeth)
          :let [width (* r (if (= :chi gear) 0.29 0.35) tooth-width-factor)
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
      (worker-slots cx cy r teeth workers on-worker-click gear))]
   [:circle {:class "hover-opacity"
             :style {:fill "white"
                     :cursor "pointer"}
             :cx cx
             :cy cy
             :r (/ r (if (= :chi gear) 1.8 2.1))
             :on-click on-center-click}]
   [:g {:style {:pointer-events "none"}
        :transform (transform-str [:rotate {:deg (-
                                                  (* (if gear
                                                       (-> spec :gears gear :location)
                                                       0)
                                                     (/ 360 -26))
                                                  (/ 360 teeth))
                                            :x cx
                                            :y cy}])}
    (let [s (* r 0.65)
          s2 (/ s 2)]
      (inner-svg gear (- cx s2) (- cy s2) s))]
   (when (= :pal gear) (jungle-svg cx cy r teeth jungle))])

(defn worker-gear-svg
  [{:keys [gear workers on-worker-click on-center-click actions rotation size jungle]}]
  [gear-svg {:cx (/ size 1.97)
             :cy (/ size 5)
             :r (if (= :chi gear) 100 80)
             :rotation rotation
             :teeth (get-in spec [:gears gear :teeth])
             :tooth-height-factor 1.15
             :tooth-width-factor 0.75
             :workers workers
             :gear gear
             :jungle jungle
             :actions actions
             :on-center-click on-center-click
             :on-worker-click on-worker-click}])

(defn players-dial-el
  [size player-order players active]
  (let [active-pid (:pid active)
        width size
        height size
        distance (/ width 11)
        el-r (/ size 70)
        num (count player-order)
        cx (/ width 2)
        cy (/ height 2)
        el-cx (fn [i] (+ cx (* distance (cos (/ (* 2 i pi) num)))))
        el-cy (fn [i] (+ cy (* distance (sin (/ (* 2 i pi) num)))))]
    [:g
     (into [:g]
       (map-indexed
        (fn [i pid]
          (let [player (get players pid)
                color (:color player)]
            [:g
             [:circle {:cx (el-cx i)
                       :cy (el-cy i)
                       :r el-r
                       :fill (get color-strings color)}]
             (when (= active-pid pid)
               (let [spinner-size (* el-r 3.5)
                     spinner-x (- (el-cx i) (/ spinner-size 2))
                     spinner-y (- (el-cy i) (/ spinner-size 2))]
                 (inner-svg :spinner spinner-x spinner-y spinner-size)))]))
        player-order))
     [:g {:transform (transform-str [:rotate {:deg 90 :x (/ size 2) :y (/ size 2)}])}
      (inner-svg :hat
                 (- cx (/ (* el-r 2.2) 2))
                 (- cy (/ size 7.9))
                 (* el-r 2.2))]]))

(defn turn-clock-el
  [size turn]
  (let [width size
        height size
        distance (/ width 6.5)
        el-size (/ size 30)
        num (:total-turns spec)
        cx (/ width 2)
        cy (/ height 2)
        el-cx (fn [i] (+ cx (* distance (cos (/ (* 2 i pi) num)))))
        el-cy (fn [i] (+ cy (* distance (sin (/ (* 2 i pi) num)))))]
    (into [:g]
      (map (fn [i]
             (when (>= i (dec turn))
               [:g {:transform (transform-str [:rotate {:deg 90
                                                        :x (el-cx i)
                                                        :y (el-cy i)}])}
                (inner-svg (case (-> (:turns spec) (get i) :type)
                             :normal          (case (-> (:turns spec) (get i) :age)
                                                1 :age1-spot
                                                2 :age2-spot)
                             :mats-food-day   :food-day-mats
                             :points-food-day :food-day-points)
                           (- (el-cx i) (/ el-size 2))
                           (- (el-cy i) (/ el-size 2))
                           el-size)]))
            ; [:text {:x (el-cx i)
            ;         :y (el-cy i)
            ;         :transform (transform-str [:rotate {:deg 90
            ;                                             :x (el-cx i)
            ;                                             :y (el-cy i)}])}
            ;  i])
           (range num)))))

(defn middle-of-gears
  [size turn player-order players active on-end-turn on-take-starting-player]
  [:g
   [:g {:transform (transform-str [:rotate {:deg -90 :x (/ size 2) :y (/ size 2)}])}
    (players-dial-el size player-order players active)
    (turn-clock-el size turn)]
   [:g {:class "hover-opacity"
        :style {:cursor "pointer"}
        :on-click on-end-turn}
    (let [turn-button-size (/ size 8)
          x (- (/ size 2) (/ turn-button-size 2))
          y x]
      (inner-svg :spin x y turn-button-size))]])

(defn starting-player-space
  [on-click corn-bonus canvas-size active-pid player-color]
  (let [hat-x (* canvas-size 0.83)
        hat-y (* canvas-size 0.13)
        hat-size (/ canvas-size 10)
        player-size (/ canvas-size 70)]
    [:g {:style {:cursor "pointer"}
         :on-click #(on-click active-pid)}
     (inner-svg :hat hat-x hat-y hat-size)
     (inner-svg :corn
                (+ hat-x (/ hat-size 3.5))
                (+ hat-y (/ hat-size 4.2))
                (/ hat-size 2.5))
     [:text {:x (+ hat-x (/ hat-size 2))
             :y (+ hat-y (/ hat-size 2))
             :style {:stroke "white"
                     :stroke-width 4
                     :paint-order "stroke"
                     :fill "black"}
             :font-size 18
             :text-anchor "middle"}
      corn-bonus]
     (when player-color
       [:circle {:cx hat-x
                 :cy hat-y
                 :r player-size
                 :fill (get color-strings player-color)}])]))

(defn gear-layout-el
  [gear-data jungle turn player-order players active on-end-turn
   on-take-starting-player starting-player-corn pid-on-start-space]
  (let [size 850]
    [:svg {:width size :height size}
           ;; for testing
           ; :style {:background-color "pink"}}
     (starting-player-space on-take-starting-player
                            starting-player-corn
                            size
                            (:pid active)
                            (get-in players [pid-on-start-space :color]))

     (map
      (fn [gear]
        (let [loc (-> spec :gears gear :location)]
          [:g {:key gear
               :transform (transform-str [:rotate {:deg (* loc (/ 360 26))
                                                   :x (/ size 1.97)
                                                   :y (if (= :chi gear)
                                                        (/ size 1.85)
                                                        (/ size 1.95))}])}
           (worker-gear-svg {:gear gear
                             :size size
                             :jungle (if (= :pal gear) jungle)
                             :workers (-> gear-data gear :workers)
                             :on-worker-click (-> gear-data gear :on-worker-click)
                             :on-center-click (-> gear-data gear :on-center-click)
                             :teeth (-> gear-data gear :teeth)
                             :rotation (-> gear-data gear :rotation)
                             :actions (-> gear-data gear :actions)})]))
      '(:pal :yax :tik :uxe :chi))
     (middle-of-gears size turn player-order players active on-end-turn on-take-starting-player)]))

(defn temples-el
  [{:keys [players]}]
  [:div.ui.equal.width.grid {:style {:padding "0.5rem"
                                     :margin-right "0.2rem"
                                     :font-size 16}}
   (for [[t temple] (:temples spec)]
     ^{:key t}
     [:div.bottom.aligned.column
      [:div
       [:div {:style {:width "5.5rem"
                      :margin "0 auto"
                      :font-size "4rem"}}
        [temple-icon t]]
       (reverse
        (map-indexed
         (fn [step-index {:keys [points material]}]
           (let [color (when (= step-index 1) "secondary ")]
             [:div {:key (str t step-index)
                    :class (str color "ui center aligned segment")
                    :style {:height "2.4rem"
                            :padding-top "0.24rem"
                            :margin "0.5rem"
                            :margin-left 0
                            :margin-right 0
                            :z-index 1}}
              [:div {:style {:position "absolute"
                             :bottom "0.5rem"
                             :left 0
                             :right 0
                             :margin "auto"}}
               (map-indexed
                (fn [pid {:keys [temples color]}]
                  (when (= (get temples t) step-index)
                    (player-circle-el color)))
                players)]
              (when (not= 1 step-index)
                [:div {:style {:position "absolute"
                               :left "0.5rem"}}
                 (points-el points)])
              (if material
                [:div {:style {:position "absolute"
                               :right "0.3rem"}}
                 (svg-icon-el material)])]))
         (:steps temple)))]])])

(defn tech-label
  [gear]
  (svg-icon-el (case gear
                 :pal :plus-pal
                 :yax :plus-yax
                 :chi :plus-chi
                 :water :plus-water)))

(defn tech-player-circles
  [players track step]
  (map
   (fn [player]
     (when (= (get-in player [:tech track]) step)
       (player-circle-el (:color player))))
   players))

(defn tech-first-col
  [players track]
  [:div.two.wide.column {:style {:padding "0.2rem"}}
    [:div.ui.segment {:style {:height "7rem"
                              :padding-left "0.2rem"
                              :padding-right "0.2rem"
                              :text-align "center"}}
     [:div {:style {:text-align "center"
                    :position "relative"
                    :top "-0.6rem"
                    :font-size "1.5rem"}}
      [temple-icon track]]
     (tech-player-circles players track 0)]])

(defn tech-player-box
  [players track step]
  [:div {:style {:top "auto"
                 :bottom "0.5rem"
                 :position "absolute"
                 :width "100%"}}
   (tech-player-circles players track step)])

(defn tech-tracks-el
  [players]
  [:div.ui.grid {:style {:margin 0
                         :font-size 16}}
   [:div.row {:style {:padding "0.1rem"}}
    [:div.two.wide.column {:style {:padding "0.2rem"}}]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.4rem" :position "relative" :z-index 1}}
       (svg-icon-el :resource)]]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.4rem" :position "relative" :z-index 1}}
       (svg-icon-el :resource) (svg-icon-el :resource)]]
    [:div.four.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.4rem" :position "relative" :z-index 1}}
       (svg-icon-el :resource) (svg-icon-el :resource) (svg-icon-el :resource)]]
    [:div.two.wide.center.aligned.column {:style {:padding "0.2rem"}}
      [:span {:style {:top "1.4rem" :position "relative" :z-index 1}}
       (svg-icon-el :resource)]]]
   [:div.row {:style {:padding "0.1rem"}}
    (tech-first-col players :agri)
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :pal)
       (svg-icon-el :corn)
       (tech-player-box players :agri 1)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :water)
       (svg-icon-el :corn)
       [:p {:style {:font-size 14}}
        "no tile req."]
       (tech-player-box players :agri 2)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :pal)
       (svg-icon-el :corn) (svg-icon-el :corn)
       (tech-player-box players :agri 3)]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:div {:display "inline"}
        (svg-icon-el :chac) (svg-icon-el :quet) (svg-icon-el :kuku)]]]]
   [:div.row {:style {:padding "0.1rem"}}
    (tech-first-col players :extr)
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax)
       (tech-label :pal)
       (svg-icon-el :wood)
       (tech-player-box players :extr 1)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax)
       (svg-icon-el :stone)
       (tech-player-box players :extr 2)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (tech-label :yax)
       (svg-icon-el :gold)
       (tech-player-box players :extr 3)]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (svg-icon-el :resource) (svg-icon-el :resource)]]]
   [:div.row {:style {:padding "0.1rem"}}
    (tech-first-col players :arch)
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (svg-icon-el :corn)
       (svg-icon-el :building)
       (tech-player-box players :arch 1)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (points-el 2)
       (svg-icon-el :building)
       (tech-player-box players :arch 2)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [:i.minus.icon] (svg-icon-el :resource)
       (svg-icon-el :building)
       (tech-player-box players :arch 3)]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (points-el 3)]]]
   [:div.row {:style {:padding "0.1rem"}}
    (tech-first-col players :theo)
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (svg-icon-el :chi) [:i.chevron.right.icon]
       (tech-player-box players :theo 1)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"
                                :padding-left "0.25rem"
                                :padding-right "0.2rem"}}
       (svg-icon-el :resource) ":"
       (svg-icon-el :chac) "/" (svg-icon-el :quet) "/" (svg-icon-el :kuku)
       (tech-player-box players :theo 2)]]
    [:div.four.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       [tech-label :chi] (svg-icon-el :skull)
       (tech-player-box players :theo 3)]]
    [:div.two.wide.column {:style {:padding "0.2rem"}}
      [:div.ui.segment {:style {:height "7rem"}}
       (svg-icon-el :skull)]]]])

(defn event-player-el
  [player]
  (if player
    [:span (player-circle-el (:color player)) (:name player)]
    [:i.cross.icon]))

(defn event-icon-group-el
  [icon]
  [:span {:style {:position "relative"
                  :bottom "-0.4em"}}
   (svg-icon-group-el icon)])

(defn event-icon-el
  [icon]
  [:span {:style {:position "relative"
                  :bottom "-0.4em"}}
   (svg-icon-el icon)])

(defn event-summary-choice
  [{:keys [index decision]}]
  (let [{:keys [options type]} decision
        choice (get options index)]
    (case type
      :double-spin? [:span " chose " (when-not choice "not ") "to spin the wheel twice!"]
      :anger-god [:span " chose to anger " (event-icon-group-el choice)]
      :beg? [:span " chose " (when-not choice "not ") "to beg for corn"]
      :starters " chose a starting tile"
      :action " chose an action..."
      :temple [:span " chose to gain favour with " (event-icon-group-el choice)]
      :pay-resource [:span " chose to pay " (event-icon-group-el choice)]
      :pay-discount [:span " chose to reduce building cost by " (event-icon-group-el choice)]
      :gain-resource [:span " chose to gain " (event-icon-group-el choice)]
      :gain-materials [:span " chose to gain " (event-icon-group-el choice)]
      :jungle-mats [:span " chose to gain " (event-icon-group-el choice)]
      :two-diff-temples [:span " chose to gain favour with "
                               (event-icon-group-el choice)]
      :tech [:span " chose to go up on " (event-icon-group-el choice)]
      :build-monument " built a monument"
      :build-building " built a building"
      [:span "ERROR: no matching choice found for key: " type])))

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
      :new-game      [:span (event-icon-el :corn) "New vanilla tzolkin game!"]
      :start-game    [:span (event-player-el active-player) "'s turn " turn]
      :take-starting [:span (event-player-el active-player) " took starting player " (event-icon-el :hat)]
      :give-stuff    [:span " + " (event-icon-group-el (:changes data))]
      :add-player    [:span [:i {:class (str (name (:color data)) " circle icon")}] (:name data) " joined the game"]
      :place-worker  [:span (event-player-el active-player) " placed a worker on " (event-icon-el (:gear data))]
      :remove-worker [:span (event-player-el active-player) " removed a worker from " (event-icon-el (:gear data))]
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
                            :height "12rem"}}
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
