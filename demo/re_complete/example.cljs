(ns re-complete.example
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-complete.core :as re-complete]
            [re-complete.dictionary :as dictionary]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   dispatch
                                   dispatch-sync
                                   register-sub
                                   subscribe]]
            [clojure.string :as string]))


;; Initial state

(def initial-state {})

;; --- Handlers ---

(register-handler
 :initialize
 (fn
   [db _]
   (merge db initial-state)))


(register-handler
 :add-item-to-list
 (fn
   [db [_ list-name input]]
   (update-in db [(keyword list-name) :added-items] #(vec (conj % input)))))


(register-handler
 :clear-input
 (fn
   [db [_ linked-component-key]]
   (assoc-in db [:re-complete :linked-components (keyword linked-component-key) :text] "")))

;; --- Subscription Handlers ---

(register-sub
 :get-list
 (fn
   [db [_ list-name]]
   (reaction (get-in @db [(keyword list-name) :added-items]))))


;; --- VIEW --- ;;

(def my-lists [["vegetable" dictionary/vegetables {:trim-chars "[]()"}]
               ["fruit" dictionary/fruits {:trim-chars "?"
                                           :case-sensitive? true}]
               ["grain" dictionary/grains]])

(defn list-view [items]
  (map (fn [item]
         [:li.item item])
       items))

(defn render-list
  ([list-name dictionary]
   (render-list list-name dictionary nil))
  ([list-name dictionary options]
   (let [get-input (subscribe [:get-previous-input list-name])
         get-list (subscribe [:get-list list-name])]
     (dispatch [:options list-name options])
     (dispatch [:dictionary list-name dictionary])
     (fn []
       [:div {:className (str list-name " my-list")}
        [:div {:className "panel panel-default re-complete"}
         [:div {:className "panel-heading"}
          [:h1 (string/capitalize (str list-name "s"))]]
         [:div.panel-body
          [:ul.checklist
           [:li.input
            [:input {:type "text"
                     :className "form-control input-field"
                     :placeholder (str list-name " name")
                     :value @get-input
                     :on-change (fn [event]
                                  (dispatch [:input list-name (.. event -target -value)]))}
             [:button {:type "button"
                       :className "btn btn-default button-ok"
                       :on-click #(do (dispatch [:add-item-to-list list-name @get-input])
                                      (dispatch [:clear-input list-name]))}
              [:span {:className "glyphicon glyphicon-ok check"}]]]]
           (list-view @get-list)]]
         [:div.re-completion-list-part
          [re-complete/completions list-name]]]]))))

(defn my-app []
  (into [:div.my-app]
        (map #(into [render-list] %) my-lists)))

;; --- Main app fn ---

(defn ^:export main []
  (dispatch-sync [:initialize])
  (reagent/render [my-app] (.getElementById js/document "app")))
