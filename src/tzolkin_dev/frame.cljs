(ns tzolkin-dev.frame
  (:require
   [reagent.core :as rg]
   [re-frame.core :as rf]
   [tzolkin.art :as art]
   [tzolkin.rules :as rules]
   [tzolkin.seed :refer [seed]]
   [tzolkin.frame])
  (:require-macros
   [devcards.core :refer [defcard-rg]]))


(def new-2p-game-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom" :color :blue}]])


(def new-4p-game-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
   [:add-player {:name "Tom"   :color :blue}]
   [:add-player {:name "Alice" :color :orange}]
   [:add-player {:name "Bob"  :color :yellow}]])


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


(rf/reg-event-db :test-ev1
  (fn [_ _] {:es (rules/gen-es test-events)}))
(rf/reg-event-db :test-ev2
  (fn [_ _] {:es (rules/gen-es new-2p-game-events)}))
(rf/reg-event-db :test-ev3
  (fn [_ _] {:es (rules/gen-es new-4p-game-events)}))
(rf/reg-event-db :test-ev4
  (fn [_ _] {:es (rules/gen-es [[:new-game]])}))
(rf/reg-event-db :test-ev5
  (fn [_ _] {:es (rules/gen-es nil)}))


(defn test-run-el
  []
  [:div {:style {:display "flex"}}
   [:button.ui.button {:on-click #(rf/dispatch [:test-ev1])}
    "test events"]
   [:button.ui.button {:on-click #(rf/dispatch [:test-ev2])}
    "new 2p game"]
   [:button.ui.button {:on-click #(rf/dispatch [:test-ev3])}
    "new 4p game"]
   [:button.ui.button {:on-click #(rf/dispatch [:test-ev4])}
    "new empty game"]
   [:button.ui.button {:on-click #(rf/dispatch [:test-ev5])}
    "nil state"]])


(defn status-bar-component
  []
  (art/status-bar-el
    @(rf/subscribe [:game-state])
    #(rf/dispatch [:do [:choose-option
                        {:index %1
                         :decision %2}]])
    #(rf/dispatch [:do [:make-trade
                        {:trade %}]])
    #(rf/dispatch [:do [:stop-trading]])
    #(rf/dispatch [:do [:end-turn]])
    #(rf/dispatch [:do [:start-game
                        {:test? false}]])
    #(rf/dispatch [:do [:add-player %]])))


(defn game-log-component
  []
  (rg/create-class
   {:component-did-update #(art/scroll-log-down!)
    :reagent-render
    (fn []
      (art/game-log-el {:stream @(rf/subscribe [:es])
                        :on-es-reset #(rf/dispatch [:reset-es %])}))}))


(defn gears-component
  []
  (let [{:keys [jungle turn player-order players active
                starting-player-corn starting-player-space] :as game-state}
        @(rf/subscribe [:game-state])]

    [art/gear-layout-el
     (into {}
           (for [[gear _] (:gears seed)]
             [gear
              {:on-worker-click #(rf/dispatch [:do [:remove-worker {:gear gear
                                                                    :slot %}]])
               :on-center-click #(rf/dispatch [:do [:place-worker {:gear gear}]])
               :teeth (get-in seed [:gears gear :teeth])
               :workers (get-in game-state [:gears gear])
               :rotation (* turn (/ 360 (get-in seed [:gears gear :teeth])))
               :actions (get-in seed [:gears gear :actions])}]))
     jungle
     turn
     player-order
     players
     active
     #(rf/dispatch [:do [:end-turn]])
     #(rf/dispatch [:do [:take-starting]])
     starting-player-corn
     starting-player-space]))


(defn temples-component
  []
  (art/temples-el @(rf/subscribe [:game-state])))


(defn tech-tracks-component
  []
  (art/tech-tracks-el (:players @(rf/subscribe [:game-state]))))


(defn app-component
  []
  [:div {:style {:display "flex"}}
   [:div {:style {:margin "2rem"}}
    [status-bar-component]
    [game-log-component]
    (test-run-el)]
   [:div
    [gears-component]]
   [:div {:style {:margin "1rem"}}
    [temples-component]
    [tech-tracks-component]]])


(defcard-rg app-test
  [:div {:style {:position "absolute" :left 0 :top 20}}
   [app-component]]
  {}
  {:frame false})
