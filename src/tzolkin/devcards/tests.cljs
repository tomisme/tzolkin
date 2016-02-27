(ns tzolkin.devcards.tests
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :as dev-game])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(def s (dev-game/new-test-game {:players 2}))

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
    (is (= (logic/adjust-materials s 0 {:stone 2 :gold 1})
           (-> s
             (update-in [:players 0 :materials :stone] + 2)
             (update-in [:players 0 :materials :gold] + 1)))))
  (testing "adjust-temples"
    (is (= (logic/adjust-temples s 0 {:chac 2 :quet 1})
           (-> s
             (update-in [:players 0 :temples :chac] + 2)
             (update-in [:players 0 :temples :quet] + 1)))))
  (testing "give-building"
    (is (= (logic/give-building s 0 {:cost {:corn 2 :wood 1}})
           (-> s
             (update-in [:players 0 :buildings] conj {:cost {:corn 2 :wood 1}})
             (update-in [:players 0 :materials :corn] - 2)
             (update-in [:players 0 :materials :wood] - 1))))
    (is (= (logic/give-building s 0 {:materials {:wood 2}})
           (-> s
             (update-in [:players 0 :buildings] conj {:materials {:wood 2}})
             (update-in [:players 0 :materials :wood] + 2))))
    (is (= (logic/give-building s 0 {:temples {:kuku 2 :chac 1}})
           (-> s
             (update-in [:players 0 :buildings] conj {:temples {:kuku 2 :chac 1}})
             (update-in [:players 0 :temples :kuku] + 2)
             (update-in [:players 0 :temples :chac] + 1))))
    (is (= (logic/give-building s 0 {:gain-worker true})
           (-> s
             (update-in [:players 0 :buildings] conj {:gain-worker true})
             (update-in [:players 0 :workers] inc))))
    (is (= (logic/give-building s 0 {:points 3})
           (-> s
             (update-in [:players 0 :buildings] conj {:points 3})
             (update-in [:players 0 :points] + 3))))
    (is (= (logic/give-building s 0 {:tech :extr})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech :extr})
             (update-in [:players 0 :tech-step :extr] inc))))
    (is (= (logic/give-building s 0 {:tech :any})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech :any})
             (assoc-in [:active :decision :type] :tech)
             (assoc-in [:active :decision :options] [:agri :extr :arch :theo]))))
    (is (= (logic/give-building s 0 {:tech :any-two})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech :any-two})
             (assoc-in [:active :decision :type] :tech-two)
             (assoc-in [:active :decision :options] [:agri :extr :arch :theo]))))
    (is (= (logic/give-building s 0 {:build :building})
           (let [num (:num-available-buildings spec)
                 buildings (vec (take num (:buildings s)))]
             (-> s
               (update-in [:players 0 :buildings] conj {:build :building})
               (assoc-in [:active :decision :type] :build-building)
               (assoc-in [:active :decision :options] buildings)))))
    (is (= (logic/give-building s 0 {:build :monument})
           (let [num (:num-available-monuments spec)
                 monuments (vec (take num (:monuments s)))]
             (-> s
               (update-in [:players 0 :monuments] conj {:build :monument})
               (assoc-in [:active :decision :type] :build-monument)
               (assoc-in [:active :decision :options] monuments))))))
  "#Actions"
  (testing ":trade"
    (is (= (logic/handle-action s 0 [:trade true])
           (-> s)
           false)))
  (testing ":build"
    (is (= (logic/handle-action s 0 [:build :single])
           (-> s)
           false))
    (is (= (logic/handle-action s 0 [:build :double])
           (-> s)
           false))
    (is (= (logic/handle-action s 0 [:build :with-corn])
           (-> s)
           false)))
  (testing ":temples"
    (is (= (logic/handle-action s 0 [:temples {:cost {:corn 3} :choose :any}])
           (-> s)
           false))
    (is (= (logic/handle-action s 0 [:temples {:cost {:corn 3} :choose :any-two}])
           (-> s)
           false)))
  (testing ":tech-step"
    (is (= (logic/handle-action s 0 [:tech-step 1])
           (-> s)
           false))
    (is (= (logic/handle-action s 0 [:tech-step 2])
           (-> s)
           false)))
  (testing ":gain-worker"
    (is (= (logic/handle-action s 0 [:gain-worker true])
           (-> s
             (update-in [:players 0 :workers] + 1)))))
  (testing ":skull-action"
    (is (= (logic/handle-action s 0 [:skull-action {:points 2 :temple :kuku}])
           (-> s
             (update-in [:players 0 :materials :skull] - 1)
             (update-in [:players 0 :points] + 2)
             (update-in [:players 0 :temples :kuku] + 1))))
    (is (= (logic/handle-action s 0 [:skull-action {:points 2 :temple :kuku}])
           (-> s
             (update-in [:players 0 :materials :skull] - 1)
             (update-in [:players 0 :points] + 2)
             (update-in [:players 0 :temples :kuku] + 1))))
    (is (= (logic/handle-action s 0 [:skull-action {:resource true}])
           (-> s
             (update-in [:players 0 :materials :skull] - 1)
             (assoc-in [:active :decision :type] :gain-materials)
             (assoc-in [:active :decision :options] [{:wood 1} {:stone 1} {:gold 1}])))))
  (testing ":choose-action"
    (is (= (logic/handle-action s 0 [:choose-action :non-chi])
           (-> s
             (assoc-in [:active :decision :type] :non-chi-action)
             (assoc-in [:active :decision :options] []))))
    (is (= (logic/handle-action s 0 [:choose-action :yax])
           (-> s
             (assoc-in [:active :decision :type] :yax-action)
             (assoc-in [:active :decision :options] [])))))
  (testing ":gain-materials"
    (is (= (logic/handle-action s 0 [:gain-materials {:corn 1 :stone 1 :skull 1}])
           (-> s
             (update-in [:players 0 :materials :corn] + 1)
             (update-in [:players 0 :materials :stone] + 1)
             (update-in [:players 0 :materials :skull] + 1)))))
  (testing ":choose-materials"
    (is (= (logic/handle-action s 0 [:choose-materials [{:corn 2} {:wood 1}]])
           (-> s
             (assoc-in [:active :decision :type] :gain-materials)
             (assoc-in [:active :decision :options] [{:corn 2} {:wood 1}])))))
  "#Decisions"
  "#Place Worker"
  "#Remove Worker")

(defcard-doc
  "##Other Tests

    - If the bank does not have enough crystal skulls to reward all the
      players who should get one, then no one gets a crystal skull.")
