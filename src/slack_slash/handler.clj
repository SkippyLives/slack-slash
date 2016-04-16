(ns slack-slash.handler
  (:gen-class) ;; for standalone
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [slack-slash.diceroller :refer [roll]]
            [ring.adapter.jetty :refer :all] ;; for standalone
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]))



(def hook-url "https://hooks.slack.com/services/<your incoming webhook urlcode here>")
(def roll-auth-token "<your slash token here>")

;; actually makes the call to the incoming webhook.
(defn post-to-slack [url msg]
   (let [m (merge {:username "dicebot"
                   :icon_emoji ":game_die:"} msg)]
      (client/post url {:body (json/write-str m)
                      :content-type :json})))

;; verifies 2d4 type text input
(defn check-dice [text]
   (re-matches #"^\d{1,2}d\d{1,3}$" (str/trim text)))


(defroutes app-routes
  (POST "/roll" {:keys [params] :as request}
        (if (and (= "/roll" (:command params))
                 (= roll-auth-token (:token params))
                 (check-dice (:text params)))
          (do
            (post-to-slack hook-url {:text (format "%s %s" (clojure.string/trim (:user_name params)) (slack-slash.diceroller/roll (clojure.string/trim (:text params))))})
            {:status 200
             :content-type "text/plain"})
          {:status 400
             :content-type "text/plain"
             :body "You need to provide <num>d<sides> "}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (run-jetty app {:port 3001}))
