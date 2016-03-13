(ns tzolkin.game
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.art   :as art]
   [tzolkin.utils :refer [log]]))

(defn worker-gear-wrapper
  [es-atom re-state gear]
  (let [on-worker-click (fn [slot]
                          (swap! es-atom logic/add-event [:remove-worker {:gear gear
                                                                          :slot slot}]))
        on-center-click (fn []
                          (swap! es-atom logic/add-event [:place-worker {:gear gear}]))
        teeth (get-in spec [:gears gear :teeth])]
    (fn []
      (art/worker-gear {:workers (get-in @re-state [:gears gear])
                        :gear gear
                        :rotation (* (/ 360 teeth) (:turn @re-state))
                        :actions (get-in spec [:gears gear :actions])
                        :on-center-click on-center-click
                        :on-worker-click on-worker-click}))))

(defn status-bar-wrapper
  [es-atom re-state]
  (let [on-decision (fn [option-index options]
                      (swap! es-atom logic/add-event [:choose-option {:index option-index
                                                                      :options options}]))]
    (fn []
      (art/status-bar-el @re-state on-decision))))

(defn end-turn-button-wrapper
  [es-atom]
  (let [on-end-turn #(swap! es-atom logic/add-event [:end-turn])]
    [:button.ui.button {:on-click on-end-turn}
     "Finish and Submit Turn"]))

(defn game-log
  [es-atom]
  (let [on-es-reset (fn [index]
                      (swap! es-atom logic/reset-es index))]
    (fn []
      (art/game-log-el {:stream @es-atom
                        :on-es-reset on-es-reset}))))

(defn temples-wrapper
  [re-state]
  (art/temples-el @re-state))

(defn test-board
  [es-atom]
  (let [re-state (reaction (logic/current-state @es-atom))]
    [:div
     (end-turn-button-wrapper es-atom)
     [status-bar-wrapper es-atom re-state]
     [art/temples-el @re-state]
     (into [:div]
       (for [[k _] (:gears spec)]
         [worker-gear-wrapper es-atom re-state k]))
     [game-log es-atom]]))

(def test-events
  [[:new-game]
   [:add-player {:name "Aaron" :color :red}]
   [:add-player {:name "Tom" :color :blue}]
   [:give-stuff {:pid 0 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:give-stuff {:pid 1 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:start-game]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:place-worker {:pid 0 :gear :uxe}]
   [:end-turn]
   [:place-worker {:pid 1 :gear :yax}]
   [:place-worker {:pid 1 :gear :yax}]
   [:end-turn]])

(defn board
  [es-atom save]
  (let [re-state (reaction (logic/current-state @es-atom))]
    [:div.ui.grid {:style {:margin 0}}
     [:div.five.wide.column
       (end-turn-button-wrapper es-atom)
       [status-bar-wrapper es-atom re-state]
       [game-log es-atom]]
     [:div.seven.wide.column
       (into [:div]
         (for [[k _] (:gears spec)]
           [worker-gear-wrapper es-atom re-state k]))]
     [:div.four.wide.column
       [temples-wrapper re-state]
       [:button.ui.button {:on-click #(save (logic/reduce-event-stream {} test-events))}
         "test events"]
       [:button.ui.button {:on-click #(save (logic/reduce-event-stream {} [[:new-game]]))}
         "new game"]
       [:button.ui.button {:on-click #(save (logic/reduce-event-stream {} nil))}
         "blank state"]]]))
