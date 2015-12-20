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
  [{:keys [cx cy r teeth rotation style workers
           tooth-height-factor tooth-width-factor]}]
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
                                 :r (/ r 5)
                                 :cx (+ cx 0)
                                 :cy (+ cy (* r 0.75))
                                 :transform transform}]))
        workers))])

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

(defn worker-gear-test
  [{:keys [workers]}]
  [:center
    [:svg {:width 300 :height 200}
      [gear-el {:cx 150
                :cy 100
                :r 75
                :teeth 10
                :tooth-height-factor 1.15
                :tooth-width-factor 0.75
                :workers workers}]]])

(defcard-rg worker-gear-test
  [worker-gear-test {:workers [:blue nil :blue :red nil nil :red nil]}])

(defn spring-gear
  [rotation]
  (let [rotation-spring (anim/spring rotation)]
    (fn render-spring-gear []
      [:center
       [:svg {:width 300 :height 200
              :on-click #(swap! rotation + 30)}
        [gear-el {:cx 150
                  :cy 100
                  :r 50
                  :teeth 10
                  :tooth-height-factor 1.25
                  :tooth-width-factor 0.75
                  :rotation @rotation-spring}]
        [:text {:x 114
                :y 100
                :style {:stroke "#3ADF00"
                        :fill "#3ADF00"}
                :font-size 18}
         "Click Me!"]]])))

(defcard-rg spring-test
  "Spinning a gear with [reanimated](https://github.com/timothypratley/reanimated)."
  (fn [data _] [spring-gear data])
  (rg/atom 0)
  {:inspect-data true})
