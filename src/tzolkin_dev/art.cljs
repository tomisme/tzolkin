(ns tzolkin-dev.art
  (:require
   [reagent.core :as rg]
   [reanimated.core :as anim]
   [tzolkin.seed :refer [seed]]
   [tzolkin.art :as art]
   [tzolkin.rules :as rules]
   [tzolkin-dev.test-data :refer [s]]
   [tzolkin.utils :refer [log sin cos pi]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))


(deftest art-tests
  (testing "transform-str"
    "rotate"
    (is (= (art/transform-str [:rotate {:deg 90}]) "rotate(90)"))
    (is (= (art/transform-str [:rotate {:deg 55 :x 10 :y 10}]
                              [:rotate {:deg 10 :x 1 :y 1}])
           "rotate(55 10 10)rotate(10 1 1)")))
  #_(testing "symbols-str"
      (is (= (art/symbols-str {:wood 1 :stone 1 :gold 2 :corn 3 :skull 1})
             "🌲🗿2🌕3🌽💀"))))


#_(defcard-rg symbol-examples
    (into [:div]
          (for [size '(16 45)]
            (into [:div {:style {:font-size size}}]
                  (for [[k v] art/symbols]
                    (str (name k) ": " v ", "))))))


(defcard-rg circle
  (let [w 150
        h w
        cx (/ w 2)
        cy (/ h 2)
        r 20]
    [:svg {:width w :height h}
     [:circle {:cx cx :cy cy :r r}]]))


(defcard-doc
  "## Positioning things around a circle
  For each element around a centre at (cx, cy):
  ```
    x = cx + r cos(2kπ/n)
    y = cy + r sin(2kπ/n)
  ```
  - r: distance from center
  - n: total number of elements
  - k: index of current element")


(defcard-rg pos-around-a-circle
  (let [width 150
        height width
        distance (/ width 3)
        el-r 10
        num 12
        cx (/ width 2)
        cy (/ height 2)
        el-cx (fn [i] (+ cx (* distance (cos (/ (* 2 i pi) num)))))
        el-cy (fn [i] (+ cy (* distance (sin (/ (* 2 i pi) num)))))]
    [:svg {:width width :height height}
     (into [:g]
           (for [i (range num)]
             [:circle {:cx (el-cx i)
                       :cy (el-cy i)
                       :r el-r}]))]))


(defcard-rg manual-annulus-sectors
  [:svg {:width 150 :height 150}
   [:circle {:cx 50 :cy 50 :r 40 :fill "transparent"
             :stroke-width 25
             :stroke "green"
             :stroke-dasharray "50 1000"
             :stroke-dashoffset -10}]
   [:path {:d "M 80 80
               A 40 40 0 0 0 120 120
               L 120 100
               A 20 20 0 0 1 100 80
               Z"}]])


(defcard-doc (:gears seed))


(defcard-rg readable-action-labels
  (into [:div]
        (for [[gear {:keys [actions]}] (:gears seed)]
          (into [:div]
                (for [action actions]
                  (let [a 500]
                    [:svg {:width (/ a 3) :height (/ a 6)}
                     (art/readable-action-label-el {:action action
                                                    :color (get art/color-strings gear)
                                                    :cx (/ a 6)
                                                    :cy (/ a 2.5)
                                                    :r (/ a 3)
                                                    :length (/ a 4)})]))))))


(defcard-rg action-labels
  (into [:div]
        (for [[gear _] (:gears seed)]
          (let [a 350]
            [:svg {:width (/ a 1.2) :height (/ a 1.2)}
             (art/action-labels-el {:gear gear
                                    :cx (/ a 2.5)
                                    :cy (/ a 2.5)
                                    :r (/ a 3)})]))))

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
                    :min 75, :max 200
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
          [art/gear-svg {:cx (* size 2)
                         :cy (* size 2)
                         :r size
                         :gear :kuku
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
        workers [:blue :none :blue :red :none :none :red :none :none :none]]
    (fn []
      [:svg {:width 300 :height 300}
       [art/gear-svg {:cx 150
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
   [spinning-worker-gear]]
  spin-test-atom)


#_(def test-event-stream
    (rules/events->es game/test-events))


#_(defcard-rg game-log-art-test
    [art/game-log-el {:stream test-event-stream}])


(defcard-rg temple-art-test
  [:div {:style {:width 462}}
   (art/temples-el s)])


(defcard-rg tech-art-test
  [:div {:style {:width 480}}
   (art/tech-tracks-el (-> (:players s)
                           (assoc-in [0 :tech :arch] 1)
                           (assoc-in [1 :tech :arch] 1)
                           (assoc-in [2 :tech :arch] 1)
                           (assoc-in [3 :tech :arch] 1)
                           (assoc-in [0 :tech :theo] 1)
                           (assoc-in [1 :tech :theo] 1)
                           (assoc-in [2 :tech :theo] 1)
                           (assoc-in [3 :tech :theo] 1)))])


(defcard-rg status-bar-test
  (art/status-bar-el
   s
   #(log [%1 %2])
   #(log "trading!")
   #(log "stopping trading")
   #(log "end turn!")
   #(log "start game!")
   #(log "add player")))


(defcard-rg all-starters
  (into [:div.ui.cards]
        (map-indexed
         (fn [index starter]
           [:div (art/starter-card starter #(log "selected!"))])
         (:starters seed))))


(defcard-rg trade-window-test
  (art/trade-window-el {:materials {:corn 8 :wood 2 :stone 1 :gold 1 :skull 1}} #(log %) #(log %)))


(defcard-rg gear-layout-test
  (art/gear-layout-el
   {:pal {:workers (vec (repeat 10 :none))}
    :yax {:workers (vec (repeat 10 :none))}
    :uxe {:workers (vec (repeat 10 :none))}
    :tik {:workers (vec (repeat 10 :none))}
    :chi {:workers (vec (repeat 13 :none))}}
   []  ;jungle
   0   ; turn
   ()  ; player-order
   {}  ; players
   0 ; active
   nil ; on-end-turn
   nil ; on-take-starting-player
   7 ; starting-player-corn
   0)) ; pid-on-start-space


(def jungle-atom
  (rg/atom [{:corn-tiles 4}
            {:corn-tiles 3
             :wood-tiles 2}
            {:corn-tiles 3
             :wood-tiles 1}
            {:corn-tiles 2
             :wood-tiles 0}]))


(defcard-rg jungle-test
  [:svg {:width 280 :height 210}
   [:g {:transform "translate(0 50)"}
    (art/jungle-svg 200 120 85 10 @jungle-atom)]]
  jungle-atom
  {:inspect-data true})
