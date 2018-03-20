(ns tzolkin.core
  (:require
   [reagent.core]
   [tzolkin.game]
   [tzolkin.db]
   [tzolkin.logic]
   ; [tzolkin.utils :refer [log]]
   [devtools.core :as devtools]))

(defonce devtools
  (devtools/install! :all))


(def *game-state
  (reagent.core/atom (tzolkin.logic/gen-es [[:new-game]])))


(def *app-state
  (reagent.core/atom {:fb-connected? false}))


(tzolkin.db/setup-game-listener *game-state)


(tzolkin.db/setup-connection-listener *app-state)


(defn app-container []
  (tzolkin.game/board *game-state *app-state tzolkin.db/save))


(defn render-app []
  (if-let [app-node (.getElementById js/document "app")]
    (reagent.core/render-component [app-container] app-node)))


(render-app)
