(ns tzolkin-devcards.game
  (:require
   [tzolkin.logic :as logic]))

(defn reduce-events
  [prev-state events]
  (reduce logic/handle-event prev-state events))

(def s
  (reduce-events {}
                 [[:new-game]
                  [:add-player {:name "Elisa" :color :red}]
                  [:add-player {:name "Tom"   :color :blue}]
                  [:add-player {:name "Aaron" :color :orange}]
                  [:add-player {:name "Jess"  :color :yellow}]
                  [:start-game {:test? true}]]))
