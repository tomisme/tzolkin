(ns tzolkin.art)

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
  {:red "red"
   :blue "blue"})

(def symbols
  {:resource "🎁"
   :wood "🌲"
   :stone "🗿"
   :gold "🌕"
   :corn "🌽"
   :skull "💀"
   :chac "🐷"
   :quet "🦁"
   :kuku "🐵"
   :yax "🍈"
   :tik "🍑"
   :uxe "🍋"
   :chi "🍇"
   :pal "🍏"
;;   :agriculture "Agri"  | Do theses guys need a symbol?
;;   :extraction "Extr"   |
;;   :architecture "Arch" |
;;   :theology "Theo"     |
   :choose-prev "⏪"})

(defn materials-str
  [materials]
  (apply str (for [[resource amount] materials]
               (if (< amount 3)
                 (apply str (repeat amount (get symbols resource)))
                 (str amount (get symbols resource))))))

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
                :font-size 16
                :text-anchor "middle"
                :transform transform}
          "🌽"]
        [:text {:x text-x
                :y text-y
                :style {:stroke "white"
                        :stroke-width 2
                        :paint-order "stroke"
                        :fill "black"}
                :font-size 14
                :text-anchor "middle"
                :transform transform}
         (str index)]])))

(defn action-labels
  [cx cy r teeth actions]
  [:g
    [:circle {:style {:fill "pink"}
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
          [:circle {:style {:fill color}
                    :on-click #(on-click index)
                    :r (/ r 5)
                    :cx (+ cx 0)
                    :cy (+ cy (* r 0.75))
                    :transform transform}]))
      workers)])

(defn gear-el
  [{:keys [cx cy r teeth rotation workers on-worker-click on-center-click
           tooth-height-factor tooth-width-factor gear actions]}]
  [:g
    (action-labels cx cy r teeth actions)
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
    [:circle {:style {:fill "white"}
              :cx cx
              :cy cy
              :r (/ r 2.1)}]
    [:text {:x cx
            :y (* cy 1.11)
            :font-size 60
            :on-click on-center-click
            :text-anchor "middle"}
      (get symbols gear)]])
