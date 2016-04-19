(ns re-complete.core
  (:require [re-complete.app :as app]
            [re-complete.handlers :as handlers]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as string]
            [reagent.core :as reagent]))

(defn completion-list
  "Render list of the items to autocomplete.
  Every item of the list is dispatched to the right place in the right input with :on-click event."
  [linked-component-key onclick-callback]
  (let [linked-component-keyword (keyword linked-component-key)
        items-to-re-complete (subscribe [:get-items-to-complete linked-component-keyword])
        current-word (subscribe [:get-previous-input linked-component-keyword])
        selected-item (subscribe [:get-selected-item linked-component-keyword])]
    (app/keys-handling linked-component-keyword onclick-callback)
    (fn []
      (let [selected @selected-item]
        [:ul.re-completion-list {:style {:display (if (empty? @items-to-re-complete) "none" "block")}}
         (when-not (string/blank? @current-word)
           (map (fn [item]
                  (if (= (str (second selected))
                         (str item))
                    [:li.re-completion-select
                     {:on-click #(do (dispatch [:add-completed-word linked-component-keyword item])
                                     (when onclick-callback
                                       (onclick-callback)))}
                     item]
                    [:li.re-completion-item
                     {:on-click #(do (dispatch [:add-completed-word linked-component-keyword item])
                                     (when onclick-callback
                                       (onclick-callback)))}
                     item]))
                @items-to-re-complete))]))))


(defn setup-key-handling []
  (fn [this]
    (let [node (reagent/dom-node this)]
      (.scrollTop node 20))))

(defn completions
  ([linked-component-key]
   (reagent/create-class
    {:component-did-mount setup-key-handling
     :reagent-render (fn [linked-component-key]
                       (completion-list linked-component-key nil))}))
  ([linked-component-key onclick-callback]
   (reagent/create-class
    {:component-did-mount setup-key-handling
     :reagent-render (fn [linked-component-key onclick-callback]
                       (completion-list linked-component-key onclick-callback))})))
