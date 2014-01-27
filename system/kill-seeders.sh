#!/bin/bash
set -e

fname=seeders

hosts=`cut -d " " -f 1 ${fname}`
id=`tail -1 ${fname}| cut -d " " -f 2`

for host in ${hosts[@]} ; do
  ssh ${id}@${host} "cd vod ; ./kill.sh "
  echo ""
  echo "See log file: $id@${host}:~/vod"
  echo ""
done
