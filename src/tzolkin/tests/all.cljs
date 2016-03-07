(ns tzolkin.tests.core
  (:require
   [clojure.data :refer [diff]]
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic]
   [tzolkin.util :as util]
   [tzolkin.tests.util :refer [s]]
   [tzolkin.tests.buildings])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

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

(deftest Logic
  "##Positions/Slots"
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
  "##Adjustments"
  (testing "adjust-points"
    (is (= (logic/adjust-points s 0 5)
           (-> s
             (update-in [:players 0 :points] + 5)))))
  (testing "adjust-workers"
    (is (= (logic/adjust-workers s 0 1)
           (-> s
             (update-in [:players 0 :workers] inc)))))
  (testing "adjust-materials"
    (is (= (logic/adjust-materials s 0 {:stone 2 :gold 1})
           (-> s
             (update-in [:players 0 :materials :stone] + 2)
             (update-in [:players 0 :materials :gold] inc)))))
  (testing "adjust-temples"
    (is (= (logic/adjust-temples s 0 {:chac 2 :quet 1})
           (-> s
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] inc)))))
  (testing "adjust-tech"
    (is (= (logic/adjust-tech s 0 {:agri 2 :arch 1})
           (-> s
             (update-in [:players 0 :tech :agri] + 2)
             (update-in [:players 0 :tech :arch] inc)))))
  "##Yaxchilan"
  (let [gear :yax
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
              (update-in [:players 0 :materials :wood] + 1))))))
  (let [gear :yax
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
              (update-in [:players 0 :materials :stone] + 1)
              (update-in [:players 0 :materials :corn] + 1))))))
  (let [gear :yax
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
              (update-in [:players 0 :materials :gold] + 1)
              (update-in [:players 0 :materials :corn] + 2))))))
  (let [gear :yax
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
              (update-in [:players 0 :materials :skull] + 1))))))
  (let [gear :yax
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
              (update-in [:players 0 :materials :gold] + 1)
              (update-in [:players 0 :materials :stone] + 1)
              (update-in [:players 0 :materials :corn] + 2))))))
  (let [gear :yax
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :yax
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  "##Tikal"
  (let [gear :tik
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (assoc-in [:active :decision :type] :tech)
               (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))))
  (let [gear :tik
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (let [num (:num-available-buildings spec)
                   buildings (vec (take num (:buildings s)))]
               (-> s
                 (assoc-in [:active :decision :type] :gain-building)
                 (assoc-in [:active :decision :options] buildings)))))))
  (let [gear :tik
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (assoc-in [:active :decision :type] :tech-two)
               (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))))
  (let [gear :tik
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :tik
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :tik
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :tik
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  "##Uxmal"
  (let [gear :uxe
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :corn] - 3)
               (assoc-in [:active :decision :type] :temple)
               (assoc-in [:active :decision :options] [{:chac 1} {:quet 1} {:kuku 1}]))))))
  (let [gear :uxe
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :uxe
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :workers] + 1))))))
  (let [gear :uxe
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :uxe
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :corn] - 1)
               (assoc-in [:active :decision :type] :non-chi-action)
               (assoc-in [:active :decision :options] []))))))
  (let [gear :uxe
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :uxe
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  "##Palenque"
  (let [gear :pal
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :corn] + 3))))))
  (let [gear :pal
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :corn] + 4))))))
  (let [gear :pal
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:corn 5} {:wood 2}]))))))
  (let [gear :pal
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:corn 7} {:wood 3}]))))))
  (let [gear :pal
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:corn 9} {:wood 4}]))))))
  (let [gear :pal
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  (let [gear :pal
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  "##Chichen Itza"
  (let [gear :chi
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 4)
               (update-in [:players 0 :temples :chac] + 1))))))
  (let [gear :chi
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 5)
               (update-in [:players 0 :temples :chac] + 1))))))
  (let [gear :chi
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 6)
               (update-in [:players 0 :temples :chac] + 1))))))
  (let [gear :chi
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 7)
               (update-in [:players 0 :temples :kuku] + 1))))))
  (let [gear :chi
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 8)
               (update-in [:players 0 :temples :kuku] + 1))))))
  (let [gear :chi
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 8)
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:wood 1} {:stone 1} {:gold 1}])
               (update-in [:players 0 :temples :kuku] + 1))))))
  (let [gear :chi
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 10)
               (update-in [:players 0 :temples :quet] + 1))))))
  (let [gear :chi
        num 7
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 11)
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:wood 1} {:stone 1} {:gold 1}])
               (update-in [:players 0 :temples :quet] + 1))))))
  (let [gear :chi
        num 8
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 13)
               (assoc-in [:active :decision :type] :gain-materials)
               (assoc-in [:active :decision :options] [{:wood 1} {:stone 1} {:gold 1}])
               (update-in [:players 0 :temples :quet] + 1))))))
  (let [gear :chi
        num 9
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             false))))
  "##Decisions"
  (testing ":gain-materials"
    (is (= (logic/handle-decision
             (logic/handle-action s 0 [:choose-materials [{:corn 1} {:stone 1}]])
             1)
           (-> s
             (update-in [:players 0 :materials :stone] + 1)))))
  (testing ":gain-building"
    (is (= (logic/handle-decision
             (-> s
               (assoc :buildings [{} {:materials {:corn 1}} {}])
               (logic/choose-building))
             1)
           (-> s
             (assoc :buildings [{} {}])
             (update-in [:players 0 :buildings] conj {:materials {:corn 1}})
             (update-in [:players 0 :materials :corn] + 1)))))
  (testing ":tech"
    (is (= (logic/handle-decision
             (logic/choose-tech s)
             1)
           (-> s
             (update-in [:players 0 :tech :extr] inc)))))
  (testing ":tech-two"
    (is (= (logic/handle-decision
             (logic/choose-tech-two s)
             2)
           (-> s
             (update-in [:players 0 :tech :arch] inc)
             (assoc-in [:active :decision :type] :tech)
             (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))))

(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")

(defcard-doc
  (drop-last
    (diff
      (logic/give-building s 0 {:build :monument})
      (-> s
        (update-in [:players 0 :buildings] conj {:build :monument})
        (assoc-in [:active :decision :type] :gain-monument)
        (assoc-in [:active :decision :options] (:monuments s))))))
