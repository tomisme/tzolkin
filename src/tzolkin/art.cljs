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

(defn gear-el
  [{:keys [cx cy r teeth rotation style workers on-worker-click
           tooth-height-factor tooth-width-factor]}]
  [:g
    (if true ; replace with check for big gear
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
    [:g {:transform (str "rotate(" (if rotation rotation 0) " " cx " " cy ")")}
      [:circle {:cx cx
                :cy cy
                :r r
                :style style}]
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
                :style style
                :transform (str "rotate(" deg " " cx " " cy ")")}])
      (if workers
        (map-indexed (fn [index worker]
                       (let [spacing (/ 360 teeth)
                             deg (* index spacing)
                             offset (/ spacing 2)
                             transform (str "rotate(" (+ deg offset) " " cx " " cy ")")
                             color (or (get color-names worker) "white")]
                         ^{:key index}
                         [:circle {:style {:fill color}
                                   :on-click #(on-worker-click index)
                                   :r (/ r 5)
                                   :cx (+ cx 0)
                                   :cy (+ cy (* r 0.75))
                                   :transform transform}]))
          workers))]])

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
         [:svg {:width (* size 3)
                :height (* size 3)}
          [gear-el {:cx (* size 1.5)
                    :cy (* size 1.5)
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
    [:svg {:width 300 :height 200}
      [gear-el {:cx 150
                :cy 100
                :r 75
                :teeth 10
                :tooth-height-factor 1.15
                :tooth-width-factor 0.75
                :workers workers
                :on-worker-click on-worker-click}]]])

(def gear-rotation-atom (rg/atom 0))

(defn worker-gear-spin-test
  [{:keys [workers on-worker-click]}]
  (let [rotation-spring (anim/spring gear-rotation-atom)]
    (fn []
      [:center
        [:button {:on-click #(swap! gear-rotation-atom + (/ 360 10))}
          "Spin the gear! (turn: " (str (/ @gear-rotation-atom 36)) ")"]
        [:svg {:width 300 :height 200}
          [gear-el {:cx 150
                    :cy 100
                    :r 75
                    :teeth 10
                    :tooth-height-factor 1.15
                    :tooth-width-factor 0.75
                    :workers workers
                    :rotation @rotation-spring
                    :on-worker-click on-worker-click}]]])))

(defcard-rg worker-gear-spin-test
  [worker-gear-spin-test {:workers [:blue nil :blue :red nil nil :red nil]}])
