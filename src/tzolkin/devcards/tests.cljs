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

(deftest Art
  (testing "transform-str"
    "rotate"
    (is (= (art/transform-str [:rotate {:deg 90}]) "rotate(90)"))
    (is (= (art/transform-str [:rotate {:deg 55 :x 10 :y 10}]
                              [:rotate {:deg 10 :x 1 :y 1}])
           "rotate(55 10 10)rotate(10 1 1)")))
  (testing "materials-str"
    (is (= (art/materials-str {:wood 1 :stone 1 :gold 2 :corn 3 :skull 1})
           "🌲🗿🌕🌕3🌽💀"))))

(deftest Utils
  (testing "indexed"
    (is (= (logic/indexed '(a b c)) '([0 a] [1 b] [2 c])))
    (is (= (logic/indexed [:a :b :c]) '([0 :a] [1 :b] [2 :c]))))
  (testing "first-nil"
    (is (= (logic/first-nil ["nil" 0 :a nil :b nil]) 3)))
  (testing "rotate-vec"
    (is (= (logic/rotate-vec [:a :b :c] 2) [:b :c :a]))
    (is (= (logic/rotate-vec [:a :b :c] 4) [:c :a :b]))
    (is (= (logic/rotate-vec [:a :b :c] -1) [:b :c :a])))
  (testing "remove-from-vec"
    (is (= (logic/remove-from-vec [:a :b :c] 1) [:a :c])))
  (testing "apply-changes-to-map"
    (is (= (logic/apply-changes-to-map {:wood 1 :gold 2 :skull 1} + {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (logic/apply-changes-to-map {:stone 1 :gold 1 :corn 9} - {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))
    (is (= (logic/apply-changes-to-map {:stone 1 :gold 1} #(* % -1))
           {:stone -1 :gold -1}))))

(deftest Logic
  (testing "gear-position"
    (is (= (logic/gear-position :yax 2 0) 2))
    (is (= (logic/gear-position :yax 2 2) 4))
    (is (= (logic/gear-position :yax 4 13) 7))
    (is (= (logic/gear-position :chi 4 14) 5)))
  (testing "gear-slot"
    (is (= (logic/gear-slot :yax 2 0) 2))
    (is (= (logic/gear-slot :yax 4 2) 2))
    (is (= (logic/gear-slot :yax 7 13) 4))
    (is (= (logic/gear-slot :chi 5 14) 4)))
  (testing "adjust-materials"
    (is (= (logic/adjust-materials test-state 0 {:stone 2 :gold 1})
           (-> test-state
             (update-in [:players 0 :materials :stone] + 2)
             (update-in [:players 0 :materials :gold] + 1)))))
  (testing "adjust-temples"
    (is (= (logic/adjust-temples test-state 0 {:chac 2 :quet 1})
           (-> test-state
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] + 1))))))

(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")
