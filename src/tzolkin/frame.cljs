(ns tzolkin.frame
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [tzolkin.rules :as rules]))


(rf/reg-event-db
  :init
  (fn [_ _]
    {:es (rules/gen-es [[:new-game]])}))


(rf/reg-event-db
  :do
  (fn [db [_ x]]
    {:es (rules/add-event (:es db) x)}))


(rf/reg-event-db
  :reset-es
  (fn [db [_ idx]]
    {:es (rules/reset-es (:es db) idx)}))


(rf/reg-sub
  :es
  (fn [db _]
    (:es db)))


(rf/reg-sub
  :game-state
  (fn [db _]
    (rules/current-state (:es db))))


(defn init-app-state!
  []
  (rf/dispatch [:init]))


#_(defn render-app []
    (if-let [app-node (.getElementById js/document "app")]
      (reagent/render-component [:div "Hi"] app-node)))


#_(render-app)
