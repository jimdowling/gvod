#!/bin/bash
set -e

for host in `echo "cloud4.sics.se cloud5.sics.se"` ; do
 ssh $USER@${host} "cd vod ; ./kill.sh "
 echo ""
 echo "See log file: $USER@${host}:~/vod"
 echo ""
done
