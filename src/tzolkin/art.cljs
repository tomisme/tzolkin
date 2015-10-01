(ns tzolkin.art
  (:require
   [reagent.core :as rg]
   [tzolkin.logic])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(defcard-doc
  "##Gears
  The spinning gears are a once of the coolest mechanics in Tzolk'in.
  We're going to make them out of SVG elements.")

(defn e->val
  [event]
  (-> event .-target .-value))

(defn gear-el
  [{:keys [cx cy r teeth tooth-width-factor tooth-height-factor rotation style]}]
  [:g {:transform (str "rotate(" (if rotation rotation 0) " " cx " " cy ")")}
   [:circle {:cx cx
             :cy cy
             :r r
             :style style}]
   (for [tooth (range teeth)
         :let [width (* r 0.35 tooth-width-factor)
               deg (* tooth (/ 360 teeth))]]
     ^{:key tooth} [:rect {:x (- cx (/ width 2))
                           :y cy
                           :rx (/ width 4)
                           :ry (/ width 4)
                           :width width
                           :height (+ (/ r 3) (* r 0.7 tooth-height-factor))
                           :style style
                           :transform (str "rotate(" deg " " cx " " cy ")")}])])

(defcard gear-creator
  (dc/reagent
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
                     :on-change #(set :tooth-height-factor (e->val %))}]] ]]
         [:div {:class "ten wide colum"}
          [:svg {:width (* size 3)
                 :height (* size 3)}
           [gear-el {:cx (* size 1.5)
                     :cy (* size 1.5)
                     :r size
                     :teeth teeth
                     :tooth-width-factor tooth-width-factor
                     :tooth-height-factor tooth-height-factor}]]]]])))
  (rg/atom {:size 75
            :teeth 12
            :tooth-width-factor 1
            :tooth-height-factor 1})
  {:inspect-data true})

(def gears-atom (rg/atom {:rotation 0}))

(defcard gears-1
  (dc/reagent
   (fn [data _]
     [:center
      [:svg {:width 350
             :height 250}
       [gear-el {:cx 100
                 :cy 155
                 :r 75
                 :teeth 13
                 :tooth-height-factor 1.25
                 :tooth-width-factor 0.75
                 :rotation (* -1 (:rotation @data)) }]
       [gear-el {:cx 256
                 :cy 95
                 :r 75
                 :teeth 13
                 :tooth-height-factor 1.25
                 :tooth-width-factor 0.75
                 :rotation (:rotation @data)}]]]))
  gears-atom)

(defcard gears-2
  (dc/reagent
   (fn [data _]
     [:center
      [:svg {:width 600
             :height 500}
       [gear-el {:cx 300
                 :cy 230
                 :r 115
                 :teeth 26
                 :tooth-height-factor 1.12
                 :tooth-width-factor 0.35
                 :rotation (* -1 (+ 14 (:rotation @data)))
                 :style {:fill "grey"}
                 }]
       [gear-el {:cx 144 ;; CHICHEN ITZA
                 :cy 338
                 :r 60
                 :teeth 13
                 :tooth-height-factor 1.32
                 :tooth-width-factor 0.64
                 :rotation (+ 28 (* (/ 26 13) (:rotation @data)))
                 :style {:fill "#5882FA"}
                 }]
       [gear-el {:cx 155
                 :cy 130
                 :r 45
                 :teeth 10
                 :tooth-height-factor 1.32
                 :tooth-width-factor 0.75
                 :rotation (* (/ 26 10) (:rotation @data))
                 :style {:fill "#74DF00"}
                 }]
       [gear-el {:cx 350
                 :cy 70
                 :r 45
                 :teeth 10
                 :tooth-height-factor 1.32
                 :tooth-width-factor 0.75
                 :rotation (* (/ 26 10) (:rotation @data))
                 :style {:fill "#F7BE81"}
                 }]
       [gear-el {:cx 457
                 :cy 211
                 :r 45
                 :teeth 10
                 :tooth-height-factor 1.32
                 :tooth-width-factor 0.75
                 :rotation (* (/ 26 10) (:rotation @data))
                 :style {:fill "#B43104"}
                 }]
       [gear-el {:cx 376
                 :cy 369
                 :r 45
                 :teeth 10
                 :tooth-height-factor 1.32
                 :tooth-width-factor 0.75
                 :rotation (* (/ 26 10) (:rotation @data))
                 :style {:fill "#FFFF00"}
                 }]]]))
  gears-atom)

(js/setInterval #(swap! gears-atom update :rotation inc) 100)
