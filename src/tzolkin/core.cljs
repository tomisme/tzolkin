(ns tzolkin.core
  (:require
   [reagent.core :as rg]
   [tzolkin.game :as game]
   [tzolkin.db :as db]
   [tzolkin.logic :as logic]
   [tzolkin.utils :refer [log]]
   [devtools.core :as devtools]))

(devtools/install! [:custom-formatters :sanity-hints])

(def es-atom
  (rg/atom (logic/gen-es [[:new-game]])))

(def local-state-atom
  (rg/atom {:fb-connected? false}))

(db/setup-game-listener es-atom)

(db/setup-connection-listener local-state-atom)

(defn app-container
  []
  (game/board es-atom local-state-atom db/save))

(defn render-app
  []
  (if-let [app-node (.getElementById js/document "app")]
    (rg/render-component [app-container] app-node)))

(render-app)
