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

(defn mk-layer [field text]
  {:mark {:type "line" :point true}
   :encoding {:y {:field field
                  :type :quantitative
                  :title "新增人数"}
              :color {:datum text}}
   })

;; ## 每日新增无症状人数
(clerk/vl
 {:title "每日新增无症状人数"
  :data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal :title "日期"}}
  :layer [(mk-layer :nosymptom "全部")
          (mk-layer :nosymptom-ctrl "隔离管控中")]
  })

;; ## 每日新增无症状人数（管控外）
(clerk/vl
 {:title "每日新增无症状人数（管控外）"
  :data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal :title "日期"}}
  :layer [(mk-layer :nosymptom-net "管控外无症状")]
  })

;; ## 每日新增确诊人数
(clerk/vl
 {:title "每日新增确诊人数"
  :data {:values ds}
  :width 600
  :height 500
  :encoding {:x {:field :date :type :temporal :title "日期"}}
  :layer [(mk-layer :confirmed "确诊全部")
          (mk-layer :confirmed-ctrl "管控中")
          (mk-layer :transformed "无症状转归")
          (mk-layer :confirmed-net "净增确诊")]
  })

