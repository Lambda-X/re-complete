(ns re-complete.app
  (:require [re-complete.utils :as utils]
            [clojure.string :as string]))

(defn items-to-autocomplete
  "List of the items to autocomplete by given input and list of the all items"
  [dictionary input]
  (let [new-items
        (if (= (re-find #"[aA-zZ]" (str (first input))) nil)
          []
          (if (= (string/upper-case (first input)) (first input))
            (map string/capitalize dictionary)
            (map string/lower-case dictionary)))]
    (filter #(string/starts-with? % input) new-items)))

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
  [input items options]
  (let [last-string (last (string/split input #" "))
        exclude-chars (:exclude-chars options)
        sort-fn (:sort-fn options)
        autocomplete-items (items-to-autocomplete items last-string)]
    (vec
     (if exclude-chars
       (if (= (first last-string) (re-find (utils/str-to-pattern exclude-chars) (str (first last-string))))
         (->> 1
              (subs last-string)
              (items-to-autocomplete items)
              (sort-by sort-fn))
         (sort-by sort-fn autocomplete-items))
       (sort-fn autocomplete-items)))))

(defn autocomplete-regex-word
  "Autocomplete word and ignore regex at the beginning and at the end of the word"
  [index-in-word word word-to-autocomplete regex-item]
  (let [partitioned-by-regex (vec (utils/partition-by-regexp word regex-item))
        index-of-part-to-autocomplete (-> index-in-word
                                          inc
                                          (take word)
                                          (utils/partition-by-regexp regex-item)
                                          count
                                          dec)]
    (->> (update-in partitioned-by-regex [index-of-part-to-autocomplete]
                    #(str word-to-autocomplete))
         (string/join ""))))

(defn autocomplete-word-to-string
  "Autocomplete regex-word to input string"
  [index regex-item text word-to-autocomplete]
  (let [text-to-index (-> index
                          inc
                          (take text))
        words-to-index (string/split (string/join "" text-to-index) #" ")
        index-of-word (-> words-to-index
                          count
                          dec)
        position-index-in-word (if (= (count words-to-index) 1)
                                 index
                                 (->> words-to-index
                                      drop-last
                                      (string/join "")
                                      count inc
                                      (- index)))]
    (->> (update-in (string/split text #" ") [index-of-word]
                    #(autocomplete-regex-word position-index-in-word % word-to-autocomplete regex-item))
         (string/join " "))))


(defn add-autocompleted-word
  "Updates text in app state"
  [db change-index word linked-component-key regex-item]
  (update-in db [:autocomplete :linked-components linked-component-key :text]
             #(autocomplete-word-to-string change-index regex-item % word)))

