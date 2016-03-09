(ns tzolkin.devcards.gears
  (:require
   [reagent.core :as rg]
   [timothypratley.reanimated.core :as anim]
   [tzolkin.spec :refer [spec]]
   [tzolkin.art :as art]
   [tzolkin.logic :as logic]
   [tzolkin.devcards.game :refer [s]]
   [tzolkin.utils :refer [diff log]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(defcard-doc
  "#Gears
  The spinning gears are a one of the coolest mechanics in Tzolk'in. They're
  also the main way that players interact with the game.")

(deftest gear-action-tests
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
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:yax 0} {:yax 1} {:yax 2} {:yax 3} {:yax 4}]}))))))
  (let [gear :yax
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:yax 0} {:yax 1} {:yax 2} {:yax 3} {:yax 4}]}))))))
  "##Tikal"
  (let [gear :tik
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :tech
                                                     :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]}))))))
  (let [gear :tik
        num 1
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (let [num (:num-available-buildings spec)
                   buildings (vec (take num (:buildings s)))]
               (-> s
                 (update-in [:active :decisions] conj {:type :gain-building
                                                       :options buildings})))))))
  (let [gear :tik
        num 2
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :tech
                                                     :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]})
               (update-in [:active :decisions] conj {:type :tech
                                                     :options [{:agri 1} {:extr 1} {:arch 1} {:theo 1}]}))))))
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
             (-> s
               (logic/add-decision 0 :two-different-temples {})
               (logic/add-decision 0 :pay-resource {}))))))
  (let [gear :tik
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}]}))))))
  (let [gear :tik
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}]}))))))
  "##Uxmal"
  (let [gear :uxe
        num 0
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :corn] - 3)
               (update-in [:active :decisions] conj {:type :temple
                                                     :options [{:chac 1} {:quet 1} {:kuku 1}]}))))))
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
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:yax 0} {:yax 1} {:yax 2} {:yax 3} {:yax 4}
                                                               {:tik 0} {:tik 1} {:tik 2} {:tik 3} {:tik 4}
                                                               {:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}
                                                               {:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]}))))))
  (let [gear :uxe
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}]}))))))
  (let [gear :uxe
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:uxe 0} {:uxe 1} {:uxe 2} {:uxe 3} {:uxe 4}]}))))))
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
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:corn 5} {:wood 2}]}))))))
  (let [gear :pal
        num 3
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:corn 7} {:wood 3}]}))))))
  (let [gear :pal
        num 4
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:corn 9} {:wood 4}]}))))))
  (let [gear :pal
        num 5
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]}))))))
  (let [gear :pal
        num 6
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:pal 0} {:pal 1} {:pal 2} {:pal 3} {:pal 4}]}))))))
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
               (update-in [:players 0 :temples :kuku] + 1)
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]}))))))
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
               (update-in [:players 0 :temples :quet] + 1)
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]}))))))
  (let [gear :chi
        num 8
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:players 0 :materials :skull] - 1)
               (update-in [:players 0 :points] + 13)
               (update-in [:players 0 :temples :quet] + 1)
               (update-in [:active :decisions] conj {:type :gain-materials
                                                     :options [{:wood 1} {:stone 1} {:gold 1}]}))))))
  (let [gear :chi
        num 9
        action (get-in spec [:gears gear :actions num])]
    (testing (str gear " " num " " action)
      (is (= (logic/handle-action s 0 action)
             (-> s
               (update-in [:active :decisions] conj {:type :action
                                                     :options [{:chi 0} {:chi 1} {:chi 2} {:chi 3} {:chi 4} {:chi 5} {:chi 6} {:chi 7} {:chi 8}]})))))))

(defcard-rg gear-creator
  (fn [data _]
    (let [{:keys [size teeth tooth-width-factor tooth-height-factor]} @data
          set #(swap! data assoc %1 %2)]
      [:div.ui.segment
       [:div.ui.grid
        [:div.six.wide.column
         [:div.ui.form
          [:div.field
           [:div.label "Size"]
           [:input {:type "range"
                    :value size
                    :min 100, :max 200
                    :on-change #(set :size (art/e->val %))}]]
          [:div.field
           [:div.label "Teeth"]
           [:input {:type "range"
                    :value teeth
                    :min 10, :max 26
                    :on-change #(set :teeth (art/e->val %))}]]
          [:div.field
           [:div.label "Tooth Width Factor"]
           [:input {:type "range"
                    :value tooth-width-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-width-factor (art/e->val %))}]]
          [:div.field
           [:div.label "Tooth Height Factor"]
           [:input {:type "range"
                    :value tooth-height-factor
                    :min 0.1, :max 2
                    :step 0.1
                    :on-change #(set :tooth-height-factor (art/e->val %))}]]]]
        [:div.ten.wide.column
         [:svg {:width (* size 5)
                :height (* size 5)}
          [art/gear-el {:cx (* size 2)
                        :cy (* size 2)
                        :r size
                        :teeth teeth
                        :tooth-width-factor tooth-width-factor
                        :tooth-height-factor tooth-height-factor}]]]]]))
  (rg/atom {:size 75
            :teeth 12
            :tooth-width-factor 1
            :tooth-height-factor 1.1})
  {:inspect-data true})

(def spin-test-atom (rg/atom 0))

(defn spinning-worker-gear
  [{:keys [workers actions on-worker-click]}]
  (let [rotation-spring (anim/spring spin-test-atom)
        workers [:blue nil :blue :red nil nil :red nil nil nil]]
    (fn []
      [:svg {:width 300 :height 300}
        [art/gear-el {:cx 150
                      :cy 150
                      :r 75
                      :teeth 10
                      :tooth-height-factor 1.15
                      :tooth-width-factor 0.75
                      :workers workers
                      :gear :tik
                      :rotation @rotation-spring}]])))

(defcard-rg spinning-worker-gear-test
  [:div
    [:button {:on-click #(swap! spin-test-atom + (/ 360 10))}
      "Spin the gear!"]
    (for [[k v] (get spec :gears)]
      [:button {:key k} (:name v)])
    [spinning-worker-gear]]
  spin-test-atom
  {:inspect-data true})
