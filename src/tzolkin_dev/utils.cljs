(ns tzolkin-dev.utils
  (:require
   [tzolkin.seed :refer [seed]]
   [tzolkin.rules :as rules]
   [tzolkin.utils :as utils]
   [tzolkin-dev.test-data :refer [s]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest utils
  (testing "indexed"
    (is (= (utils/indexed '(a b c)) '([0 a] [1 b] [2 c])))
    (is (= (utils/indexed [:a :b :c]) '([0 :a] [1 :b] [2 :c]))))
  (testing "first-val"
    (is (= (utils/first-val ["none" 0 :a :none :b :none] :none) 3)))
  (testing "rotate-vec"
    (is (= (utils/rotate-vec [:a :b :c] 2) [:b :c :a]))
    (is (= (utils/rotate-vec [:a :b :c] 4) [:c :a :b]))
    (is (= (utils/rotate-vec [:a :b :c] -1) [:b :c :a])))
  (testing "remove-from-vec"
    (is (= (utils/remove-from-vec [:a :b :c] 1) [:a :c])))
  (testing "change-map"
    (is (= (utils/change-map {:wood 1 :gold 2 :skull 1} + {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (utils/change-map {:stone 1 :gold 1 :corn 9} - {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))
    (is (= (utils/change-map {:stone 1 :gold 1} #(* % -1))
           {:stone -1 :gold -1}))))
