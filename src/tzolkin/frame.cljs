(ns tzolkin.frame
  (:require
   [re-frame.core :as rf]
   [tzolkin.logic :as logic]))


(rf/reg-event-db
  :init
  (fn [_ _]
    {:es (logic/gen-es [[:new-game]])}))


(rf/reg-event-db
  :do
  (fn [db [_ x]]
    {:es (logic/add-event (:es db) x)}))


(rf/reg-event-db
  :reset-es
  (fn [db [_ idx]]
    {:es (logic/reset-es (:es db) idx)}))


(rf/reg-sub
  :es
  (fn [db _]
    (:es db)))


(rf/reg-sub
  :game-state
  (fn [db _]
    (logic/current-state (:es db))))


(defn init-app-state!
  []
  (rf/dispatch [:init]))
