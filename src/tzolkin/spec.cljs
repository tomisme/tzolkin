(ns tzolkin.spec)

(def tech
  {:agri {}
   :extr {}
   :arch {}
   :theo {}})

(def buildings
  [{:cost {:corn 4}
    :farm :all
    :color :yellow
    :age 1}
   {:cost {:corn 4}
    :farm :all
    :color :yellow
    :age 1}
   {:cost {:wood 1}
    :farm 1
    :color :yellow
    :age 1}
   {:cost {:wood 1}
    :farm 1
    :color :yellow
    :age 1}
   {:cost {:wood 1}
    :farm 1
    :color :yellow
    :age 1}
   {:cost {:wood 1 :stone 1}
    :tech :extr
    :materials {:corn 1}
    :color :green
    :age 1}
   {:cost {:wood 2 :stone 1}
    :tech :extr
    :materials {:gold 1}
    :color :green
    :age 1}
   {:cost {:wood 3}
    :tech :agri
    :materials {:stone 1}
    :color :green
    :age 1}
   {:cost {:wood 2}
    :tech :agri
    :color :green
    :age 1}
   {:cost {:wood 2 :stone 1}
    :temples {:chac 1 :quet 1}
    :color :brown
    :age 1}
   {:cost {:wood 1 :gold 1}
    :temples {:any 1}
    :build :building
    :color :brown
    :age 1}
   {:cost {:wood 1 :stone 2}
    :temples {:chac 1 :kuku 1}
    :color :brown
    :age 1}
   {:cost {:stone 1 :gold 1}
    :tech :theo
    :temples {:kuku 1}
    :color :blue
    :age 1}
   {:cost {:gold 1}
    :tech :arch
    :color :blue
    :age 1}
   {:cost {:wood 2}
    :farm 3
    :color :yellow
    :age 2}
   {:cost {:wood 2}
    :farm 3
    :color :yellow
    :age 2}
   {:cost {:wood 2}
    :farm 3
    :color :yellow
    :age 2}
   {:cost {:wood 2}
    :materials {:corn 8}
    :color :yellow
    :age 2}
   {:cost {:wood 3 :stone 1}
    :tech :any
    :materials {:corn 6}
    :color :green
    :age 2}
   {:cost {:wood 2 :stone 2}
    :tech :any
    :materials {:skulls 1}
    :color :green
    :age 2}
   {:cost {:wood 2 :stone 1}
    :tech :any
    :materials {:gold 1}
    :color :green
    :age 2}
   {:cost {:wood 3}
    :tech :any
    :materials {:stone 1}
    :color :green
    :age 2}
   {:cost {:wood 1 :stone 1 :gold 1}
    :gain-worker true
    :points 6
    :color :brown
    :age 2}
   {:cost {:wood 2 :stone 1 :gold 1}
    :free-action-for-corn true
    :points 2
    :color :brown
    :age 2}
   {:cost {:wood 1 :stone 2 :gold 1}
    :temples {:chac 1 :quet 1 :kuku 1}
    :points 3
    :color :brown
    :age 2}
   {:cost {:wood 1 :gold 2}
    :points 8
    :color :brown
    :age 2}
   {:cost {:wood 1 :gold 1}
    :build :monument
    :points 1
    :color :brown
    :age 2}
   {:cost {:stone 3}
    :trade true
    :points 6
    :color :brown
    :age 2}
   {:cost {:stone 1 :gold 2}
    :tech :any-two
    :color :blue
    :age 2}
   {:cost {:stone 2 :gold 1}
    :tech :theo
    :temples {:chac 1 :kuku 1}
    :color :blue
    :age 2}
   {:cost {:gold 2}
    :temples {:kuku 2}
    :points 3
    :color :blue
    :age 2}
   {:cost {:stone 2}
    :temples {:chac 2}
    :points 2
    :color :blue
    :age 2}
   {:cost {:gold 3}
    :temples {:quet 2}
    :points 4
    :color :blue
    :age 2}
   {:cost {:stone 1 :gold 1}
    :tech :arch
    :color :blue
    :age 2}])

(def monuments
  [{:cost {:stone 3 :gold 3}
    :for-highest-track true
    :color :blue}
   {:cost {:stone 4 :gold 3}
    :for-multi-tracks true
    :color :blue}
   {:cost {:stone 2 :gold 3}
    :for-each-of-color :blue
    :color :blue}
   {:cost {:wood 2 :stone 3 :gold 1}
    :for-each-of-color :green
    :color :green}
   {:cost {:wood 1 :stone 1 :gold 3}
    :for-each-max-track true
    :color :green}
   {:cost {:wood 2 :stone 1 :gold 3}
    :for-each-track true
    :color :green}
   {:cost {:wood 3 :gold 3}
    :for-each-worker true
    :color :green}
   {:cost {:wood 1 :stone 3 :gold 2}
    :for-each-building-monument true
    :color :brown}
   {:cost {:wood 2 :stone 2 :gold 2}
    :for-monument-workers true
    :color :brown}
   {:cost {:wood 3 :stone 2 :gold 1}
    :for-each-of-color :brown
    :color :brown}
   {:cost {:wood 1 :stone 1 :gold 4}
    :for-each-tile :corn
    :color :brown}
   {:cost {:wood 1 :gold 4}
    :for-each-tile :wood
    :color :brown}])

(def starters
  [{:materials {:corn 5 :stone 1}
    :tech :theo
    :chi 3}
   {:materials {:corn 3 :wood 1}
    :farm 1
    :uxe 3}
   {:materials {:corn 9 :stone 1}
    :pal 7}
   {:materials {:corn 8 :gold 1}
    :uxe 7}
   {:materials {:corn 6 :wood 1 :stone 1}
    :tik 7}
   {:materials {:corn 2}
    :temple :chac
    :track :arch
    :tik 1}
   {:materials {:corn 5 :stone 1}
    :temple :chac
    :uxe 1}
   {:materials {:corn 5 :gold 1}
    :temple :quet
    :pal 3}
   {:materials {:corn 2 :wood 2}
    :tech :theo
    :chi 7}
   {:materials {:corn 4 :wood 1}
    :tech :extr
    :yax 5}
   {:materials {:corn 3 :gold 1}
    :tech :arch
    :tik 5}
   {:materials {:corn 3}
    :temple :quet
    :tech :agri
    :pal 1}
   {:materials {:corn 6 :stone 2}
    :tik 3}
   {:materials {:corn 3 :wood 2 :stone 1}
    :yax 7}
   {:materials {:stone 1 :gold 1}
    :tech :agri
    :pal 5}
   {:materials {:wood 1}
    :temple :kuku
    :tech :extr
    :yax 1}
   {:materials {:corn 4 :wood 1 :skulls 1}
    :chi 0}
   {:materials {:corn 4 :wood 3}
    :chi 5}
   {:materials {:corn 2 :wood 2}
    :temple :kuku
    :yax 3}
   {:gain-worker true
    :uxe 5}
   {:materials {:corn 7 :wood 2}
    :chi 10}])

(def gears
  {:yax {:name "Yaxchilan"
         :teeth 10
         :location 1
         :actions [[:gain-materials {:wood 1}]
                   [:gain-materials {:stone 1
                                     :corn 1}]
                   [:gain-materials {:gold 1
                                     :corn 2}]
                   [:gain-materials {:skull 1}]
                   [:gain-materials {:gold 1
                                     :stone 1
                                     :corn 2}]
                   [:choose-action-from :yax]
                   [:choose-action-from :yax]]}
   :tik {:name "Tikal"
         :teeth 10
         :location 6
         :actions [[:tech-step 1]
                   [:build :single]
                   [:tech-step 2]
                   [:build :double]
                   [:temple {:cost {:any-resource 1}
                             :track :choose-two-different}]
                   [:choose-action-from :tik]
                   [:choose-action-from :tik]]}
   :uxe {:name "Uxmal"
         :teeth 10
         :location 11
         :actions [[:temple {:cost {:corn 3}
                             :track :choose-one}]
                   [:trade nil]
                   [:gain-worker nil]
                   [:build :with-corn]
                   [:choose-action-from-all {:cost {:corn 1}}]
                   [:choose-action-from :uxe]
                   [:choose-action-from :uxe]]}
   :pal {:name "Palenque"
         :teeth 10
         :location 22
         :actions [[:gain-materials {:corn 3}]
                   [:gain-materials {:corn 4}]
                   [:choose-materials [{:corn 5}
                                       {:wood 2}]]
                   [:choose-materials [{:corn 7}
                                       {:wood 3}]]
                   [:choose-materials [{:corn 9}
                                       {:wood 4}]]
                   [:choose-action-from :pal]
                   [:choose-action-from :pal]]}
   :chi {:name "Chichen Itza"
         :teeth 13
         :location 16
         :actions [[:pay-skull {:points 4
                                :temple :chac}]
                   [:pay-skull {:points 5
                                :temple :chac}]
                   [:pay-skull {:points 6
                                :temple :chac}]
                   [:pay-skull {:points 7
                                :temple :kuku}]
                   [:pay-skull {:points 8
                                :temple :kuku}]
                   [:pay-skull {:points 8
                                :temple :kuku
                                :resource true}]
                   [:pay-skull {:points 10
                                :temple :quet}]
                   [:pay-skull {:points 11
                                :temple :quet
                                :resource true}]
                   [:pay-skull {:points 13
                                :temple :quet
                                :resource true}]
                   [:choose-action-from :chi]]}})

(def temples
  {:chac {:name "Chaac"
          :bonus [6 2]
          :steps [{:points -1}
                  {:points 0}
                  {:points 2
                   :material :stone}
                  {:points 4}
                  {:points 6
                   :material :stone}
                  {:points 7}
                  {:points 8}]}
   :quet {:name "Quetzalcoatl"
          :bonus [2 6]
          :steps [{:points -2}
                  {:points 0}
                  {:points 1}
                  {:points 2
                   :material :gold}
                  {:points 4}
                  {:points 6
                   :material :gold}
                  {:points 9}
                  {:points 12}
                  {:points 13}]}
   :kuku {:name "Kukulcan"
          :bonus [4 4]
          :steps [{:points -3}
                  {:points 0}
                  {:points 1
                   :material :wood}
                  {:points 3}
                  {:points 5
                   :material :wood}
                  {:points 7
                   :material :skull}
                  {:points 9}
                  {:points 10}]}})

(def spec
  {:total-turns 26
   :until-food-day [7 6 5 4 3 2 1 0 5 4 3 2 1 0 6 5 4 3 2 1 0 5 4 3 2 1 0]
   :trades {:wood 2 :stone 3 :gold 4}
   :num-available-buildings 4
   :num-available-monuments 4
   :tech tech
   :gears gears
   :temples temples
   :starters starters
   :buildings buildings
   :monuments monuments})
