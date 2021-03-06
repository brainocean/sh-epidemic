(ns collect-data
  (:require [net.cgrand.enlive-html :as e]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn map-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn re-find-multi
  "re-find multiple regex rs on txt"
  [txt rs]
  (keep #(re-find % txt) rs))

(defn default-to [d v]
  (if (empty? v) d v))

(defn parse-number [rs txt]
  (->> rs
       (re-find-multi txt)
       first                         ; first matched result is a list
       rest                          ; first is the whole match, rest are groups
       (clojure.string/join "-")
       (default-to "0")))

(defn parse-stat-page [url]
  (let [txt (-> (java.net.URL. (str "https://wsjkw.sh.gov.cn" url))
                (e/html-resource)
                (e/select [:div#ivs_content])
                first
                e/text)
        regexes {:date [ #"(\d{4})年(\d{1,2})月(\d{1,2})日" ]
                 :confirmed [ #"新增本土新冠肺炎确诊病例(\d+)例" ]
                 :nosymptom [ #"和无症状感染者(\d+)例" ]
                 :transformed [#"其中(\d+)例确诊病例为(?:此前|既往)?无症状感染者转归" #"既往无症状感染者转为确诊病例(\d+)例"]
                 :confirmed-ctrl [ #"(\d+)例确诊病例和(?:\d+)例无症状感染者在隔离管控中发现" ]
                 :nosymptom-ctrl [ #"(?:\d+)例确诊病例和(\d+)例无症状感染者在隔离管控中发现" ]
                 :confirmed-import [ #"新增境外输入性新冠肺炎确诊病例(\d+)例(?:和无症状感染者\d+例)?" #"新增境外输入性新冠肺炎确诊病例(\d+)例" ]
                 :nosymptom-import [ #"新增境外输入性新冠肺炎确诊病例(?:\d+)例和(?:无症状感染者(\d+)例)?" #"新增境外输入性新冠肺炎无症状感染者(\d+)例" ]
                 }
        ]

    (map-vals regexes #(parse-number % txt))))

(defn link-to-report-page? [elm]
  (-> (get-in elm [:attrs :title])
       (re-find-multi [#"新增本土新冠肺炎确诊病例" #"上海新增\d+例本土新冠肺炎确诊病例"])
       not-empty))

(defn parse-index-page [page-url]
  (let [elms (-> (java.net.URL.  (str  "https://wsjkw.sh.gov.cn/yqtb" page-url))
                 (e/html-resource)
                 (e/select [:ul.uli16 :li :a]))
        links (->> elms
                   ;; (filter #(re-find #"新增本土新冠肺炎确诊病例" (get-in % [:attrs :title] )))
                   (filter link-to-report-page?)
                   (map #(get-in % [:attrs :href]))
                   )]
    (mapv #(parse-stat-page %) links)))

(defn write-csv [content]
  (let [colmn (mapv name (keys (first content)))
        to-write (into [colmn] (mapv vals content))]
    (with-open [writer (io/writer "data/sh-epidemic.csv")]
      (csv/write-csv writer to-write))))

(defn collect-data []
  (write-csv
   (concat
    (parse-index-page "/index.html")
    (parse-index-page "/index_2.html")
    (parse-index-page "/index_3.html"))))

(comment
  (collect-data))

(comment
  (require '[clojure.tools.deps.alpha.repl :refer [add-libs]])

  (add-libs '{enlive/enlive {:mvn/version "1.1.6"}
              org.clojure/data.csv {:mvn/version "1.0.1"}})
  (require '[net.cgrand.enlive-html :as e]
           '[clojure.data.csv :as csv]
           '[clojure.java.io :as io])
  )
