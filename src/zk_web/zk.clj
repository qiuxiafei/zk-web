(ns zk-web.zk
  (:import [com.netflix.curator.retry RetryNTimes]
           [com.netflix.curator.framework CuratorFramework CuratorFrameworkFactory]
           [java.nio.charset Charset]))

(defn mk-zk-cli
  "Create a zk client using addr as connecting string"
  [ addr ]
  (let [cli (-> (CuratorFrameworkFactory/builder)
                (.connectString addr)
                (.retryPolicy (RetryNTimes. (int 3) (int 1000)))
                (.build))
        _ (.start cli)]
    cli))

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
  (-> cli (.checkExists) (.forPath path) bean (dissoc :class)))

(defn set
  "Set data to a node"
  [cli path data]
  (-> cli (.setData) (.forPath path data)))

(defn get
  "Get data from a node"
  [cli path]
  (-> cli (.getData) (.forPath path)))

(defn bytes->str
  "Convert byte[] to String"
  [bytes]
  (String. bytes (Charset/forName "UTF-8")))
