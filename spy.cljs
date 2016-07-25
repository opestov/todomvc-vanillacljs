(ns o8v.spy
  [:require clojure.browser.repl])

(defonce conn
  (clojure.browser.repl/connect "http://localhost:9000/repl"))
