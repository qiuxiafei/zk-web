(defproject zk-web "0.2.0"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [noir "1.3.0-beta3"]
                           [com.netflix.curator/curator-framework "1.1.16"]
                           [com.netflix.curator/curator-test "1.1.16"]
                           [clj-time "0.14.4"]]
            :main zk-web.server)
