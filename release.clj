(require 'cljs.build.api)

(defn delete-recursively [node]
  (when (.exists node)
    (when (.isDirectory node)
      (doseq [f (.listFiles node)] (delete-recursively f)))
    (clojure.java.io/delete-file node)))

(delete-recursively (clojure.java.io/file "out"))
(delete-recursively (clojure.java.io/file "main.js"))
(cljs.build.api/build "main.cljs"
  {:output-to "main.js"
  :optimizations :advanced})
(delete-recursively (clojure.java.io/file "out"))

