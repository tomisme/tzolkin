(ns tzolkin.game
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.art   :as art]
   [tzolkin.utils :refer [log]]))

(defn worker-gear-wrapper
  [es-atom gear]
  (fn []
    (let [state (logic/current-state @es-atom)
          pid (get-in state [:active :pid])
          on-worker-click (fn [slot]
                            (swap! es-atom logic/add-event [:remove-worker {:pid pid
                                                                            :gear gear
                                                                            :slot slot}]))
          on-center-click (fn []
                            (swap! es-atom logic/add-event [:place-worker {:pid pid
                                                                           :gear gear}]))
          teeth (get-in spec [:gears gear :teeth])]
      (art/worker-gear {:workers (get-in state [:gears gear])
                        :gear gear
                        :rotation (* (/ 360 teeth) (:turn state))
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
     "End Turn"]))

(defn game-log
  [es-atom]
  (let [on-es-reset (fn [index]
                      (swap! es-atom logic/reset-es index))]
    (fn []
      (art/game-log-el {:stream @es-atom
                        :on-es-reset on-es-reset}))))

(defn board
  [es-atom]
  (let [re-state (reaction (logic/current-state @es-atom))]
    [:div
     (end-turn-button-wrapper es-atom)
     [status-bar-wrapper es-atom re-state]
     [art/temples-el @re-state]
     (into [:div]
       (for [[k _] (:gears spec)]
         [worker-gear-wrapper es-atom k]))
     [game-log es-atom]]))
