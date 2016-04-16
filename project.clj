(defproject slack-slash "0.1.0-SNAPSHOT"
  :description "Slack integration of / commands"
  :url "http://www.puffingtonpress.com:3001/cmd"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [clj-http "2.0.0"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :main  slack-slash.handler  ;; for standalone
  :ring {:handler slack-slash.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
