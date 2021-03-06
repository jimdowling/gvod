#!/bin/bash

if [ $# -lt 1 ] ; then
echo "usage: $0 videoFilename [other params]"
echo "./start-seeders.sh messi.mp4"
exit 1
fi

id=`tail -1 seeders | cut -d " " -f 2`
parallel-ssh -h ./seeders -l $id "cd vod ; ./kill.sh ; ./seeder.sh $@"

host=`cut -d " " -f 1 seeders`
echo ""
echo "See log file: ssh ${id}@${host} \"tail -200 ~/vod/gvod.log\""
echo ""

