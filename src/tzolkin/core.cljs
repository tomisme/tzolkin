(ns tzolkin.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as rg]
   [matchbox.core :as m]
   [tzolkin.game :as game]
   [tzolkin.art :as art]
   [tzolkin.db :as db]
   [tzolkin.logic :as logic]
   [tzolkin.art :as art]
   [tzolkin.spec :refer [spec]]
   [tzolkin.utils :refer [log]]
   [devtools.core :as devtools]))

(devtools/install!)

(def es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(def local-state-atom
  (rg/atom {:fb-connected? false}))

(db/setup-game-listener es-atom)

(db/setup-connection-listener local-state-atom)

(defn app-container
  []
  (game/board es-atom local-state-atom db/save))

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render-component [app-container] app-node)))

(main)
