#!/bin/bash

id=`tail -1 leechers | cut -d " " -f 2`
parallel-ssh -h ./leechers -l $id "cd vod ; ./kill.sh ; ./leecher.sh"

host=`tail -1 leechers | cut -d " " -f 1`
echo ""
echo "See log file: $id@$host ~/vod"
echo ""
