(ns zk-web.pages
  (:require [zk-web.zk :as zk]
            [noir.cookies :as cookies]
            [noir.session :as session]
            [noir.response :as resp]
            [clojure.string :as str])
  (:use [noir.core]
        [hiccup.page]
        [hiccup element core]))

;; layout

(defpartial header []
  [:div.page-header.span9
   [:h1 (link-to "/" "ZK-Web")
    [:small "&nbsp;&nbsp;&nbsp;&nbsp;Make zookeeper simpler."]]])

(defpartial footer []
  [:div])

(defpartial layout [& content]
  (html5
   [:head
    [:title "zk-web"]
    (include-css "/css/bootstrap.css")
    (include-css "/css/bootstrap-responsive.css")
    (include-js "/js/bootstrap.js")]
   [:body
    [:div.container
     (header)
     content
     (footer)]]))

;; util functions

(defn node-link
  "Return http link to node page"
  [node text]
  [:a {:href (str "/node?path=" node)} text])

(defn normalize-path
  "fix the path to normalized form"
  [path]
  (let [path (if (empty? path) "/" path)
        path (if  (and (.endsWith path "/") (> (count path) 1))
               (apply str (drop-last path))
               path)]
    path))

(defn nodes-parents-and-link
  "Return name parents and there links"
  [path]
  (let [node-seq (rest (str/split path #"/"))
        link-seq (reduce #(conj %1 (str (last %1) %2 "/"))
                         ["/"] node-seq)
        node-seq (cons (session/get :addr) node-seq)]
    [node-seq link-seq]))

;; page elements

(defpartial node-stat [stat]
  [:div.span3
   [:table.table-striped.table-bordered.table
    [:tr [:h3 "Node Stat"]]
    (map (fn [kv]
           [:tr
            [:td (first kv)]
            [:td (last kv)]])
         stat)]])

(defpartial nav-bar [path]
  (let [[node-seq link-seq] (nodes-parents-and-link path)]
    [:ul.breadcrumb.span9
     (interleave (repeat [:i.icon-chevron-right])
                 (map (fn [l n] [:li (node-link l n)]) link-seq  node-seq))]))

(defpartial node-children [parent children]
  (let [parent (if (.endsWith parent "/")
                 parent
                 (str parent "/"))]
    [:div.span3
     [:ul.nav.nav-tabs.nav-stacked
      [:h3 "Children"]
      (if (empty? children)
        [:div.alert "No children"]
        (map (fn [s] [:li (node-link (str parent s) s)]) children))

      ]]))

(defpartial node-data [data]
  [:div.span3
   [:h3 "Node Data"]
   (if (nil? data)
     [:div.alert.alert-error "God, zookeeper returns NULL!"]
     [:div.well
      [:p {:style "word-break:break-all;"}
       (zk/bytes->str data)]]
     )])

;; pages

(defpage "/node" {:keys [path]}
  (let [path (normalize-path path)
        cli (session/get :cli)]
    (if (nil? cli)
      (resp/redirect "/")
      (layout
       (nav-bar path)
       (node-children path (zk/ls cli path))
       (node-stat (zk/stat cli path))
       (node-data (zk/get cli path))))))

(defpage "/" []
  (let [cookie (cookies/get :history)
        cookie (if (nil? cookie) "[]" cookie)]
    (layout
     (map
      #(link-to (str "/init?addr=" %) [:div.well.span6 [:h3 %]])
      (read-string cookie))
     [:form.well.span6 {:action "/init" :method "get"}
      [:input.span4. {:type "text" :name "addr" :placeholder "Connect String Here"}]
      [:button.btn.btn-primary {:type "submit"} "Go"]])))

(defpage [:get "/init"] {:keys [addr]}
  (let [cookie-str (cookies/get :history)
        cookie-str (if (nil? cookie-str) "[]" cookie-str)
        cookie (read-string cookie-str)
        _ (cookies/put! :history  (str (vec (take 3 (cons addr cookie)))))
        _ (session/put! :cli (zk/mk-zk-cli addr))
        _ (session/put! :addr addr)]
    (resp/redirect "/node")))
