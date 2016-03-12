(ns tzolkin.db
  (:require
   [tzolkin.spec :refer [spec]]
   [tzolkin.logic :as logic]
   [reagent.core :as rg]
   [tzolkin.utils :refer [log]]
   [matchbox.core :as m]
   [tzolkin.devcards.game :refer [s]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]
   [cljs.test :refer [testing is run-tests]]))

(def root
  (m/connect "https://bgames.firebaseio.com"))

(def connected-ref
  (m/connect "https://bgames.firebaseio.com/.info/connected"))

(def fb-connection (rg/atom {:connected false}))

(m/listen-to connected-ref :value #(swap! fb-connection assoc :connected (second %)))

(def fb-game (m/get-in root [:tzolkin]))

#_(rf/register-handler
    :class
    (fn [db [_ command id attribute value]]
      (case command
        :delete (m/dissoc-in! fb-classes [id])
        :update (m/reset-in!  fb-classes [id attribute] value)
        :new    (m/conj!      fb-classes (:class new-default)))
      db))

(defn setup-game-listener
  [es-atom]
  (m/listen-to fb-game :value (fn [[_ v]]
                                (reset! es-atom (:a v)))))

(defn save
  [es]
  (m/reset-in! fb-game [:a] es))
