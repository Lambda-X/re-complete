(ns re-complete.app
  (:require [re-complete.utils :as utils]
            [clojure.string :as string]))


(defn case-sensitivity [case-sensitive? dictionary-item input]
  (if case-sensitive?
    (string/starts-with? dictionary-item input)
    (string/starts-with? (string/lower-case dictionary-item) (string/lower-case input))))

(defn items-to-autocomplete
  "List of the items to autocomplete by given input and list of the all items"
  [case-sensitive? dictionary input]
  (if (= input nil)
    []
    (filter #(case-sensitivity case-sensitive? % input) dictionary)))

(defn index [previous-input input]
  "Finds the index where the change occured"
  (let [real-index (count (take-while #(identical? (first %) (second %))
                                      (map vector previous-input input)))]
    (if (< (count previous-input) (count input))
      real-index
      (- real-index 1))))

(defn current-word [input index]
  "Finds the current word based on index"
  (let [current-word-item (last (string/split (->> input
                                                   (take (inc index))
                                                   (reduce str))
                                              #" "))]
    (if (string/blank? (last input))
      ""
      current-word-item)))

(defn completions
  "Autocomplete options for word"
  [input dictionary options]
  (let [last-string (last (string/split input #" "))
        trim-chars (:trim-chars options)
        sort-fn (:sort-fn options)
        case-sensitive? (:case-sensitive? options)
        autocomplete-items (items-to-autocomplete case-sensitive? dictionary last-string)]
    (vec
     (if trim-chars
       (if (= (first last-string) (re-find (utils/str-to-pattern trim-chars) (str (first last-string))))
         (->> 1
              (subs last-string)
              (items-to-autocomplete case-sensitive? dictionary)
              (sort-by sort-fn))
         (sort-by sort-fn autocomplete-items))
       (sort-fn autocomplete-items)))))

(defn words-to-index
  "Words to change index"
  [index text]
  (let [text-to-index (-> index
                          inc
                          (take text))]
    (string/split (string/join "" text-to-index) #" ")))

(defn index-of-word
  "Index of word for autocompletion"
  [index text]
  (-> (words-to-index index text)
      count
      dec))

(defn index-in-word
  "Position index in word for autocompletion"
  [index text]
  (if (= (count (words-to-index index text)) 1)
    index
    (->> (words-to-index index text)
         drop-last
         (string/join "")
         count inc
         (- index))))

(defn autocomplete-word-with-trimmed-chars
  "Autocomplete word and ignore regex at the beginning and at the end of the word"
  [index-in-word word word-to-autocomplete trim-chars]
  (let [partitioned-by-trimmed-chars (vec (utils/partition-by-regexp word trim-chars))
        index-of-part-to-autocomplete (-> index-in-word
                                          inc
                                          (take word)
                                          (utils/partition-by-regexp trim-chars)
                                          count
                                          dec)]
    (->> (update-in partitioned-by-trimmed-chars [index-of-part-to-autocomplete]
                    #(str word-to-autocomplete))
         (string/join ""))))

(defn autocomplete-word-to-string
  "Autocomplete word with trimmed chars to input string"
  [index trim-chars text word-to-autocomplete]
  (->> (update-in (string/split text #" ") [(index-of-word index text)]
                  #(autocomplete-word-with-trimmed-chars (index-in-word index text)
                                                         %
                                                         word-to-autocomplete
                                                         trim-chars))
       (string/join " ")))
