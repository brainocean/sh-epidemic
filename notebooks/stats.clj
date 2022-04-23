(ns stats
  (:require [clojure.string :as str]
            [meta-csv.core :as csv]
            [nextjournal.clerk :as clerk]))

;; # 上海疫情统计分析


(defn confirmed-net [rec]
  {:confirmed-net  (- (:confirmed rec)
                      (:confirmed-ctrl rec)
                      (Integer/parseInt (or  (str (:transformed rec)) "0")))})

(defn nosymptom-net [rec]
  {:nosymptom-net (- (:nosymptom rec) (:nosymptom-ctrl rec))})

(def ds
  (->> (csv/read-csv "./data/sh-epidemic.csv")
       (map #(merge % (confirmed-net %) (nosymptom-net %)))
       ))

(clerk/table ds)

(defn mk-layer [field color]
  {:mark {:type "line" :point true :color color}
   :encoding {:y {:field field
                  :type :quantitative}}})

;; ## 每日新增无症状人数
(clerk/vl
 {:title "每日新增无症状人数"
  :data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal :title "日期"}}
  :layer [(mk-layer :nosymptom :green)
          (mk-layer :nosymptom-ctrl :blue)]
  })

;; ## 每日新增无症状人数（管控外）
(clerk/vl
 {:data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal}}
  :layer [(mk-layer :nosymptom-net :red)]
  })

;; ## 每日新增确诊人数
(clerk/vl
 {:data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal}}
  :layer [(mk-layer :confirmed :green)
          (mk-layer :confirmed-ctrl :blue)
          (mk-layer :transformed :orange)
          (mk-layer :confirmed-net :red)]
  })

