#!/bin/bash
set -e
./build.sh
./copy.sh
./start.sh $1
