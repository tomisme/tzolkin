(ns tzolkin.devcards.main
  (:require
   [tzolkin.art :as art]
   [tzolkin.game :as game]
   [tzolkin.logic :as logic]
   [tzolkin.spec :refer [spec]]
   [tzolkin.utils :as utils :refer [log diff]]
   [tzolkin.devcards.art]
   [tzolkin.devcards.buildings]
   [tzolkin.devcards.decisions]
   [tzolkin.devcards.game :refer [s]]
   [tzolkin.devcards.gears]
   [tzolkin.devcards.logic]
   [tzolkin.devcards.spec]
   [tzolkin.devcards.temples]
   [tzolkin.devcards.utils])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

;; TODO make into a macro!
(deftest differ
  (testing ":gain-materials"
    (is (= '(nil nil)
           (diff
             (logic/handle-decision
               (-> s
                 (logic/handle-action 0 [:choose-materials [{:corn 1} {:stone 1}]]))
               0)
             (-> s
               (update-in [:players 0 :materials :stone] + 1)))))))
