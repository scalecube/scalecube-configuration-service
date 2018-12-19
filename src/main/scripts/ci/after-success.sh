#!/bin/bash

DIRNAME=$(dirname $0)

echo       Running $0
echo *-*-*-*-*-*-*-*-*-*-*-*-*-*


if [ -z "$CODACY_PROJECT_TOKEN" ]; then
    echo [WARNING] Please go to https://app.codacy.com/app/$TRAVIS_REPO_SLUG/settings/coverage and add CODACY_PROJECT_TOKEN to travis settings
else 
    find -name jacoco.xml | xargs -i java -jar ~/codacy-coverage-reporter-assembly.jar report -l Java --partial -r {}
    java -jar ~/codacy-coverage-reporter-assembly.jar final
fi;

AFTER_SUCCESS_EXEC_FILES=$(find $DIRNAME -name 'after-success-*.sh')
for file in $AFTER_SUCCESS_EXEC_FILES; do
    source $file
done

