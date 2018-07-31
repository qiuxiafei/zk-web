(ns zk-web.zk
  (:import [com.netflix.curator.retry RetryNTimes]
           [com.netflix.curator.framework CuratorFramework CuratorFrameworkFactory])
  (:require [clj-time.format :as time-format]
            [clj-time.coerce :as time-coerce])
  (:refer-clojure :exclude [set get])
  (:use zk-web.util))

(defn- mk-zk-cli-inner
  "Create a zk client using addr as connecting string"
  [ addr ]
  (let [cli (-> (CuratorFrameworkFactory/builder)
                (.connectString addr)
                (.retryPolicy (RetryNTimes. (int 3) (int 1000)))
                (.build))
        _ (.start cli)]
    cli))

;; memorize this function to save net connection
(def mk-zk-cli (memoize mk-zk-cli-inner))

(defn create
  "Create a node in zk with a client"
  ([cli path data]
     (-> cli
      (.create)
      (.creatingParentsIfNeeded)
      (.forPath path data)))
  ([cli path]
     (-> cli
         (.create)
         (.creatingParentsIfNeeded)
         (.forPath path))))

(defn rm
  "Delete a node in zk with a client"
  [cli path]
  (-> cli (.delete) (.forPath path)))

(defn ls
  "List children of a node"
  [cli path]
  (-> cli (.getChildren) (.forPath path)))

(defn stat
  "Get stat of a node, return nil if no such node"
  [cli path]
  (letfn [(pretty-time [t]
            (let [date-time (time-coerce/from-long t)]
              (time-format/unparse (time-format/formatters :date-hour-minute-second) date-time)))]
    (let [node-data (-> cli (.checkExists) (.forPath path) bean)
          ctime-pretty (pretty-time (clojure.core/get node-data :ctime 0))
          mtime-pretty (pretty-time (clojure.core/get node-data :mtime 0))
          modified-node-data (assoc
                               (dissoc node-data :class)
                               :ctime-pretty ctime-pretty
                               :mtime-pretty mtime-pretty)]
      (sort-by first (seq modified-node-data)))))

(defn set
  "Set data to a node"
  [cli path data]
  (-> cli (.setData) (.forPath path data)))

(defn get
  "Get data from a node"
  [cli path]
  (-> cli (.getData) (.forPath path)))

(defn rmr
  "Remove recursively"
  [cli path]
  (println "rmr " path)
  (doseq [child (ls cli path)]
    (rmr cli (child-path path child)))
  (rm cli path))
