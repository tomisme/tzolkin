(ns tzolkin.devcards.tests
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :as dev-game])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(def test-state (dev-game/new-test-game {:players 2}))

(defcard-doc
  "`(def test-state (dev-game/new-test-game {:players 2}))`")

(deftest transform-str-test
  "`transform-str` takes any number of (supported) svg transform definitions
  and returns a string for use as an svg element's `transform` attribute
  (docs at
  [mdn](https://developer.mozilla.org/en/docs/Web/SVG/Attribute/transform))."
  (testing
    "rotate"
    (is (= (art/transform-str [:rotate {:deg 90}]) "rotate(90)"))
    (is (= (art/transform-str [:rotate {:deg 55 :x 10 :y 10}]
                              [:rotate {:deg 10 :x 1 :y 1}])
           "rotate(55 10 10)rotate(10 1 1)"))))

(deftest materials-str-test
  "`materials-str` takes a map of resources and the amount of each and returns
  a string of symbols.

  Amounts larger than two represented by a number and a single symbol."
  (testing
    (is (= (art/materials-str {:wood 1 :stone 1 :gold 2 :corn 3 :skull 1})
           "🌲🗿🌕🌕3🌽💀"))))

(deftest apply-changes-to-map
  (testing
    (is (= (logic/apply-changes-to-map + {:wood 1 :gold 2 :skull 1} {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (logic/apply-changes-to-map - {:stone 1 :gold 1 :corn 9} {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))))

(deftest temple-movement
  (testing "adjust-temples"
    (is (= (logic/adjust-temples test-state 0 {:chac 2 :quet 1})
           (-> test-state
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] + 1))))))

(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")
