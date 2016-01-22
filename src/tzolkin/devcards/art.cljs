(ns tzolkin.devcards.art
  (:require
   [reagent.core :as rg]
   [timothypratley.reanimated.core :as anim]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

 (defcard-doc
   "#Art
    The game board is made up HTML and SVG elements.")

(deftest transform-str-test
  "`transform-str` takes any number of (supported) svg transform definitions
   and returns a string for use as an svg element's `transform` attribute
   (docs at
   [mdn](https://developer.mozilla.org/en/docs/Web/SVG/Attribute/transform))."
  (testing
    "rotate"
    (is (= (art/transform-str [:rotate {:deg 90}]) "rotate(90)"))
    (is (= (art/transform-str [:rotate {:deg 55 :x 10 :y 10}]
                          [:rotate {:deg 10 :x 1 :y 1}])
           "rotate(55 10 10)rotate(10 1 1)"))))

(defcard-rg symbols-examples
  [:div (for [size '(16)]
          [:div {:style {:font-size size}}
            (for [[k v] art/symbols]
              (str (name k) ": " v ", "))])])

(deftest resources-str-test
  "`resources-str` takes a map of resources and the amount of each and returns
   a string of symbols."
   (testing
     (is (= (art/resources-str {:wood 1 :stone 1 :gold 2 :corn 3 :skull 1})
            "🌲🗿🌕🌕🌽🌽🌽💀"))))

(defcard-doc
  "##Gears
   The spinning gears are a one of the coolest mechanics in Tzolk'in. They're
   also the main way that players interact with the game.")

 (defcard-rg gear-creator
   (fn [data _]
     (let [{:keys [size teeth tooth-width-factor tooth-height-factor]} @data
           set #(swap! data assoc %1 %2)]
       ;; TODO: try using dot syntax for div classes
       [:div {:class "ui segment"}
        [:div {:class "ui grid"}
         [:div {:class "six wide column"}
          [:div {:class "ui form"}
           [:div {:class "field"}
            [:div {:class "label"} "Size"]
            [:input {:type "range"
                     :value size
                     :min 100, :max 200
                     :on-change #(set :size (art/e->val %))}]]
           [:div {:class "field"}
            [:div {:class "label"} "Teeth"]
            [:input {:type "range"
                     :value teeth
                     :min 10, :max 26
                     :on-change #(set :teeth (art/e->val %))}]]
           [:div {:class "field"}
            [:div {:class "label"} "Tooth Width Factor"]
            [:input {:type "range"
                     :value tooth-width-factor
                     :min 0.1, :max 2
                     :step 0.1
                     :on-change #(set :tooth-width-factor (art/e->val %))}]]
           [:div {:class "field"}
            [:div {:class "label"} "Tooth Height Factor"]
            [:input {:type "range"
                     :value tooth-height-factor
                     :min 0.1, :max 2
                     :step 0.1
                     :on-change #(set :tooth-height-factor (art/e->val %))}]]]]
         [:div {:class "ten wide colum"}
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

(def gear-rotation-atom (rg/atom 0))

 (defn worker-gear-spin-test
   [{:keys [workers actions on-worker-click]}]
   (let [rotation-spring (anim/spring gear-rotation-atom)]
     (fn []
       [:center
         [:button {:on-click #(swap! gear-rotation-atom + (/ 360 10))}
           "Spin the gear! (turn: " (str (/ @gear-rotation-atom 36)) ")"]
         [:svg {:width 300 :height 300}
           [art/gear-el {:cx 150
                     :cy 150
                     :r 75
                     :teeth 10
                     :tooth-height-factor 1.15
                     :tooth-width-factor 0.75
                     :workers workers
                     :rotation @rotation-spring
                     :on-worker-click on-worker-click}]]])))

(defcard-rg worker-gear-spin-test
  [worker-gear-spin-test
    {:workers [:blue nil :blue :red nil nil :red nil nil nil]}])
