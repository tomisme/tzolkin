(ns tzolkin.core
  (:require
   [reagent.core :as rg])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

(defn next-state
  [prev-state]
  (-> prev-state
    (update :turn inc)))

(defn app-container
  []
  [:div {:class "ui button"} "Here be the app (eventually)."])

(defcard test-board
  "Tzolk'in is fun game.

  Let's build a version of it in clojurescript, using:

   - devcards!
   - reagent
   - re-frame?
   - semantic-ui?

  Here's our first test board. It's just a button that increments the turn number.

  The game logic is currently:

  ```
  (defn next-state
    [prev-state]
    (-> prev-state
      (update :turn inc)))
  ```

  With the magic of devcards, the data behind it can be inspected and time travelled, whoa!"
  (dc/reagent
   (fn [data-atom _]
     [:div {:class "ui segment"}
      [:div {:class "ui button"
             :onClick (fn [] (swap! data-atom next-state data-atom))}
       "Current Turn: " (:turn @data-atom)]]))
  {:turn 1}
  {:inspect-data true :history true})

(defn main []
  (if-let [node (.getElementById js/document "app")]
    (rg/render [app-container] node)))

(main)
