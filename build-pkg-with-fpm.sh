#!/bin/bash
# build package for zk-web

set -x -e

name=zk-web
version="0.1.0-SNAPSHOT"
packaging_version=""

maintainer="${USER}@localhost"
description="zk-web is a web ui for zookeeper"

url="https://github.com/qiuxiafei/zk-web"
arch="all"
section="mics"
prefix="/usr/lib"

origdir="$(pwd)"

# Cleanup old debian files
rm -rf ${name}*.deb
# If temp directory exists, remove it
if [ -d tmp ]; then
	rm -rf tmp
fi

# Make build directory, save location
mkdir -p tmp && pushd tmp
# Create build structure for package
mkdir -p zk-web
cd zk-web
mkdir -p build/usr/lib/zk-web
mkdir -p build/etc/systemd/system

# populate the directory
cp ${origdir}/debian/*.service build/etc/systemd/system

cp ${origdir}/target/zk-web-${version}-standalone.jar build/usr/lib/zk-web
ln -s zk-web-${version}-standalone.jar build/usr/lib/zk-web/zk-web-standalone.jar

cd build

fpm -t deb \
    -n ${name} \
    -v "${version}${packaging_version_suffix}" \
    --description "${description}" \
    --category "${section}" \
    --url="{$url}" \
    -a ${arch} \
    --vendor "" \
    --deb-user "root" \
    --deb-group "root" \
    -m ${maintainer} \
    --before-install ${origdir}/debian/zk-web.preinst \
    --after-install ${origdir}/debian/zk-web.postinst \
    --after-remove ${origdir}/debian/zk-web.postrm \
    --prefix=/ \
    -s dir \
    -d default-jre-headless \
    -- .

mv ${name}*.deb ${origdir}
popd

dpkg -c ${name}*.deb
