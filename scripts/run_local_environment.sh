#!/usr/bin/env bash

SCRIPTS_DIR=$(dirname $0)
TARGET_DIR="${SCRIPTS_DIR%/*}"/scalecube-configuration-benchmarks/target
JAR_FILE=$(ls ${TARGET_DIR} | grep -m 1 .jar)

java -cp ${TARGET_DIR}/${JAR_FILE} io.scalecube.configuration.benchmarks.Environment
