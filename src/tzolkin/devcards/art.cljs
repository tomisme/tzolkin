(ns tzolkin.devcards.art
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(deftest art-tests
  (testing "transform-str"
    "rotate"
    (is (= (art/transform-str [:rotate {:deg 90}]) "rotate(90)"))
    (is (= (art/transform-str [:rotate {:deg 55 :x 10 :y 10}]
                              [:rotate {:deg 10 :x 1 :y 1}])
           "rotate(55 10 10)rotate(10 1 1)")))
  (testing "materials-str"
    (is (= (art/materials-str {:wood 1 :stone 1 :gold 2 :corn 3 :skull 1})
           "🌲🗿🌕🌕3🌽💀"))))

(defcard-rg symbol-examples
  [:div (for [size '(16 45)]
          [:div {:style {:font-size size}}
            (for [[k v] art/symbols]
              (str (name k) ": " v ", "))])])
