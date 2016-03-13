(ns tzolkin.utils
  (:require
   [clojure.data :as data]))

(defn log
  [arg]
  (.log js/console arg) arg)

(defn diff
  [a b]
  (drop-last (data/diff a b)))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 'seq' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [coll]
  (map-indexed vector coll))

(defn first-val
  "Returns the index of the first instance of nil in 'coll'"
  [coll val]
  (first (for [[index element] (indexed coll) :when (= element val)] index)))

(defn rotate-vec
  "Circularly shifts items in a vector forward 'num' times.

  (rotate-vec [:a :b :c :d :e] 2)  =>  [:d :e :a :b :c]"
  [vec num]
  (let [length (count vec)
        rotations (mod num length)
        break (- length rotations)]
    (into (subvec vec break) (subvec vec 0 break))))

(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1  =>  [:a :c])"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn change-map
  "Applies a function 'f' to each value in  'original-map' that has a
  corresponding key in 'changes', supplying the value of that key as the first
  argument to the function.

  If a map of changes is not supplied, applies 'f' to every value.

  (change-map {:a 1 :b 1} + {:a 2})  =>  {:a 3 :b 1}
  (change-map {:a 1 :b 1} inc)  =>  {:a 2 :b 2}"
  ([original-map f]
   (into {} (for [[k v] original-map] [k (f v)])))
  ([original-map f changes]
   (reduce
     (fn [m [k v]] (update m k #(f % v)))
     original-map
     (for [[k v] changes] [k v]))))

(defn negatise-map
  "Multiple each value in map 'm' by -1"
  [m]
  (change-map m #(* % -1)))
