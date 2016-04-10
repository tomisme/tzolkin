(ns tzolkin.game
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as rg]
   [tzolkin.spec  :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.art   :as art]
   [tzolkin.utils :refer [log]]))

(defn worker-gears-wrapper
  [es-atom re-state save]
  (let [data (into {}
               (for [[gear _] (:gears spec)]
                 [gear
                  {:on-worker-click (fn [slot]
                                      (if save
                                        (save (logic/add-event @es-atom [:remove-worker {:gear gear
                                                                                         :slot slot}]))
                                        (swap! es-atom logic/add-event [:remove-worker {:gear gear
                                                                                        :slot slot}])))
                   :on-center-click (fn []
                                      (if save
                                        (save (logic/add-event @es-atom [:place-worker {:gear gear}]))
                                        (swap! es-atom logic/add-event [:place-worker {:gear gear}])))
                   :teeth (get-in spec [:gears gear :teeth])
                   :workers (get-in @re-state [:gears gear])
                   :rotation (* (/ 360 (get-in spec [:gears gear :teeth])) (:turn @re-state))
                   :actions (get-in spec [:gears gear :actions])}]))]
    [art/gear-layout-el data]))

(defn status-bar-wrapper
  [es-atom re-state save]
  (let [on-end-turn #(if save
                       (save (logic/add-event @es-atom [:end-turn]))
                       (swap! es-atom logic/add-event [:end-turn]))
        on-start-game #(if save
                         (save (logic/add-event @es-atom [:start-game {:test? false}]))
                         (swap! es-atom logic/add-event [:start-game {:test? false}]))
        on-decision (fn [option-index decision]
                      (if save
                        (save (logic/add-event @es-atom [:choose-option {:index option-index
                                                                         :decision decision}]))
                        (swap! es-atom logic/add-event [:choose-option {:index option-index
                                                                        :decision decision}])))
        on-trade (fn [trade]
                   (if save
                     (save (logic/add-event @es-atom [:make-trade {:trade trade}]))
                     (swap! es-atom logic/add-event [:make-trade {:trade trade}])))
        on-stop-trading #(if save
                           (save (logic/add-event @es-atom [:stop-trading]))
                           (swap! es-atom logic/add-event [:stop-trading]))
        ;; TODO
        on-add-player #(log %)]
    (fn []
      (art/status-bar-el @re-state
                         on-decision
                         on-trade
                         on-stop-trading
                         on-end-turn
                         on-start-game
                         on-add-player))))

(defn game-log-wrapper
  [es-atom save]
  (let [on-es-reset (fn [index]
                      (if save
                        (save (logic/reset-es @es-atom index))
                        (swap! es-atom logic/reset-es index)))]
    (rg/create-class
     {:component-did-update #(art/scroll-log-down!)
      :reagent-render
       (fn []
         (art/game-log-el {:stream @es-atom
                           :on-es-reset on-es-reset}))})))

(defn fb-conn-indicator-wrapper
  [local-state-atom]
  (let [connected? (reaction (:fb-connected? @local-state-atom))]
    (fn []
      (art/fb-conn-indicator-el @connected?))))

(defn temples-wrapper
  [es-atom re-state]
  (art/temples-el @re-state))

(def new-game-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom" :color :blue}]])

(def test-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom" :color :blue}]
   [:give-stuff {:pid 0 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:give-stuff {:pid 1 :k :materials :changes {:corn 99 :wood 99 :stone 99 :gold 99}}]
   [:start-game {:test? true}]
   [:place-worker {:gear :uxe}]
   [:place-worker {:gear :uxe}]
   [:place-worker {:gear :uxe}]
   [:end-turn]
   [:place-worker {:gear :yax}]
   [:place-worker {:gear :yax}]
   [:end-turn]])

(defn board
  [es-atom local-state-atom save]
  (let [re-state (reaction (logic/current-state @es-atom))]
    [:div.ui.grid {:style {:margin 0}}
     [:div.five.wide.column
       [status-bar-wrapper es-atom re-state save]
       [game-log-wrapper es-atom save]
       [:button.ui.button {:on-click #(save (logic/gen-es test-events))}
         "test events"]
       [:button.ui.button {:on-click #(save (logic/gen-es new-game-events))}
         "new 2p game"]
       [:button.ui.button {:on-click #(save (logic/gen-es [[:new-game]]))}
         "new empty game"]
       [:button.ui.button {:on-click #(save (logic/gen-es nil))}
         "nil state"]
       [fb-conn-indicator-wrapper local-state-atom]]
     [:div.seven.wide.column {:style {:padding-left 0}}
      [worker-gears-wrapper es-atom re-state save]]
       ; (into [:div]
       ;   (for [[k _] (:gears spec)]
       ;     [worker-gear-wrapper es-atom re-state k save]))]
     [:div.four.wide.column
       [temples-wrapper es-atom re-state]
       [art/tech-tracks-el]]]))
