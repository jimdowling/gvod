#!/bin/bash
set -e
mvn assembly:assembly -DskipTests
cp target/bootstrap-server-1.0-SNAPSHOT-jar-with-dependencies.jar deploy/boot.jar

