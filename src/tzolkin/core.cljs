(ns tzolkin.core
  (:require
   [reagent.core :as rg]
   [tzolkin.game :as game]
   [tzolkin.devcards.art]
   [tzolkin.devcards.game]
   [tzolkin.devcards.spec]
   [tzolkin.devcards.tests]))

(defn app-container
  []
  [:div {:class "ui button"} "Here be the app (eventually)."])

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render [app-container] app-node)))

(main)
