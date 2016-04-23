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

;; matches Atom for global matches list
(def matches 
  (atom {}))

;; standings Atom for global matches list
(def standings 
  (atom {"RPSBot" {:wins 0 :losses 0}}))


;; parse text into a list
(defn parse-text [text]
  (str/split text #" "))

;; get a random weapon
(defn rand-weapon []
  (rand-nth weapons))

;; MATCHES FUNCTONS
(defn split-players
    [players]
    (str/split players  #"-"))

(defn combine-players
    [challenger-name opponent-name]
      (format "%s-%s" challenger-name opponent-name))

(defn add-match [challenger-name challenger-weapon opponent-name]
    (swap! matches conj {(combine-players challenger-name opponent-name) challenger-weapon}))

(defn remove-match [challenger-name opponent-name]
    (swap! matches dissoc (combine-players challenger-name opponent-name)))

(defn format-matches
  [keyval]
  (format "%s challenges %s\n" (first (split-players (key keyval))) (last (split-players (key keyval)))))

(defn display-matches []
  (let [arr (seq @matches)]
    (loop [text "PENDING MATCHES\n" x arr]
      (if (< (count x) 1)
        (format "%s" text)
        (recur (str text (format-matches (first x))) (rest x))))))

(defn search-matches [challenger-name opponent-name]
  (get-in @matches [(combine-players challenger-name opponent-name)]))

;; STANDINGS FUNCTIONS
(defn format-standings 
     [keyval]
  (format "%s - Wins %d, Losses %d\n" (key keyval) (:wins (val keyval)) (:losses (val keyval))))

(defn display-standings []
  (let [arr (seq @standings)]
    (loop [text "   STANDINGS\n" x arr]
      (if (< (count x) 1)
        (format "%s" text)
        (recur (str text (format-standings (first x))) (rest x))))))

(defn search-standings [user-name]
  (get @standings user-name)) 

(defn add-winner [winner]
  (if (nil? (search-standings winner))
    (swap! standings conj {winner {:wins 1, :losses 0}})
    (swap! standings update-in [winner :wins] inc))) 

(defn add-loser [loser]
  (if (nil? (search-standings loser))
    (swap! standings conj {loser {:wins 0, :losses 1}})
    (swap! standings update-in [loser :losses] inc)))

(defn update-standings
  [winner loser]
  (add-winner winner)
  (add-loser loser))
  
(defn display [cmd]
  (if (= cmd "standings")
    (display-standings)
    (display-matches)))

;; determine outcome of 2 weapons
(defn get-outcome
  [opponent-weapon challenger-weapon]
  (if (= opponent-weapon challenger-weapon)
    (int 0)
    (case opponent-weapon
     "rock" (if (= challenger-weapon "scissors")
        (int 1)
        (int 2))
     "paper" (if (= challenger-weapon "rock")
          (int 1)
          (int 2))
     "scissors" (if (= challenger-weapon "paper")
          (int 1)
          (int 2)))))

;; determines winner and if one updates standings. 
;; returns string for display
(defn get-winner 
  [opponent-name opponent-weapon challenger-name challenger-weapon]
  (let [result (get-outcome opponent-weapon challenger-weapon)
        text (format "Challenge Accepted!\n%s chose %s\n%s chose %s\n"
                     opponent-name opponent-weapon
                     challenger-name challenger-weapon)]
    ;;(format "%s vs %s = %d" opponent-weapon challenger-weapon result)
    (case result
      (0) (format "%s %s" text "Its a Tie!\n")
      (1) (do
            (update-standings opponent-name challenger-name)
            (format "%s %s Wins!\n" text opponent-name))
      (2) (do
            (update-standings challenger-name opponent-name)
            (format "%s %s Wins!\n" text challenger-name)))))


;; user is challenging bot so pick a random weapon and check outcome
(defn play-bot 
  [challenger-name challenger-weapon]
  (let [opponent-name "RPSBot"
        opponent-weapon (rand-weapon)]
    (get-winner opponent-name opponent-weapon challenger-name challenger-weapon)))

(defn play-match
  [opponent-name opponent-weapon challenger-name]
  (let [challenger-weapon (search-matches challenger-name opponent-name)]
    (remove-match challenger-name  opponent-name)
    (get-winner opponent-name opponent-weapon challenger-name challenger-weapon)))

(defn challenge
  [challenger-name challenger-weapon opponent-name]
  (if (nil? (search-matches challenger-name  opponent-name)) 
    (add-match challenger-name challenger-weapon opponent-name))
  (format "%s challenges %s" challenger-name opponent-name))

(defn accept
  [opponent-name opponent-weapon challenger-name]
  (if (nil? (search-matches challenger-name  opponent-name))
    (format "There is no pending match")
    (play-match opponent-name opponent-weapon challenger-name)))

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


