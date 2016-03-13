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
   ; [devcards.core] ;; TODO remove for prod
   ; [devtools.core :as devtools]))

; (devtools/install!)

(def es-atom
  (rg/atom (logic/reduce-event-stream {} [[:new-game]])))

(db/setup-game-listener es-atom)

(db/setup-connection-listener)

(defn app-container
  []
  (game/board es-atom db/save))

(defn main []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render-component [app-container] app-node)))

(main)
