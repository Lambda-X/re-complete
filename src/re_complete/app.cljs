(ns re-complete.app
  (:require [re-complete.utils :as utils]
            [clojure.string :as string]))

(defn autocomplete
  "Function finds the index where the change occured and based on this index makes appropriate changes"
  [db linked-component-key previous-input input autocomplete-data options]
  (let [index (count (take-while #(identical? (first %) (second %))
                                 (map vector previous-input input)))
        new-index (if (< (count previous-input) (count input))
                    index
                    (- index 1))
        current-word-item (last (string/split (->> input
                                                   (take (inc new-index))
                                                   (reduce str))
                                              #" "))
        current-word (if (string/blank? (last input))
                       ""
                       current-word-item)]
    (-> db
        (assoc-in [:autocomplete :linked-components (keyword linked-component-key) :change-index]
                  new-index)
        (assoc-in [:autocomplete :linked-components (keyword linked-component-key) :current-word]
                  current-word)
        (assoc-in [:autocomplete :linked-components (keyword linked-component-key) :completions]
                  (vec (utils/autocomplete-options current-word autocomplete-data options))))))

(defn add-autocompleted-word
  "Updates text in app state"
  [db change-index word linked-component-key regex-item]
  (update-in db [:autocomplete :linked-components linked-component-key :text]
             #(utils/autocomplete-word-to-string change-index regex-item % word)))
