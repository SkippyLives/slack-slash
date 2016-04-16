(ns  slack-slash.diceroller
  (:require 
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]))


;; parse text into a list
(defn parse-text [text]
  (let [parse (str/split text #"d")]
    (list (Integer. (first parse))
          (Integer. (last parse)))))

    
;; rolls one die of #sides
(defn roll-die [sides] 
  (+ (rand-int sides) 1))

;; main dice roller returns text result of rolls and total
(defn roll-dice [times sides]
  (loop [n times total 0 text ""] 
    (let [result (roll-die sides)]
    (if (zero? n)
      (format " rolled %dd%d:\n%s = %d" times sides text total)
      (recur (- n 1) (+ total result) (if (= n times) (format "%d" result) 
             (format "%s + %d" text result))))))) 
             
;; takes input and rolls 
(defn roll [text]
  (let [res (parse-text text)
        times (first res)
        sides (last res)]
    (roll-dice times sides)))



