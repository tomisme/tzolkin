(ns tzolkin.core
  (:require
   [reagent.core :as rg]
   [tzolkin.art]
   [tzolkin.logic])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(defcard-doc
  "Tzolk'in is fun game.

  Let's build a version of it in clojurescript, using:

   - devcards!
   - reagent
   - re-frame?
   - semantic-ui
   - svg elements")

(defn app-container
  []
  [:div {:class "ui button"} "Here be the app (eventually)."])

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render [app-container] app-node)))

(main)
