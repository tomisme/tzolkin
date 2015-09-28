(ns tzolkin.core
  (:require
   [reagent.core :as rg]
   [tzolkin.util :refer [e->val]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(defn app-container
  []
  [:div {:class "ui button"} "Here be the app (eventually)."])

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render [app-container] app-node)))

(main)

(defcard-doc
  "Tzolk'in is fun game.

  Let's build a version of it in clojurescript, using:

   - devcards!
   - reagent
   - re-frame?
   - semantic-ui
   - svg elements")

(defcard first-board
  "Here's our first test board. It's just a button that increments the turn number.

  The game logic is simply:

  ```
  (defn next-state
    [prev-state]
    (-> prev-state
      (update :turn inc)))
  ```

  With the magic of devcards, our game state can be inspected and time travelled!"
  (dc/reagent
   (fn [state _]
     (let [next-state (fn [prev-state]
                        (-> prev-state
                          (update :turn inc)))]
       [:div {:class "ui segment"}
        [:div {:class "ui button"
               :onClick (fn [] (swap! state next-state state))}
         "Current Turn: " (:turn @state)]])))
  {:turn 1}
  {:inspect-data true :history true})

(defcard-doc
  "##Gears
  The spinning gears are a once of the coolest mechanics in Tzolk'in.
  We're going to make them out of standard SVG elements.

  For reference, here are the different gears and their number of teeth:

  * Calendar Gear (26)
  * Chichen Itza (13)
  * Uxmal (10)
  * Tikal (10)
  * Yaxchilan (10)
  * Palenque (10)")

(defn gear-el
  [{:keys [cx cy r teeth tooth-width-factor tooth-height-factor]}]
  [:g
   [:circle {:cx cx
             :cy cy
             :r r}]
   (for [tooth (range teeth)
         :let [width (* r 0.35 tooth-width-factor)
               deg (* tooth (/ 360 teeth))]]
     ^{:key tooth} [:rect {:x (- cx (/ width 2))
                           :y cy
                           :rx (/ width 4)
                           :ry (/ width 4)
                           :width width
                           :height (+ (/ r 3) (* r 0.7 tooth-height-factor))
                           ;:style {:fill "red"}
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
