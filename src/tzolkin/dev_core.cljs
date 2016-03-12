(ns tzolkin.dev-core
  (:require
   [tzolkin.devcards.main]
   [devtools.core :as devtools]
   [tzolkin.utils :refer [log]]))

(devtools/install!)

#_(log js/window)

(enable-console-print!)
