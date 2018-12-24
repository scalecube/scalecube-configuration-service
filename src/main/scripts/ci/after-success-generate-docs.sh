#!/usr/bin/env bash

SCALECUBE_CFG_SERVICE_DOCS='/tmp/scalecube-repo/configuration-service/'

git clone git@github.com:scalecube/scalecube.github.io.git /tmp/scalecube-repo
git clone https://github.com/apidoc/apidoc.git /tmp/apidoc-repo

docker build -t apidoc /tmp/apidoc-repo

mkdir -p /tmp/docs-generated $SCALECUBE_CFG_SERVICE_DOCS && rm -rf $SCALECUBE_CFG_SERVICE_DOCS*

docker run -u $(id -u) \
   -v $TRAVIS_BUILD_DIR/ApiDocs:/apidoc/docs \
   -v /tmp/docs-generated:/apidoc/docs-generated \
   -it apidoc -f ".*\\.apidoc$" -i "/apidoc/docs" -v -o "docs-generated"

cp -R /tmp/docs-generated/* $SCALECUBE_CFG_SERVICE_DOCS

cd $SCALECUBE_CFG_SERVICE_DOCS && \
    git add . && \
    git commit -m "Feature: updated configuration-service documentation" && \
    git push

