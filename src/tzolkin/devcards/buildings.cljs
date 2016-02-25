(ns tzolkin.devcards.buildings
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(defcard-doc
  "#Buildings")

(def random-building
  (first (shuffle (:buildings spec))))

(defcard-rg single-building
  (art/building-card random-building nil false)
  random-building
  {:inspect-data true})

(defcard-rg all-buildings
  [:div.ui.cards
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div (art/building-card building nil false)])
      (:buildings spec))])
