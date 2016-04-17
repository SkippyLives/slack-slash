(ns slack-slash.rps
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.string :as str]))

;; command list for single arg
(def commands1 ["standings","matches"])

;; command list for multiple args
(def commands ["challenge","accept"])

;; weapon list
(def weapons ["rock","paper","scissors"])
;; match data structure
(defn match 
  [cn on w]
  {:challengerName cn
   :oppenentName on
   :weapon w})

;; player record data structure
(def player-record
  {:userName ""
   :wins 0
   :losses 0})

(defn match-list []
  ())

(defn standings []
  ())

;; parse text into a list
(defn parse-text [text]
  (str/split text #" "))

(defn display-standings []
  (format "Standings\n%s" (standings)))

(defn display-matches []
  (format "Pending Matches\n%s" (match-list)))

(defn display [cmd]
  (if (= cmd "standings")
    (display-standings)
    (display-matches)))

(defn play-bot 
  [challenger-name challenger-weapon]
  (format "%s chose %s" challenger-name challenger-weapon))

(defn challenge
  [challenger-name challenger-weapon opponent-name]
  (format "%s challenges %s" challenger-name opponent-name))

(defn accept
  [opponent-name opponent-weapon challenger-name]
  (format "%s accepts %s challenge" opponent-name challenger-name))

(defn process-input
  [username commandline]
  (let [parse (parse-text commandline)]
    (if (= (count parse) 1)
      (if (> (.indexOf commands1 (first parse)) -1)
        (display (first parse))
        (play-bot username (first parse)))
      (if (= (first parse) "challenge")
        (challenge username (get parse 2) (get parse 1))
        (accept username (get parse 2) (get parse 1))))))


