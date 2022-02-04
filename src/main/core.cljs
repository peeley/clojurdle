(ns main.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

(def word-list ["alert" "loves" "cakes"])
(def target-word (first word-list))
(def keyboard-keys [["q" "w" "e" "r" "t" "y" "u" "i" "o" "p"]
                    ["a" "s" "d" "f" "g" "h" "j" "k" "l"]
                    [:enter "z" "x" "c" "v" "b" "n" "m" :backspace]])

(defonce current-try (r/atom ""))
(defonce tries (r/atom ["arbys" "farts"]))

(defn player-won?
  []
  (= (last @tries) target-word))

(defn player-lost?
  []
  (< 5 (count @tries)))

(defn game-over?
  []
  (or (player-won?) (player-lost?)))

(defn get-letter-color
  [idx-in-current-word letter]
  (let [idx-in-target (str/index-of target-word letter)]
    (cond
      (= idx-in-current-word idx-in-target) "bg-lime-600"
      (not (nil? idx-in-target)) "bg-yellow-500"
      :else "bg-stone-600")))

(defn render-tried-letter
  [idx letter]
  [:td>span {:key idx
             :class (str "text-white " (get-letter-color idx letter))}
   letter])

(defn render-try
  [tried-word]
  (let [letters (into [] tried-word)]
    [:tr (map-indexed render-tried-letter letters)]))

(defn try-new-word
  [word]
  (reset! current-try "")
  (swap! tries #(conj %1 word)))

(defn update-try
  [value]
  (when (< (count value) 6)
    (reset! current-try value)))

(defn component
  []
  [:div [:table.table-auto>tbody (map render-try @tries)]
   (if (not (game-over?))
     [:div
      [:input {:type "text"
               :value @current-try
               :on-change #(update-try (-> % .-target .-value))}]
      [:button {:on-click #(try-new-word @current-try)
                :disabled (not= 5 (count @current-try))} "enter"]]
     [:div
      (if (player-won?) [:p "Congrats!"]
                        [:p (str "The word of the day is `" target-word "`.")])])])

(defn main
  []
  (dom/render [component] (.getElementById js/document "clojurdle-root")))

(comment
  (reset! tries ["arbys" "farts" "grasp"])
  @tries
  target-word
  )
