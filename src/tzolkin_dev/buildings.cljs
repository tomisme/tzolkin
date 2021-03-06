(ns tzolkin-dev.buildings
  (:require
   [tzolkin.seed :refer [seed]]
   [tzolkin.art :as art]
   [tzolkin.rules :as rules]
   [tzolkin-dev.test-data :refer [s]]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [tzolkin-dev.macros :refer [nod]]
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(deftest buildings
  (testing
   (nod (rules/build-building s 0 {:cost {:corn 2 :wood 1}})
        (-> s
            (update-in [:players 0 :buildings] conj {:cost {:corn 2 :wood 1}})
            (update-in [:players 0 :materials :corn] - 2)
            (update-in [:players 0 :materials :wood] dec)))
   (nod (rules/build-building s 0 {:materials {:wood 2}})
        (-> s
            (update-in [:players 0 :buildings] conj {:materials {:wood 2}})
            (update-in [:players 0 :materials :wood] + 2)))
   (nod (rules/build-building s 0 {:temples {:kuku 2 :chac 1}})
        (-> s
            (update-in [:players 0 :buildings] conj {:temples {:kuku 2 :chac 1}})
            (update-in [:players 0 :temples :kuku] + 2)
            (update-in [:players 0 :temples :chac] inc)))
   (nod (rules/build-building s 0 {:gain-worker true})
        (-> s
            (update-in [:players 0 :buildings] conj {:gain-worker true})
            (update-in [:players 0 :workers] inc)))
   (nod (rules/build-building s 0 {:points 3})
        (-> s
            (update-in [:players 0 :buildings] conj {:points 3})
            (update-in [:players 0 :points] + 3)))
   (nod (rules/build-building s 0 {:tech {:extr 1}})
        (-> s
            (update-in [:players 0 :buildings] conj {:tech {:extr 1}})
            (update-in [:players 0 :tech :extr] inc)))
   (nod (rules/build-building s 0 {:tech :any})
        (-> s
            (update-in [:players 0 :buildings] conj {:tech :any})
            (update-in [:active :decisions] conj {:type :free-tech
                                                  :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]})))
   (nod (rules/build-building s 0 {:tech :any-two})
        (-> s
            (update-in [:players 0 :buildings] conj {:tech :any-two})
            (update-in [:active :decisions] conj {:type :free-tech
                                                  :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]})
            (update-in [:active :decisions] conj {:type :free-tech
                                                  :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]})))
   (nod (rules/build-building s 0 {:build :building})
        (let [num (:num-available-buildings seed)
              buildings (vec (take num (:buildings s)))]
          (-> s
              (update-in [:players 0 :buildings] conj {:build :building})
              (update-in [:active :decisions] conj {:type :build-building
                                                    :options buildings}))))
   (nod (rules/build-building s 0 {:build :monument})
        (-> s
            (update-in [:players 0 :buildings] conj {:build :monument})
            (update-in [:active :decisions] conj {:type :build-monument
                                                  :options (:monuments s)})))
   (nod (rules/build-building s 0 {:trade true})
        (-> s
            (update-in [:players 0 :buildings] conj {:trade true})
            (update :active assoc :trading? true)))))

(def random-building
  (first (shuffle (:buildings seed))))

(defcard-rg single-building
  (art/building-card random-building nil false)
  random-building
  {:inspect-data true})

(defcard-rg all-buildings
  [:div.ui.cards
   (map-indexed
    (fn [index building]
      [:div {:key index} (art/building-card building nil false)])
    (:buildings seed))])
