(ns tzolkin.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as rg]
   [matchbox.core :as m]
   [tzolkin.game :as game]
   [tzolkin.db :as db]
   [tzolkin.logic :as logic]
   [tzolkin.art :as art]
   [tzolkin.spec :refer [spec]]))

(def test-events
  [[:new-game]
   [:add-player {:name "Elisa" :color :red}]
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

(def es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(db/setup-game-listener es-atom)

(defn app-container
  []
  (let [re-state (reaction (logic/current-state @es-atom))]
    [:div.ui.grid {:style {:margin 0}}
     [:div.five.wide.column
       (game/end-turn-button-wrapper es-atom)
       [game/status-bar-wrapper es-atom re-state]
       [game/game-log es-atom]]
     [:div.seven.wide.column
       (into [:div]
         (for [[k _] (:gears spec)]
           [game/worker-gear-wrapper es-atom k]))]
     [:div.four.wide.column
       [art/temples-el @re-state]
       [:button.ui.button {:on-click #(db/save (logic/reduce-event-stream {} test-events))}
         "test events"]
       [:button.ui.button {:on-click #(db/save (logic/reduce-event-stream {} [[:new-game]]))}
         "new game"]
       [:button.ui.button {:on-click #(db/save (logic/reduce-event-stream {} nil))}
         "blank state"]]]))

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render-component [app-container] app-node)))

(main)
