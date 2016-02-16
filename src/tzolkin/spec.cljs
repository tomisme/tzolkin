(ns tzolkin.spec)

(def spec
  {:total-turns 26
   :until-food-day [7 6 5 4 3 2 1 0 5 4 3 2 1 0 6 5 4 3 2 1 0 5 4 3 2 1 0]
   :buildings [{:cost {:stone 1
                       :gold 1}
                :points 3
                :free-tech :agri}
               {:cost {:wood 3}
                :materials {:stone 1}
                :free-tech :agri}]
   :monuments {:cost {:wood 1
                      :stone 3
                      :gold 2}
               :per :buildings-and-monuments
               :points 2}
   :starting [{:materials {:corn 6
                           :wood 1
                           :stone 1}
               :gear :tik}
              {:materials {:corn 6
                           :wood 1
                           :stone 1}}]
   :trades {:wood 2
            :sone 3
            :gold 4}
   :tech {:agri {}
          :extr {}
          :arch {}
          :theo {}}
   :gears
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
           :actions [[:tech-step :single]
                     [:build :single]
                     [:tech-step :double]
                     [:build :double]
                     [:god-track {:cost {:any-resource 1}
                                  :track :choose-two-different}]
                     [:choose-action-from :tik]
                     [:choose-action-from :tik]]}

     :uxe {:name "Uxmal"
           :teeth 10
           :location 11
           :actions [[:god-track {:cost {:corn 3}
                                  :track :choose-one}]
                     [:trade nil]
                     [:gain-worker nil]
                     [:build :with-corn]
                     [:choose-action-from-all {:cost {:corn 1}}]
                     [:choose-action-from :uxe]
                     [:choose-action-from :uxe]]}
     :chi {:name "Chichen Itza"
           :teeth 13
           :location 16
           :actions [[:pay-skull {:points 4
                                  :god :chac}]
                     [:pay-skull {:points 5
                                  :god :chac}]
                     [:pay-skull {:points 6
                                  :god :chac}]
                     [:pay-skull {:points 7
                                  :god :kuku}]
                     [:pay-skull {:points 8
                                  :god :kuku}]
                     [:pay-skull {:points 8
                                  :god :kuku
                                  :resource true}]
                     [:pay-skull {:points 10
                                  :god :quet}]
                     [:pay-skull {:points 11
                                  :god :quet
                                  :resource true}]
                     [:pay-skull {:points 13
                                  :god :quet
                                  :resource true}]
                     [:choose-action-from :chi]]}

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
                     [:choose-action-from :pal]]}}
   :temples {:chac {:name "Chaac"
                    :bonus {:age1 6
                            :age2 2}
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
                    :bonus {:age1 2
                            :age2 6}
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
                    :bonus {:age1 4
                            :age2 4}
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
                            {:points 10}]}}})
