(ns tzolkin.game
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.art   :as art]))

(defn worker-gear-wrapper
  [state-atom gear]
  (let [pid (get-in @state-atom [:active :pid])
        on-worker-click (fn [slot]
                          (swap! state-atom logic/remove-worker pid gear slot))
        on-center-click (fn []
                          (swap! state-atom logic/place-worker pid gear))
        teeth (get-in spec [:gears gear :teeth])]
    (art/worker-gear {:workers (get-in @state-atom [:gears gear])
                      :gear gear
                      :rotation (* (/ 360 teeth) (:turn @state-atom))
                      :actions (get-in spec [:gears gear :actions])
                      :on-center-click on-center-click
                      :on-worker-click on-worker-click})))

(defn status-bar-wrapper
  [state-atom]
  (let [on-decision (fn [option-index]
                      (swap! state-atom logic/handle-decision option-index))]
    (art/status-bar @state-atom on-decision)))

(defn board
  [state-atom]
  [:div
    [:p
      [:button {:on-click #(swap! state-atom logic/end-turn)}
        "End Turn"]]
    (status-bar-wrapper state-atom)
    (art/temples-el @state-atom)
    (for [[k _] (:gears spec)]
      (worker-gear-wrapper state-atom k))])
