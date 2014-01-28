#!/bin/bash

if [ $# -ne 1 ] ; then
echo "usage: $0 videoFilename"
exit 1
fi

id=`tail -1 seeders | cut -d " " -f 2`
parallel-ssh -h ./seeders -l $id "cd vod ; ./kill.sh ; ./seeder.sh $1"

host=`cut -d " " -f 1 seeders`
echo ""
echo "See log file: ${id}@${host} ~/vod"
echo ""
