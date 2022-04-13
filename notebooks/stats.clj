(ns stats
  (:require [clojure.string :as str]
            [meta-csv.core :as csv]
            [nextjournal.clerk :as clerk]))

;; # demo page
(def ds
  (csv/read-csv "./data/sh-epidemic.csv"))

(clerk/table ds)

(clerk/vl
 {:data {:values ds}
  :width 700
  :height 500
  :mark {:type "line"}
  :encoding {:x {:field :date
                 :type :temporal}
             :y {:field :nosymptom
                 :type :quantitative}}})
