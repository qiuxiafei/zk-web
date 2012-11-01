(ns zk-web.conf
  (:require [clojure.java.io :as io])
  (:import [java.io File PushbackReader]))

(defn valid-conf-file?
  "Check if a file exists and is a normal file"
  [path]
  (let [file (File. path)]
    (and (.exists file)
         (.isFile file))))

(defn load-conf []
  "load the config from ~/.zk-web-conf.clj"
  (let [file (str (get (System/getenv) "HOME") File/separator ".zk-web-conf.clj")]
    (if (valid-conf-file? file)
      (with-open [r (io/reader file)]
        (read (PushbackReader. r)))
      {
       :server-port 8080
       :users {"admin" "hello"}
       })))
