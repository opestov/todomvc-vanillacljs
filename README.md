# Todo list

Version of the [todo application](https://todomvc.com/) that is
written in Clojurescipt.

There was no goal to use the MVC pattern. I wanted to create something that
works and if possible does not use external libraries.

The live result is available [here](https://opestov.github.io/sketches/1/).

## Build

Run 

```
java -cp [path-to-cljs.jar] clojure.main release.clj
```

to create a Javascript file main.js.

## Development

I used REPL during development. The following command
starts REPL that allows to evaluate expressions in the browser.

```
java -cp [path-to-cljs.jar] clojure.main repl.clj
```
