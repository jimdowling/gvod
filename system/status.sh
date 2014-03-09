#!/bin/bash
set -e

function status() 
{
echo "Checking $1"
hosts=`cut -d " " -f 1 $1`
id=`tail -1 $1 | cut -d " " -f 2`
for host in ${hosts[@]} ; do
  echo "$host"
  ssh ${id}@${host} "ps -ef | grep -v 'bash' | grep -i gvod "
done
}


status "leechers"
status "seeders"
