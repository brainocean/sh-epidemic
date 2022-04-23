(ns user
  (:require [nextjournal.clerk :as clerk]))

;; start Clerk's buit-in webserver on the default port 7777, opening the browser when done
(clerk/serve! {:browse? true})

;; or let Clerk watch the given `:paths` for changes
(clerk/serve! {:watch-paths ["notebooks"]})

(do
  (clerk/clear-cache!)
  (clerk/show! "notebooks/stats.clj"))
