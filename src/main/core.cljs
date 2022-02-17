(ns main.core
  (:require [main.wordlist :refer [daily-word-list]]
            [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

(def target-word (first daily-word-list))

(def keyboard-rows [["q" "w" "e" "r" "t" "y" "u" "i" "o" "p"]
                    ["a" "s" "d" "f" "g" "h" "j" "k" "l"]
                    [:enter "z" "x" "c" "v" "b" "n" "m" :backspace]])

(def green "bg-lime-600")
(def yellow "bg-yellow-500")
(def grey "bg-stone-600")
(def black "bg-stone-900")

(defonce current-try (r/atom ""))

(defonce tries (r/atom []))

(defonce tried-letters (r/atom (->> keyboard-rows
                                    flatten
                                    (remove keyword?)
                                    (map #(vector % black))
                                    (into {}))))

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
      (= idx-in-current-word idx-in-target) green
      (not (nil? idx-in-target)) yellow
      :else grey)))

(defn try-new-word
  []
  (let [current-try-val @current-try
        new-letter-colors (map-indexed get-letter-color current-try-val)
        new-letters (mapcat #(hash-map %1 %2) current-try-val new-letter-colors)
        ; if a player got a green letter previously, don't overwrite it
        new-letters (remove #(= green (get @tried-letters (first %))) new-letters)]
    (swap! tries #(conj % current-try-val))
    (reset! current-try "")
    (swap! tried-letters #(merge % new-letters))))

(defn add-letter-to-try
  [letter]
  (when (< (count @current-try) 5)
    (swap! current-try #(str % letter))))

(defn backspace
  []
  (swap! current-try #(->> % drop-last (apply str))))

(defn render-key
  [key]
  [:button {:class (str "py-4 px-2.5 md:px-3 mx-0.5 border " (get @tried-letters key))
            :on-click (case key
                        :backspace #(backspace)
                        :enter try-new-word
                        #(add-letter-to-try key))}
   (case key
     :backspace "DELETE"
     :enter "ENTER"
     (str/upper-case key))])

(defn render-tried-letter
  [idx letter color]
  [:p.text-3xl.md:text-4xl.border.w-16.md:w-20.aspect-square.inline-flex.justify-center.items-center
   {:key (str idx letter) :class color}
   (str/upper-case letter)])

(defn render-try
  [tried-word]
  (let [letters (into [] tried-word)]
    (map-indexed #(render-tried-letter %1 %2 (get-letter-color %1 %2)) letters)))

(defn render-remaining-tries
  []
  (let [remaining-tries (- 5 (count @tries))
        remaining-letters (+ (- 5 (count @current-try)) (* 5 remaining-tries))]
    (repeat remaining-letters [:span.border.w-16.md:w-20.aspect-square {:class black}])))

(defn render-keyboard-row
  [row]
  [:div {:class "flex flex-row my-1 place-content-center"}
   (map render-key row)])

(defn render-keyboard
  []
  [:div {:class "flex flex-col text-sm"}
   (map render-keyboard-row keyboard-rows)])

(defn component
  []
  [:div {:class "w-full m-auto h-screen flex flex-col justify-around items-center bg-stone-900 text-white"}
   [:div {:class "w-100 grid font-bold gap-2 grid-cols-5 grid-rows-6 place-content-center"}
    (mapcat render-try @tries)
    (->> @current-try (map-indexed #(render-tried-letter %1 %2 black)))
    (render-remaining-tries)]
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
  (reset! tries ["farts"])
  )
