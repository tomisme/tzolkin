(ns tzolkin.core
  (:require
   [reagent.core :as reagent]))


(defn render-app []
  (if-let [app-node (.getElementById js/document "app")]
    (reagent/render-component [:div "Hi"] app-node)))


(render-app)
