#!/bin/bash
mvn assembly:single -DskipTests
cp target/system-1.0-SNAPSHOT-jar-with-dependencies.jar deploy/gvod.jar
