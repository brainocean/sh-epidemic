(ns collect-data
  (:require [net.cgrand.enlive-html :as e]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn map-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn parse-number [r txt]
  (->> (re-find r txt)
       rest
       (mapv #(or % "0"))
       (clojure.string/join "-")))

(defn parse-stat-page [url]
  (let [txt (-> (java.net.URL. (str "https://wsjkw.sh.gov.cn" url))
                (e/html-resource)
                (e/select [:div#ivs_content])
                first
                e/text)
        regexes {:date #"(\d{4})年(\d{1,2})月(\d{1,2})日"
                 :confirmed #"确诊病例(\d+)例"
                 :nosymptom #"和无症状感染者(\d+)例"
                 :transformed #"其中(\d+)例确诊病例为此前无症状感染者转归"
                 :confirmed-ctrl #"(\d+)例确诊病例和(?:\d+)例无症状感染者在隔离管控中发现"
                 :nosymptom-ctrl #"(?:\d+)例确诊病例和(\d+)例无症状感染者在隔离管控中发现"
                 :confirmed-import #"新增境外输入性新冠肺炎确诊病例(\d+)例(?:和无症状感染者\d+例)?"
                 :nosymptom-import #"新增境外输入性新冠肺炎确诊病例(?:\d+)例(?:和无症状感染者(\d+)例)?"
                 }
        ]

    (map-vals regexes #(parse-number % txt))))

(defn parse-index-page [page-url]
  (let [elms (-> (java.net.URL.  (str  "https://wsjkw.sh.gov.cn/yqtb" page-url))
                 (e/html-resource)
                 (e/select [:ul.uli16 :li :a]))
        links (->> elms
                   (filter #(re-find #"新增本土新冠肺炎确诊病例" (get-in % [:attrs :title] )))
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
   (into
    (parse-index-page "/index.html")
    (parse-index-page "/index_2.html"))))

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
