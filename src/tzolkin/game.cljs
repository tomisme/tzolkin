(ns tzolkin.game
  (:require
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.art   :as art]))

(defn worker-gear-wrapper
  [state gear]
  (let [player-id (get-in @state [:active :player-id])
        on-worker-click (fn [slot]
                          (swap! state logic/remove-worker player-id gear slot))
        on-center-click (fn []
                          (swap! state logic/place-worker player-id gear))
        teeth (get-in spec [:gears gear :teeth])]
    (art/worker-gear {:workers (get-in @state [:gears gear])
                      :gear gear
                      :rotation (* (/ 360 teeth) (:turn @state))
                      :actions (get-in spec [:gears gear :actions])
                      :on-center-click on-center-click
                      :on-worker-click on-worker-click})))

(defn status-bar-wrapper
  [state]
  (let [on-decision (fn [option-index]
                      (swap! state logic/handle-decision option-index))]
    (art/status-bar @state on-decision)))

(defn board
  [state]
  [:div
    [:p
      [:button {:on-click #(swap! state logic/end-turn)}
        "End Turn"]]
    (status-bar-wrapper state)
    (art/god-tracks @state)
    (for [[k _] (:gears spec)]
      (worker-gear-wrapper state k))])
