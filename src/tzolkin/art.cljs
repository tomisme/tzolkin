(ns tzolkin.art
  (:require [tzolkin.spec :as spec]))

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

(defn worker-option-str
  [active]
  (let [option (:worker-option active)
        placed (:placed active)]
    (case option
      :none " has not yet chosen to pick or place."
      :place (str " has placed " placed " worker(s).")
      :pick " is picking up workers.")))

(defn status-bar
  [dstate]
  (let [turn (inc (:turn dstate))
        turns (:total-turns spec/game)
        active (:active dstate)
        next-food-day (mod (+ 3 (- turns turn)) 7)
        player-id (get-in dstate [:active :player-id])
        player (get-in dstate [:players player-id])
        player-name (:name player)
        materials (:materials player)
        corn (:corn materials)
        remaining-workers (:workers player)]
    [:div
      [:p "Turn " turn "/" turns ", that's " next-food-day " spins until Food Day!"]
      [:p [:b player-name (worker-option-str active)]]
      [:p "Click a worker to remove it, click on a fruit to place a worker
           on a specific gear."]
      [:p
        [:span remaining-workers " workers remaining | "]
        [:span (for [[k v] materials]
                 (str v " " (get symbols k) " | "))]]]))

(defn action-label
  [[k data]]
  (case k
    :gain-materials  (materials-str data)
    :choose-materials (str (materials-str (first data)) "/" (materials-str (second data)))
    :choose-action-from (str (:choose-prev symbols) (get symbols data))
    :choose-action-from-all "any"
    :tech-step "+tech"
    :build "build"
    :god-track "+god"
    :trade "trade"
    :gain-worker "+worker"
    :pay-skull (str (get symbols (:god data))
                    (:points data) "p"
                    (when (:resource data) (:resource symbols)))
    "WHAT"))

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
      [:g
        [:text {:x text-x
                :y text-y
                :style {:stroke "black"
                        :fill "black"}
                :font-size 20
                :text-anchor "middle"
                :transform transform}
          "🌽"]
        [:text {:x text-x
                :y text-y
                :style {:stroke "white"
                        :stroke-width 4
                        :paint-order "stroke"
                        :fill "black"}
                :font-size 20
                :text-anchor "middle"
                :transform transform}
         (str index)]])))

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
