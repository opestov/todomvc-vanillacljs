(ns o8v.core
  (:require cljs.reader clojure.string))

(defn element-by-class [class]
  (.item (js/document.getElementsByClassName class) 0))

(defn remove-children [node]
  (while (.hasChildNodes node)
    (.removeChild node (.-lastChild node))))

(defn dom
  "Returns a collection of HTML elements."
  [x]
  (cond
    (string? x)
      (vector (js/document.createTextNode x))
    (keyword? (first x))
      (let [element (js/document.createElement (name (first x)))
            [attrs children]
              (if (map? (fnext x)) [(fnext x) (nnext x)] [nil (next x)])]
        (doseq [[k v] attrs] (aset element k v))
        (doseq [i children j (dom i)] (.appendChild element j))
        (vector element))
    :else
      (mapcat dom x)))

(defn toggle-item [state id]
  (swap! state assoc :todos
    (mapv (fn [x] (if (= id (:id x)) (update x :completed not) x))
      (:todos @state))))

(defn toggle-items [state coll]
  (let [f (constantly (not-every? :completed coll))
        ids (set (map :id coll))]
    (swap! state assoc :todos
      (mapv (fn [x] (if (contains? ids (:id x)) (update x :completed f) x))
        (:todos @state)))))

(defn add-item [state text]
  (swap! state update-in [:todos]
    conj {:id (rand-int 1000000) :text (clojure.string/trim text)}))

(defn update-item [state id text]
  (swap! state assoc :todos
    (mapv (fn [x] (if (= id (:id x)) (assoc x :text text) x)) (:todos @state))))

(defn delete-item [state id]
  (swap! state assoc :todos
    (filterv (fn [x] (not= id (:id x))) (:todos @state))))

(defn clear-completed [state]
  (swap! state assoc :todos
    (filterv (comp not :completed) (:todos @state))))

(defn header [state]
  [:header {"className" "header"}
    [:h1 "todos"]
    [:input
      {"className" "new-todo"
      "placeholder" "What needs to be done?"
      "onchange" (fn [e] (add-item state (.-value (.-target e))))}]])

(defn todo1 [state {:keys [id text completed]}]
  [:li
    (if (= (:editing @state) id)
      {"className" "editing"}
      (if completed {"className" "completed"} {}))
    [:div {"className" "view"}
      [:input
        (let [f (fn [e] (toggle-item state id))
              x {"className" "toggle" "type" "checkbox" "onclick" f}]
          (if completed (assoc x "checked" "true") x))]
      [:label {"ondblclick" (fn [_] (swap! state assoc :editing id))} text]
      [:button
        {"className" "destroy" "onclick" (partial delete-item state id)}]]
    (if (= (:editing @state) id)
      [:input
        {"className" "edit""value" text
        "onchange" (fn [e]
          (update-item state id (.-value (.-target e)))
          (swap! state dissoc :editing))
        "onblur" (fn [_] (swap! state dissoc :editing))}]
      [])])

(defn main-section [state]
  (let [f (case (:node @state)
            "all" (constantly true)
            "active" (complement :completed)
            "completed" :completed)
        items (filter f (:todos @state))]
    [:section {"className" "main"}
      [:input
        (let [f (fn [_] (toggle-items state items))
              x {"className" "toggle-all" "type" "checkbox" "onclick" f}]
          (if (and (seq items) (every? :completed items))
            (assoc x "checked" "true") x))]
      [:label {"htmlFor" "toggle-all"} "Mark all as complete"]
      [:ul {"className" "todo-list"}
        (map (partial todo1 state) items)]]))

(defn footer [state]
  (let [f (fn [x] (merge {"href" "#"
                          "onclick" (fn [_] (swap! state assoc :node x))}
                    (if (= x (:node @state)) {"className" "selected"} {})))
        active (count (remove :completed (:todos @state)))]
    [:footer {"className" "footer"}
      [:span {"className" "todo-count"}
        [:strong (str active)]
        (str " item" (if (= 1 active) "" "s") " left")]
      [:ul {"className" "filters"}
        [:li [:a (f "all") "All"]]
        [:li [:a (f "active") "Active"]]
        [:li [:a (f "completed") "Completed"]]]
      (if (= 0 (count (filter :completed (:todos @state))))
        []
        [:button {"className" "clear-completed"
                  "onclick" (partial clear-completed state)}
          "Clear completed"])]))

(defn render [app-class state]
  (let [app-element (element-by-class app-class)]
    (remove-children app-element)
    (doseq [x (dom [(header state) (main-section state) (footer state)])]
      (.appendChild app-element x))
    (if (:editing @state)
      (.focus (element-by-class "edit"))
      (.focus (element-by-class "new-todo")))))

(defonce storage js/localStorage)

(defn save-clj [key data]
  (.setItem storage key (str data)))

(defn load-clj [key]
  (when-let [s (.getItem storage key)]
    (cljs.reader/read-string s)))

(defonce app-state
  (atom
    (if-let [data (load-clj "todoapp")]
      data
      {:node "all"
      :todos []})))

(add-watch app-state :on-change
  (fn [_ _ _ _]
    (render "todoapp" app-state)
    (save-clj "todoapp" @app-state)))
(render "todoapp" app-state)
