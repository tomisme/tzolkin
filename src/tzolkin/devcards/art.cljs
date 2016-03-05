(ns tzolkin.devcards.art
  (:require
   [reagent.core :as rg]
   [timothypratley.reanimated.core :as anim]
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :as dev-game])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]))

(defcard-doc
  "#Art
  The game board is made up HTML and SVG elements.")

(defcard-rg symbols-examples
  [:div (for [size '(16 45)]
          [:div {:style {:font-size size}}
            (for [[k v] art/symbols]
              (str (name k) ": " v ", "))])])

(defcard-doc
  "#Gears
  The spinning gears are a one of the coolest mechanics in Tzolk'in. They're
  also the main way that players interact with the game.")

(defcard-rg gear-creator
  (fn [data _]
    (let [{:keys [size teeth tooth-width-factor tooth-height-factor]} @data
          set #(swap! data assoc %1 %2)]
      [:div.ui.segment
       [:div.ui.grid
        [:div.six.wide.column
         [:div.ui.form
          [:div.field
           [:div.label "Size"]
           [:input {:type "range"
                    :value size
                    :min 100, :max 200
                    :on-change #(set :size (art/e->val %))}]]
          [:div.field
           [:div.label "Teeth"]
           [:input {:type "range"
                    :value teeth
                    :min 10, :max 26
                    :on-change #(set :teeth (art/e->val %))}]]
          [:div.field
           [:div.label "Tooth Width Factor"]
           [:input {:type "range"
                    :value tooth-width-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-width-factor (art/e->val %))}]]
          [:div.field
           [:div.label "Tooth Height Factor"]
           [:input {:type "range"
                    :value tooth-height-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-height-factor (art/e->val %))}]]]]
        [:div.ten.wide.column
         [:svg {:width (* size 5)
                :height (* size 5)}
          [art/gear-el {:cx (* size 2)
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

(def spin-test-atom (rg/atom 0))

(defn spinning-worker-gear
  [{:keys [workers actions on-worker-click]}]
  (let [rotation-spring (anim/spring spin-test-atom)
        workers [:blue nil :blue :red nil nil :red nil nil nil]]
    (fn []
      [:svg {:width 300 :height 300}
        [art/gear-el {:cx 150
                      :cy 150
                      :r 75
                      :teeth 10
                      :tooth-height-factor 1.15
                      :tooth-width-factor 0.75
                      :workers workers
                      :gear :tik
                      :rotation @rotation-spring}]])))

(defcard-rg spinning-worker-gear-test
  [:div
    [:button {:on-click #(swap! spin-test-atom + (/ 360 10))}
      "Spin the gear!"]
    (for [[k v] (get spec :gears)]
      [:button (:name v)])
    [spinning-worker-gear]]
  spin-test-atom
  {:inspect-data true})

(defcard-doc "#Temples")

(defcard-rg temple-art
  (art/temples-el (dev-game/new-test-game {:players 2})))

(defcard-doc
  "#Buildings")

(def random-building
  (first (shuffle (:buildings spec))))

(defcard-rg single-building
  (art/building-card random-building nil false)
  random-building
  {:inspect-data true})

(defcard-rg all-buildings
  [:div.ui.cards
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div (art/building-card building nil false)])
      (:buildings spec))])
