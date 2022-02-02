(ns main.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

(def max-tries 6)
(def word-length 5)
(def word-list ["alert" "loves" "cakes"])
(def target-word (first word-list))

(defonce tries (r/atom ["arbys" "farts"]))

(defn get-letter-color
  [idx-in-current-word letter]
  (let [idx-in-target (str/index-of target-word letter)]
    (cond
      (= idx-in-current-word idx-in-target) "green"
      (not (nil? idx-in-target)) "yellow"
      :else "gray")))

(defn render-tried-letter
  [idx letter]
  [:td>span {:key idx
             :style {:color "white" :background-color (get-letter-color idx letter)}}
   letter])

(defn render-try
  [tried-word]
  (let [letters (into [] tried-word)]
    [:tr (map-indexed render-tried-letter letters)]))

(defn try-new-word
  [word]
  (swap! tries #(conj %1 word)))

(defn update-try
  [current-try value]
  (if (> (count value) 5)
    (js/alert "Word must be five letters long!")
    (reset! current-try value)))

(defn component
  []
  (let [current-try (r/atom "")]
    (fn []
      [:div [:table>tbody (map render-try @tries)]
       [:input {:type "text"
                :value @current-try
                :on-change #(update-try current-try (-> % .-target .-value))}]
       [:button {:on-click #(try-new-word @current-try)
                 :disabled (< 5 (count @tries))} "enter"]])))

(defn main
  []
  (dom/render [component] (.getElementById js/document "clojurdle-root")))
