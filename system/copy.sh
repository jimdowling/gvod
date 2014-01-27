#!/bin/bash
set -e

if [ "$1" = "-help" ] ; then
 echo "usage: <prog> [-assemble] [-video]" 
 echo "      ./build.sh              # deploy the jar"
 echo "      ./build.sh -video      # deploy the jar and video "
 exit 0
fi

if [ "$1" = "-video" ] ; then
parallel-rsync -raz -h seeders ./src/test/resources/ ${HOME}/vod
fi

parallel-rsync -raz -h seeders ./deploy/ ${HOME}/vod
parallel-rsync -raz -h leechers ./deploy/ ${HOME}/vod
