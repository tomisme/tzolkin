(ns tzolkin.d3
  (:require [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

(defn log
  [args]
  (.log js/console args))

(defn rg-card
  [el]
  (dc/reagent el))

(defn svg-test
  [child]
  [:div {:class "center aligned ui segment"}
   [:svg {:width 650 :height 400
          :style {:background "red"}}
    child]])

(defcard svg-test
  (rg-card [svg-test [:g [:circle {:cx 50 :cy 50 :r 100}]]]))
