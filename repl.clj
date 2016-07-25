(require 'cljs.build.api)

(cljs.build.api/build "spy.cljs"
  {:output-to "main.js"
  :main 'o8v.spy
  :optimizations :none
  :source-map true})

(require 'cljs.repl)
(require 'cljs.repl.browser)

(cljs.repl/repl (cljs.repl.browser/repl-env))
