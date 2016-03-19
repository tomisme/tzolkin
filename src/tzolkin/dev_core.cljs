(ns tzolkin.dev-core
  (:require
   [tzolkin.devcards.main]
   [devcards.core]
   [devtools.core :as devtools]
   [tzolkin.utils :refer [log]])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg defcard-doc deftest]]))

(devtools/install!)

#_(log js/window)

(enable-console-print!)
