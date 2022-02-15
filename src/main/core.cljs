(ns main.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

; TODO grab all five-letter words, maybe straight from wordle?
(def word-list ["alert" "loves" "cakes"])

 ; TODO choose word based on day
(def target-word (first word-list))

(def keyboard-rows [["q" "w" "e" "r" "t" "y" "u" "i" "o" "p"]
                    ["a" "s" "d" "f" "g" "h" "j" "k" "l"]
                    [:enter "z" "x" "c" "v" "b" "n" "m" :backspace]])

(def green "bg-lime-600")
(def yellow "bg-yellow-500")
(def grey "bg-stone-600")
(def black "bg-black")

(defonce current-try (r/atom ""))

(defonce tries (r/atom ["arbys" "farts"]))

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
  [:button {:class (str "mx-3 " (get @tried-letters key))
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
  [:span.text-6xl.text-white {:key (str idx letter)
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
    (mapcat render-try @tries)
    ;; TODO this is ugly, deduplicate w/ `render-try` somehow
    (->> @current-try (map-indexed #(vector :span.text-6xl.text-white {:key (str %1 %2)
                                                                       :class black}
                                            %2)))]
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
  )
