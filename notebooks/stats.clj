(ns stats
  (:require [clojure.string :as str]
            [meta-csv.core :as csv]
            [nextjournal.clerk :as clerk]))

;; # 上海疫情统计分析

(def ds
  (csv/read-csv "./data/sh-epidemic.csv"))

(clerk/table ds)

(defn mk-layer [field color]
  {:mark {:type "line" :point true :color color}
   :encoding {:y {:field field
                  :type :quantitative}}})

;; ## 每日新增无症状人数
(clerk/vl
 {:data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal}}
  :layer [(mk-layer :nosymptom :orange)
          (mk-layer :nosymptom-ctrl :red)
          ]
  })

;; ## 每日新增确诊人数
(clerk/vl
 {:data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal}}
  :layer [(mk-layer :confirmed :green)
          (mk-layer :confirmed-ctrl :blue)
          ]
  })
