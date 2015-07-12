(ns zk-web.conf
  (:require [clojure.java.io :as io]
            [zk-web.util :as u])
  (:import [java.io File PushbackReader]))

(defn- valid-conf-file?
  "Check if a file exists and is a normal file"
  [path]
  (let [file (File. path)]
    (and (.exists file)
         (.isFile file))))

(defn- load-conf-file [path]
  (when (valid-conf-file? path)
    (read-string (slurp path :encoding "utf-8"))))

(defn load-conf []
  "load the config from ~/.zk-web-conf.clj or conf/zk-web-conf.clj"
  (let [home-conf (str (System/getenv "HOME") File/separator ".zk-web-conf.clj")
        pwd-conf "conf/zk-web-conf.clj"
        env-port (u/str->int (System/getenv "PORT"))
        env-node (str (System/getenv "DEFAULT_NODE"))
        conf     (or (load-conf-file home-conf) (load-conf-file pwd-conf)
                  {
                   :server-port 8080
                   :users {"admin" "hello"}
                   :default-node ""
                  })]
        (if env-port
          (assoc conf :server-port env-port)
          conf)
        (if env-node
          (assoc conf :default-node env-node)
          conf)))

