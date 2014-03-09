#!/bin/bash
set -e

hostid=`tail -1 machines | cut -f 1 -d " "`
userid=`tail -1 machines | cut -f 2 -d " "`
ssh $userid@$hostid "cd boot ; ./kill.sh ; ./boot.sh"

echo ""
echo "See log file: "
echo "ssh $userid@$hostid \"tail -200f ~/boot/boot.log\""
echo ""
