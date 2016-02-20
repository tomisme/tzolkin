(ns tzolkin.devcards.buildings
  (:require
   [reagent.core :as rg]
   [timothypratley.reanimated.core :as anim]
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
  (art/building-card random-building))

(defcard-rg all-buildings
  [:div
    (map-indexed
      (fn [index building]
        ^{:key index}
        [:div.item (art/building-card building)])
      (:buildings spec))])
