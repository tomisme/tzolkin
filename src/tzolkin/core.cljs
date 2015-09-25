(ns tzolkin.core
  (:require
   #_[om.core :as om :include-macros true]
   [sablono.core :as sab :include-macros true])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

#_(defn next-state
    [prev-state input]
    #_{:pre (is input? Vector)}
    (-> prev-state
      (assoc :num inc)))

#_(defn view
    [current-state]
    [:span "I'm board"])

(enable-console-print!)

(defcard board
  "You're playing Tzolk'in! Just pretend for now."
  (sab/html [:center "bam"]))

(defn main []
  ;; conditionally start the app based on wether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (js/React.render (sab/html [:div "WHAT TIME IS IT?"]) node)))

(main)
