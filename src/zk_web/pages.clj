(ns zk-web.pages
  (:require [zk-web.zk :as zk]
            [zk-web.conf :as conf]
            [noir.cookies :as cookies]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.request :as req]
            [clojure.string :as str])
  (:use [noir.core]
        [hiccup page form element core]))

;; util functions

(defn node-link
  "Return http link to node page"
  [node text]
  [:a {:href (str "/node?path=" node)} text])

(defn normalize-path
  "fix the path to normalized form"
  [path]
  (let [path (if (empty? path) "/" path)
        path (if (and (.endsWith path "/") (> (count path) 1))
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

(defn space
  "Retrn a number of html space"
  [n]
  (apply str (repeat n "&nbsp;")))

(defn referer
  "Get the referer from http header"
  []
  (let [header (:headers (req/ring-request) {"referer" "/"})
        referer (header "referer")]
    referer))

(def all-users (:users (conf/load-conf)))

;; layout

(defpartial header []
  [:div.span9.page-header
   [:div.row
    [:div.span6
     [:h1 (link-to "/" "ZK-Web")
      [:small (space 4) "Make zookeeper simpler."]]]
    [:div.span2
     (if-let [user (session/get :user)]
       [:div
        (link-to "/logout" [:span.badge.badge-error.pull-right "Logout"])
        [:span.badge.badge-info.pull-right user]]
       (link-to "/login" [:span.badge.badge-success.pull-right "Login"]))]]
   ])

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
      [:span.badge.pull-right (count children)]
      [:h3 "Children"]
      (if (empty? children)
        [:div.alert "No children"]
        (map (fn [s] [:li (node-link (str parent s) s)]) children))]]))

(defpartial node-data [data]
  [:div.span3
   [:span.badge.pull-right (count data) " byte(s)"]
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
     (map #(link-to (str "init?addr=" %) [:div.well.span6 [:h3 %]])
          (read-string cookie))
     [:form.well.span6 {:action "/init" :method "get"}
      [:input.span4. {:type "text" :name "addr" :placeholder "Connect String Here"}]
      [:button.btn.btn-primary {:type "submit"} "Go"]])))

(defpage [:get "/init"] {:keys [addr]}
  (let [addr (str/trim addr)
        cookie-str (cookies/get :history)
        cookie-str (if (nil? cookie-str) "[]" cookie-str)
        cookie (read-string cookie-str)
        _ (cookies/put! :history  (str (vec (take 3 (cons addr cookie)))))
        _ (session/put! :addr addr)
        _ (session/put! :cli (zk/mk-zk-cli addr))]
    (resp/redirect "/node")))


(defpage [:get "/login"] {:keys [msg target]}
  (layout
   [:div.span3.offset3
    [:div.row.form-actions
     (when-not (nil? msg) [:div.alert.alert-error [:h4 msg]])
     (form-to [:post "/login"]
              (label "user" "User Name")
              [:input.span3 {:type "text" :name "user"}]
              (label "pass" "Pass Word")
              [:input.span3 {:type "password" :name "pass"}]
              [:input.span3 {:type "hidden" :name "target" :value (if (nil? target) (referer) target)}]
              [:button.btn.btn-primary {:type "submit"} "Login"])]]))

(defpage [:post "/login"] {:keys [user pass target]}
  (cond
   (= (all-users user) pass) (do
                               (session/put! :user user)
                               (resp/redirect target))
   :else (render [:get "/login"]
                 {:msg "Incorrect password." :target target})))

(defpage "/logout" []
  (do
    (session/put! :user nil)
    (resp/redirect (referer))))

(defpage "/css" []
  (layout
   [:div.row
    [:div.span6 "level 222"]
    [:div.span6 "level 222"]]))
