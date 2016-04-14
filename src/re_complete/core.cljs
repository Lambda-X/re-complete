(ns re-complete.core
  (:require [re-complete.handlers :as handlers]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as string]))

(defn completions
  "Render list of the items to autocomplete.
  Every item of the list is dispatched to the right place in the right input with :on-click event."
  ([linked-component-key]
   (completions linked-component-key nil))
  ([linked-component-key onclick-callback]
   (let [linked-component-keyword (keyword linked-component-key)
         items-to-re-complete (subscribe [:get-items-to-complete linked-component-keyword])
         current-word (subscribe [:get-previous-input linked-component-keyword])]
     (fn []
       (when-not (empty? @items-to-re-complete)
         (when-not (string/blank? @current-word)
           (into [:ul.re-completion-list]
                 (map (fn [item]
                        [:li.re-completion-item
                         {:on-click #(do (dispatch [:add-completed-word linked-component-keyword item])
                                         (dispatch [:clear-complete-items linked-component-keyword])
                                         (when onclick-callback
                                           (onclick-callback %)))}
                         item])
                      @items-to-re-complete))))))))
