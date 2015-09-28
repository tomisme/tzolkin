(ns tzolkin.util)

(defn e->val
  [event]
  (-> event .-target .-value))
