(ns main.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

(def word-list ["alert" "loves" "cakes"])
(def target-word (first word-list))
(def keyboard-rows [["q" "w" "e" "r" "t" "y" "u" "i" "o" "p"]
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


(defn try-new-word
  []
  (swap! tries #(conj % @current-try)
  (reset! current-try "")))

(defn add-letter-to-try
  [letter]
  (when (< (count @current-try) 5)
    (swap! current-try #(str % letter))))

(defn backspace
  []
  (swap! current-try #(->> % drop-last (apply str))))

(defn render-key
  [key]
  [:button {:class "mx-3"
            :on-click (case key
                        :backspace #(backspace)
                        :enter try-new-word
                        #(add-letter-to-try key))}
   (case key
     :backspace "DELETE"
     :enter "ENTER"
     (str/upper-case key))])

(defn render-tried-letter
  [idx letter]
  [:span.text-6xl.text-white {:key idx
                              :class (get-letter-color idx letter)}
   letter])

(defn render-try
  [tried-word]
  (let [letters (into [] tried-word)]
    (map-indexed render-tried-letter letters)))

(defn render-keyboard-row
  [row]
  [:div {:class "flex flex-row my-3 place-content-center"}
           (map render-key row)])

(defn render-keyboard
  []
  [:div {:class "flex flex-col"}
   (map render-keyboard-row keyboard-rows)])

(defn component
  []
  [:div {:class "w-1/2 m-auto h-screen flex flex-col justify-around"}
   [:div {:class "aspect-5/6 grid gap-4 grid-cols-5 grid-rows-6 place-content-center"}
    (mapcat render-try (conj @tries @current-try))]
   (if (not (game-over?))
     [:div {:class "flex justify-center"}
      (render-keyboard)]
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
