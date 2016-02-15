(ns tzolkin.game
  (:require
   [tzolkin.logic :as logic]
   [tzolkin.spec  :as spec]
   [tzolkin.art   :as art]))

(defn worker-gear
  [{:keys [gear workers on-worker-click on-center-click actions rotation]}]
  ^{:key gear}
  [:svg {:width 340 :height 340}
    [art/gear-el {:cx 170
                  :cy 170
                  :r 85
                  :rotation rotation
                  :teeth (get-in spec/game [:gears gear :teeth])
                  :tooth-height-factor 1.15
                  :tooth-width-factor 0.75
                  :workers workers
                  :gear gear
                  :actions actions
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click}]])

(defn worker-gear-wrapper
  [state gear]
  (let [player-id (get-in @state [:active :player-id])
        on-worker-click (fn [slot]
                          (swap! state logic/remove-worker player-id gear slot))
        on-center-click (fn []
                          (swap! state logic/place-worker player-id gear))
        teeth (get-in spec/game [:gears gear :teeth])]
    (worker-gear {:workers (get-in @state [:gears gear])
                  :gear gear
                  :rotation (* (/ 360 teeth) (:turn @state))
                  :actions (get-in spec/game [:gears gear :actions])
                  :on-center-click on-center-click
                  :on-worker-click on-worker-click})))
