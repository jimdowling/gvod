#!/bin/bash
set -e

if [ $# -ne 1 ] ; then
 echo "usage: $0 mysql_password"
 exit 1
fi

hostid=`tail -1 machines | cut -f 1 -d " "`
userid=`tail -1 machines | cut -f 2 -d " "`
ssh $userid@$hostid "cd boot ; ./kill.sh ; ./boot.sh $1"

if [ $? -ne 0 ] ; then
 echo "Error, exit code: $?"
 exit 1
fi
sleep 4 

ssh $userid@$hostid "tail -200 ~/boot/boot.log"

echo ""
echo "See log file: "
echo "ssh $userid@$hostid \"tail -200f ~/boot/boot.log\""
echo ""
