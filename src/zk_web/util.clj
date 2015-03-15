(ns zk-web.util
  (:require [noir.session :as session])
  (:import [java.nio.charset Charset]))

(defn bytes->str
  "Convert byte[] to String"
  [bytes]
  (if bytes
    (String. bytes (Charset/forName "UTF-8"))
    ""))

(defn normalize-path
  "fix the path to normalized form"
  [path]
  (let [path (if (empty? path) "/" path)
        path (if (and (.endsWith path "/") (> (count path) 1))
               (apply str (drop-last path))
               path)]
    path))

(defn child-path
  "get child path by parent and child name"
  [parent child]
  (str parent (when-not (.endsWith parent "/") "/") child))

(defn space
  "Retern a number of html space"
  [n]
  (apply str (repeat n "&nbsp;")))

(defmacro when-admin [ & exprs ]
  `(when (session/get :user)
     ~@exprs))

(defn drop-last-while
  "Drop from last while pred is true"
  [pred coll]
  (loop [c coll]
    (let [tail (last c)]
      (if (and (not= 0 (count c)) (pred tail))
       (recur (drop-last c))
       c))))

(defn parent
  "Get parent's path"
  [path]
  (cond
   (= path "/") "/"
   :default (let [path (if (.endsWith path "/") (drop-last path) path)]
              (apply str (drop-last-while #(not= % \/) path)))))

(defn str->int [string]
  (if string
    (Integer. (re-find  #"\d+" string ))))
