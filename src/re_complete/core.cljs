(ns re-complete.core
  (:require [re-complete.handlers :as handlers]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as string]))

(defn autocompletion-list
  "Render list of the items to autocomplete.
  Every item of the list is dispatched to the right place in the right input with :on-click event."
  [linked-component-key]
  (let [linked-component-keyword (keyword linked-component-key)
        items-to-autocomplete (subscribe [:get-items-to-autocomplete linked-component-keyword])
        current-word (subscribe [:get-previous-input linked-component-keyword])]
    (fn []
      (when-not (string/blank? @current-word)
        (into [:ul.autocompletion-list]
              (map (fn [item]
                     [:li.autocompletion-item
                      {:on-click #(do (dispatch [:add-autocompleted-word linked-component-keyword item])
                                      (dispatch [:clear-autocomplete-items linked-component-keyword]))}
                      item])
                   @items-to-autocomplete))))))
