(ns slack-slash.handler
  (:gen-class) ;; for standalone
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [slack-slash.diceroller :refer [roll]]
            [slack-slash.rps :refer :all]
            [ring.adapter.jetty :refer :all] ;; for standalone
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]))


;; code therapy
(def hook-url "")
(def roll-auth-token "")
(def rps-auth-token "")


(def dicebot 
  {:username "dicebot"
   :icon_emoji ":game_die:"})

(def rpsbot
  {:username "RPSBot"
   :icon_emoji ":punch:"})

;; actually makes the call to the incoming webhook.
(defn post-to-slack [url botinfo msg]
  (let [m (merge botinfo msg)]
    (client/post url {:body (json/write-str m)
                      :content-type :json})))

;; verifies 2d4 type text input
(defn check-dice [text]
  (re-matches #"^\d{1,2}d\d{1,3}$" (str/trim text)))

;; make sure commands/weapon are valid
(defn check-rps [text]
  (let [parse (str/split text #" ")]
    (if (= (count parse) 1)
      (if (or (> (.indexOf slack-slash.rps/commands1 (first parse)) -1)
              (> (.indexOf slack-slash.rps/weapons (first parse)) -1))
        true
        false)
      (if (= (count parse) 3)
        (if (and (> (.indexOf slack-slash.rps/weapons (last parse)) -1)
                 (> (.indexOf slack-slash.rps/commands (first parse)) -1))
          true
          false)
        false))))

(defroutes app-routes
  ;; Dicebot
  (POST "/roll" {:keys [params] :as request}
    (if (and (= "/roll" (:command params))
             (= roll-auth-token (:token params))
             (check-dice (:text params)))
      (do
        (post-to-slack hook-url dicebot {:text (format "%s %s" (clojure.string/trim (:user_name params)) (slack-slash.diceroller/roll (clojure.string/trim (:text params))))})
        {:status 200
         :content-type "text/plain"})
      {:status 400
       :content-type "text/plain"
       :body "You need to provide <num>d<sides> "}))
  ;;RPS
  (POST "/rps" {:keys [params] :as request}
    (if (and (= "/rps" (:command params))
             (= rps-auth-token (:token params))
             (check-rps (:text params)))
      (do
        (post-to-slack hook-url rpsbot {:text (format "%s" (slack-slash.rps/process-input (clojure.string/trim (:user_name params)) (clojure.string/trim (:text params))))})
        {:status 200
         :content-type "text/plain"})
      {:status 400
       :content-type "text/plain"
       :body "Usage: /rps <challenge|accept|standings> <member> rock|paper|scissors"}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (run-jetty app {:port 3001}))

