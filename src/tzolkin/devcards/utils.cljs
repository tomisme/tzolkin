(ns tzolkin.devcards.utils
  (:require
   [clojure.data :refer [diff]]
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.utils :as utils]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(deftest utils
  (testing "indexed"
    (is (= (utils/indexed '(a b c)) '([0 a] [1 b] [2 c])))
    (is (= (utils/indexed [:a :b :c]) '([0 :a] [1 :b] [2 :c]))))
  (testing "first-nil"
    (is (= (utils/first-nil ["nil" 0 :a nil :b nil]) 3)))
  (testing "rotate-vec"
    (is (= (utils/rotate-vec [:a :b :c] 2) [:b :c :a]))
    (is (= (utils/rotate-vec [:a :b :c] 4) [:c :a :b]))
    (is (= (utils/rotate-vec [:a :b :c] -1) [:b :c :a])))
  (testing "remove-from-vec"
    (is (= (utils/remove-from-vec [:a :b :c] 1) [:a :c])))
  (testing "apply-changes-to-map"
    (is (= (utils/apply-changes-to-map {:wood 1 :gold 2 :skull 1} + {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (utils/apply-changes-to-map {:stone 1 :gold 1 :corn 9} - {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))
    (is (= (utils/apply-changes-to-map {:stone 1 :gold 1} #(* % -1))
           {:stone -1 :gold -1}))))
