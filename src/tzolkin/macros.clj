(ns tzolkin.macros
  (:require
   [clojure.data :as data :refer [diff]]
   [cljs.test :refer [is]]
   [pl.danieljanus.tagsoup :as ts]))


(defmacro nod-old
  [a b]
  `(is (= (drop-last (diff ~a ~b)) '(nil nil))))


(defmacro nod
  "nod or 'no difference' emits an 'is' statement from cljs.test that checks
   equality between two args 'a' and 'b'. Useful when a test failure would
   print off a huge comparison (e.g. comparing large maps ) as this just shows
   the parts that are different."
  [a b]
  (list 'is
        (list '=
              (list 'drop-last
                    (list 'clojure.data/diff a b))
              ''(nil nil))))


(defmacro embed-svg [file]
  (let [hiccup (ts/parse-string (slurp (str "resources/public/images/" file)))]
    `~hiccup))
