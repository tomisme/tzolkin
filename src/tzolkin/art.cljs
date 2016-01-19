(ns tzolkin.art
  (:require
   [reagent.core :as rg]
   [timothypratley.reanimated.core :as anim])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]))

(defcard-doc
  "##Gears
  The spinning gears are a one of the coolest mechanics in Tzolk'in.

  Let's make them out of svg elements!")

(defn e->val
  [event]
  (-> event .-target .-value))

(def color-names
  {:red "red"
   :blue "blue"})

(def test-actions
  [{:wood 1}
   {:stone 1
    :corn 1}
   {:gold 1
    :corn 2}
   {:skull 1}
   {:gold 1
    :stone 1
    :corn 2}
   :free
   :free])

(defn corn-cost-labels
  [cx cy r teeth]
  (for [index (range 0 (- teeth 2))]
    (let [spacing (/ 360 teeth)
          deg (* index spacing)
          offset (/ spacing 2)
          text-x (+ cx (* r 0.05))
          text-y (+ cy (* r 1.15))
          transform1 (str "rotate(" (+ deg offset) " " cx " " cy ")")
          transform2 (str transform1 " rotate(180 " text-x " " text-y ")")]
      ^{:key index}
      [:g
        [:circle {:style {:fill "yellow"}
                  :r (/ r 7)
                  :cx cx
                  :cy (+ cy (* r 1.2))
                  :transform transform1}]
        [:text {:x text-x
                :y text-y
                :style {:stroke "black"
                        :fill "black"}
                :font-size 11
                :transform transform2}
         (str index)]])))

(defn action-labels
  [cx cy r teeth actions]
  [:g
    [:circle {:style {:fill "green"}
              :r (* r 1.85)
              :cx cx
              :cy cy}]
    [:circle {:style {:fill "white"}
              :r (* r 1.4)
              :cx cx
              :cy cy}]
    ;; WHITE SEPARATORS
    (for [tooth (range teeth)
          :let [width (* r 0.1)
                deg (* tooth (/ 360 teeth))]]
      ^{:key tooth}
      [:rect {:x (- cx (/ width 2))
              :y (+ cy (/ r 3))
              :width width
              :height (* r 1.6)
              :style {:fill "white"}
              :transform (str "rotate(" deg " " cx " " cy ")")}])
    ;; BLOCK OFF FINAL 3 LABELS
    (for [tooth (range 3)
          :let [width (* r 0.15)
                space (/ 360 teeth)
                deg (- (* tooth space) (* 2 space))]]
      (for [block-num (range 10)]
        ^{:key (str tooth "-" block-num)}
        [:rect {:x (- cx width)
                :y (+ cy (/ r 3))
                :width width
                :height (* r 1.6)
                :style {:fill "white"}
                :transform (str "rotate(" (+ deg (* block-num 3.5)) " " cx " " cy ")")}]))
    (map-indexed (fn [index action]
                   (let [x cx
                         y (+ cy (* r 1.56))
                         spacing (/ 360 teeth)
                         offset (/ spacing 2)
                         deg (+ (* (+ index 1) spacing) offset)
                         first-transform (str "rotate(" deg " " cx " " cy ")")
                         second-transform (str "rotate(" 180 " " x " " y ")")
                         transform (str first-transform " " second-transform)]
                     [:text {:x x
                             :y y
                             :font-size 12
                             :text-anchor "middle"
                             :transform transform}
                       "poops"]))
      actions)])

(defn worker-slots
  [cx cy r teeth workers on-click]
  [:g
    (map-indexed
      (fn [index worker]
        (let [spacing (/ 360 teeth)
              deg (* index spacing)
              offset (/ spacing 2)
              transform (str "rotate(" (+ deg offset) " " cx " " cy ")")
              color (or (get color-names worker) "white")]
          ^{:key index}
          [:circle {:style {:fill color}
                    :on-click #(on-click index)
                    :r (/ r 5)
                    :cx (+ cx 0)
                    :cy (+ cy (* r 0.75))
                    :transform transform}]))
      workers)])

(defn gear-el
  [{:keys [cx cy r teeth rotation workers on-worker-click
           tooth-height-factor tooth-width-factor]}]
  [:g
    (action-labels cx cy r teeth test-actions)
    (corn-cost-labels cx cy r teeth)
    [:g {:transform (str "rotate(" (if rotation rotation 0) " " cx " " cy ")")}
      [:circle {:cx cx
                :cy cy
                :r r}]
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
                :transform (str "rotate(" deg " " cx " " cy ")")}])
      (if workers
        (worker-slots cx cy r teeth workers on-worker-click))]])

(defcard-rg gear-creator
  (fn [data _]
    (let [{:keys [size teeth tooth-width-factor tooth-height-factor]} @data
          set #(swap! data assoc %1 %2)]
      [:div {:class "ui segment"}
       [:div {:class "ui grid"}
        [:div {:class "six wide column"}
         [:div {:class "ui form"}
          [:div {:class "field"}
           [:div {:class "label"} "Size"]
           [:input {:type "range"
                    :value size
                    :min 100, :max 200
                    :on-change #(set :size (e->val %))}]]
          [:div {:class "field"}
           [:div {:class "label"} "Teeth"]
           [:input {:type "range"
                    :value teeth
                    :min 10, :max 26
                    :on-change #(set :teeth (e->val %))}]]
          [:div {:class "field"}
           [:div {:class "label"} "Tooth Width Factor"]
           [:input {:type "range"
                    :value tooth-width-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-width-factor (e->val %))}]]
          [:div {:class "field"}
           [:div {:class "label"} "Tooth Height Factor"]
           [:input {:type "range"
                    :value tooth-height-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-height-factor (e->val %))}]]]]
        [:div {:class "ten wide colum"}
         [:svg {:width (* size 5)
                :height (* size 5)}
          [gear-el {:cx (* size 2)
                    :cy (* size 2)
                    :r size
                    :teeth teeth
                    :tooth-width-factor tooth-width-factor
                    :tooth-height-factor tooth-height-factor}]]]]]))
  (rg/atom {:size 75
            :teeth 12
            :tooth-width-factor 1
            :tooth-height-factor 1.1})
  {:inspect-data true})

(defn worker-gear
  [{:keys [workers on-worker-click]}]
  [:center
    [:svg {:width 300 :height 300}
      [gear-el {:cx 150
                :cy 150
                :r 75
                :teeth 10
                :tooth-height-factor 1.15
                :tooth-width-factor 0.75
                :workers workers
                :on-worker-click on-worker-click}]]])

(def gear-rotation-atom (rg/atom 0))

(defn worker-gear-spin-test
  [{:keys [workers actions on-worker-click]}]
  (let [rotation-spring (anim/spring gear-rotation-atom)]
    (fn []
      [:center
        [:button {:on-click #(swap! gear-rotation-atom + (/ 360 10))}
          "Spin the gear! (turn: " (str (/ @gear-rotation-atom 36)) ")"]
        [:svg {:width 300 :height 300}
          [gear-el {:cx 150
                    :cy 150
                    :r 75
                    :teeth 10
                    :tooth-height-factor 1.15
                    :tooth-width-factor 0.75
                    :workers workers
                    :rotation @rotation-spring
                    :on-worker-click on-worker-click}]]])))

(defcard-rg worker-gear-spin-test
  [worker-gear-spin-test {:workers [:blue nil :blue :red nil nil :red nil nil nil]}])
