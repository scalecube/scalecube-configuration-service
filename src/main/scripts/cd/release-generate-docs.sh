#!/usr/bin/env bash

# This script generates Release api documentation and saves it to develop branch

SCALECUBE_CFG_SERVICE_DOCS='/tmp/scalecube-repo/configuration-service/'
RELEASE=$(cd $TRAVIS_BUILD_DIR && \
          mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)
RELEASE_TEMPLATE='0.0.0-CURRENT'

# clone needed repositories
git clone git@github.com:scalecube/scalecube.github.io.git /tmp/scalecube-repo
git clone git@github.com:scalecube/scalecube-configuration-service.git /tmp/scalecube-cfg-service
git clone https://github.com/apidoc/apidoc.git /tmp/apidoc-repo

# build apidoc docker image
docker build -t apidoc /tmp/apidoc-repo

mkdir -p /tmp/docs-generated $SCALECUBE_CFG_SERVICE_DOCS && rm -rf $SCALECUBE_CFG_SERVICE_DOCS*

# set release version to documentation
for apidocument in $(find $TRAVIS_BUILD_DIR/ApiDocs -regex '.*\.\(apidoc\|json\)$'); do
    sed -i "s/$RELEASE_TEMPLATE/$RELEASE/g" $apidocument
    cat $apidocument >> /tmp/scalecube-cfg-service/ApiDocs/_apidoc.js
done

docker run -u $(id -u) \
   -v $TRAVIS_BUILD_DIR/ApiDocs:/apidoc/docs \
   -v /tmp/docs-generated:/apidoc/docs-generated \
   -it apidoc -f ".*\\.apidoc$" -i "/apidoc/docs" -v -o "docs-generated"

cp -R /tmp/docs-generated/* $SCALECUBE_CFG_SERVICE_DOCS

cd $SCALECUBE_CFG_SERVICE_DOCS && \
    git add . && \
    git commit -m "Feature: updated configuration-service documentation" && \
    git push

cd $SCALECUBE_CFG_SERVICE_DOCS && \
    git add . && \
    git commit -m "Feature: updated configuration-service documentation" && \
    git push


