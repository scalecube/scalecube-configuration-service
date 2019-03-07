#!/usr/bin/env bash

SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPTS_DIR%/*}"
TARGET_DIR=${PROJECT_DIR}/scalecube-configuration-benchmarks/target
JAR_FILE=$(ls ${TARGET_DIR} | grep -m 1 SNAPSHOT.jar)

if [[ -z "$JAR_FILE" ]]; then
  mvn -f ${PROJECT_DIR} clean install -DskipTests -P RunDockerGoals
  JAR_FILE=$(ls ${TARGET_DIR} | grep -m 1 SNAPSHOT.jar)
fi

java -cp ${TARGET_DIR}/${JAR_FILE} io.scalecube.configuration.benchmarks.Environment
