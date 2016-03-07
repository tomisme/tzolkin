(ns tzolkin.devcards.utils
  (:require
   [clojure.data :refer [diff]]
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.util :as util]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(deftest utils
  (testing "indexed"
    (is (= (util/indexed '(a b c)) '([0 a] [1 b] [2 c])))
    (is (= (util/indexed [:a :b :c]) '([0 :a] [1 :b] [2 :c]))))
  (testing "first-nil"
    (is (= (util/first-nil ["nil" 0 :a nil :b nil]) 3)))
  (testing "rotate-vec"
    (is (= (util/rotate-vec [:a :b :c] 2) [:b :c :a]))
    (is (= (util/rotate-vec [:a :b :c] 4) [:c :a :b]))
    (is (= (util/rotate-vec [:a :b :c] -1) [:b :c :a])))
  (testing "remove-from-vec"
    (is (= (util/remove-from-vec [:a :b :c] 1) [:a :c])))
  (testing "apply-changes-to-map"
    (is (= (util/apply-changes-to-map {:wood 1 :gold 2 :skull 1} + {:wood 2 :gold 2})
           {:wood 3 :gold 4 :skull 1}))
    (is (= (util/apply-changes-to-map {:stone 1 :gold 1 :corn 9} - {:corn 7 :gold 1})
           {:stone 1 :gold 0 :corn 2}))
    (is (= (util/apply-changes-to-map {:stone 1 :gold 1} #(* % -1))
           {:stone -1 :gold -1}))))
