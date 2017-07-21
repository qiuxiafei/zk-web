# zk-web

zk-web is a Web UI of [Zookeeper](http://zookeeper.apache.org), just making it easier to use. Sometimes I really get tired of the command line.
zk-web is written in [clojure](http://clojure.org) with [noir](http://webnoir.org) and [boostrap](http://twitter.github.com/bootstrap/). Currently there're just less than 450 lines clojure code at all. Clojure is really so simple and so elegent!

## Usage

To use zk-web, you need [leiningen](https://github.com/technomancy/leiningen) and git currentlly. (And I'll make a stand-alone package later).
Run the following command:

```bash
git clone git://github.com/qiuxiafei/zk-web.git
cd zk-web
lein deps # run this if you're using lein 1.x
lein run
```
Meet with zk-web at [http://localhost:8080](http://localhost:8080)! I'am sure it's super easy!

## Package UberJar

```bash
cd zk-web
lein uberjar
```
## Create debian package

```bash
docker build -t zk-web .
docker run -it --rm -v $(pwd):/usr/src/app zk-web /bin/bash /usr/src/app/build-pkg-with-fpm.sh
```
Install and run it with following commands:

```bash
sudo dpkg -i zk-web_0.1.0-SNAPSHOT_all.deb
sudo systemctl start zk-web
```

Enable the zk-web service in order for it to be autorestart on host reboot:

```bash
sudo systemctl enable zk-web
```

## Configuration

zk-web is also easy to configurate. It reads `$HOME/.zk-web-conf.clj` or `conf/zk-web-conf.clj` when it starts up. As you‘ve already seen, the configuration file is also clojure code. Let's see an example:

```clojure
{
 :server-port 8989  ;; optional, 8080 by default
 :users {
         "admin" "hello"
         ;; map of user -> password
         ;; you can add more
         }
 :default-node "localhost:2181/my-start-node" ;; optional
 }
```

## Features
* Jump to ancesters of a node in navigation bar.
* List children of a node with link to them.
* Show stat and data of a node.
* Remember last 3 zookeepers you visit in cookie.
* Create/edit/delete/rmr a node.
* Simple authority management.
* Default node for first-arrival guest.

## TODO
* Data Format - Format json, xml and so on.

## Contributers
* @lra 
* @lispmind 
* @killme2008 
* @pershyn

## License

Copyright (C) 2012

Distributed under the Eclipse Public License, the same as Clojure.
