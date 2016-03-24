(ns re-complete.utils
  (:require [clojure.string :as string]))

(defn str-to-pattern
  "Functions transform given string to pattern"
  [string]
  (->> string
       (map (partial str "\\"))
       (interpose "|")
       (apply str)
       re-pattern))


(defn partition-by-regexp
  "Function partitions characters from string into multiple strings by given regexp"
  [word trim-chars]
  (map #(string/join "" %)
       (partition-by #(re-find (str-to-pattern trim-chars) %) (mapv str word))))
