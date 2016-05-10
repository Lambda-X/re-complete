(ns re-complete.core
  (:require [re-complete.app :as app]
            [re-complete.handlers :as handlers]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as string]
            [reagent.core :as reagent]))

(defn completion-list
  "Render list of the items to autocomplete.
  Every item of the list is dispatched to the right place in the right input with :on-click event."
  ([linked-component-key onclick-callback]
   (let [linked-component-keyword (keyword linked-component-key)
         items-to-re-complete (subscribe [:get-items-to-complete linked-component-keyword])
         current-word (subscribe [:get-previous-input linked-component-keyword])
         selected-item (subscribe [:get-selected-item linked-component-keyword])
         is-mouse-on-suggestion-list? (subscribe [:is-mouse-on-suggestion-list linked-component-keyword])
         focus? (subscribe [:focus? linked-component-key])]
     (fn []
       (.log js/console (not @focus?))
       (let [selected @selected-item]
         (when (zero? (count @items-to-re-complete))
           (dispatch [:clear-selected-item linked-component-keyword]))
         [:ul {:className (str "re-completion-list " linked-component-key)
               :style {:visibility (if (or (not @focus?) (empty? @items-to-re-complete)) "hidden" "visible")}}
          (when-not (string/blank? @current-word)
            (map (fn [item]
                   (if (= (str (second selected))
                          (str item))
                     ^{:key item}
                     [:li.re-completion-selected
                      {:on-click #(do (dispatch [:add-completed-word linked-component-keyword item])
                                      (when onclick-callback
                                        (onclick-callback)))
                       :on-mouse-out #(dispatch [:mouse-on-suggestion-list linked-component-key false])}
                      item]
                     ^{:key item}
                     [:li.re-completion-item
                      {:on-click #(do (dispatch [:add-completed-word linked-component-keyword item])
                                      (when onclick-callback
                                        (onclick-callback)))
                       :on-mouse-over  #(when-not @is-mouse-on-suggestion-list?
                                          (dispatch [:selected-item linked-component-keyword item]))
                       :on-mouse-out #(dispatch [:mouse-on-suggestion-list linked-component-key false])}
                      item]))
                 @items-to-re-complete))])))))

(defn setup-key-handling [this linked-component-key onclick-callback]
  (let [node (reagent/dom-node this)
        current-view (atom [0 0])]
    (app/keys-handling (keyword linked-component-key) onclick-callback node current-view)))

(defn completions
  ([linked-component-key]
   (reagent/create-class
    {:component-did-mount (fn [this]
                            (setup-key-handling this linked-component-key nil))
     :reagent-render (fn [linked-component-key]
                       (completion-list linked-component-key nil))}))
  ([linked-component-key onclick-callback]
   (reagent/create-class
    {:component-did-mount (fn [this]
                            (setup-key-handling this linked-component-key onclick-callback))
     :reagent-render (fn [linked-component-key onclick-callback]
                       (completion-list linked-component-key onclick-callback))})))
