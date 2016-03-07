(ns tzolkin.tests.buildings
  (:require
   [clojure.data :refer [diff]]
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [tzolkin.tests.util :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is]]))

(deftest buildings
  (testing
    (is (= (logic/give-building s 0 {:cost {:corn 2 :wood 1}})
           (-> s
             (update-in [:players 0 :buildings] conj {:cost {:corn 2 :wood 1}})
             (update-in [:players 0 :materials :corn] - 2)
             (update-in [:players 0 :materials :wood] dec))))
    (is (= (logic/give-building s 0 {:materials {:wood 2}})
           (-> s
             (update-in [:players 0 :buildings] conj {:materials {:wood 2}})
             (update-in [:players 0 :materials :wood] + 2))))
    (is (= (logic/give-building s 0 {:temples {:kuku 2 :chac 1}})
           (-> s
             (update-in [:players 0 :buildings] conj {:temples {:kuku 2 :chac 1}})
             (update-in [:players 0 :temples :kuku] + 2)
             (update-in [:players 0 :temples :chac] inc))))
    (is (= (logic/give-building s 0 {:gain-worker true})
           (-> s
             (update-in [:players 0 :buildings] conj {:gain-worker true})
             (update-in [:players 0 :workers] inc))))
    (is (= (logic/give-building s 0 {:points 3})
           (-> s
             (update-in [:players 0 :buildings] conj {:points 3})
             (update-in [:players 0 :points] + 3))))
    (is (= (logic/give-building s 0 {:tech {:extr 1}})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech {:extr 1}})
             (update-in [:players 0 :tech :extr] inc))))
    (is (= (logic/give-building s 0 {:tech :any})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech :any})
             (assoc-in [:active :decision :type] :tech)
             (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))
    (is (= (logic/give-building s 0 {:tech :any-two})
           (-> s
             (update-in [:players 0 :buildings] conj {:tech :any-two})
             (assoc-in [:active :decision :type] :tech-two)
             (assoc-in [:active :decision :options] [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]))))
    (is (= (logic/give-building s 0 {:build :building})
           (let [num (:num-available-buildings spec)
                 buildings (vec (take num (:buildings s)))]
             (-> s
               (update-in [:players 0 :buildings] conj {:build :building})
               (assoc-in [:active :decision :type] :gain-building)
               (assoc-in [:active :decision :options] buildings)))))
    (is (= (logic/give-building s 0 {:build :monument})
           (-> s
             (update-in [:players 0 :buildings] conj {:build :monument})
             (assoc-in [:active :decision :type] :gain-monument)
             (assoc-in [:active :decision :options] (:monuments s)))))))
