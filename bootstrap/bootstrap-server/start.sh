#!/bin/bash
set -e

if [ $# -lt 1 ] ; then
 echo "usage: $0 mysql_password [other params]"
 exit 1
fi

hostid=`tail -1 machines | cut -f 1 -d " "`
userid=`tail -1 machines | cut -f 2 -d " "`
ssh $userid@$hostid "cd boot ; ./kill.sh ; ./boot.sh $@"

if [ $? -ne 0 ] ; then
 echo "Error, exit code: $?"
 exit 1
fi
sleep 3

ssh $userid@$hostid "tail -200 ~/boot/boot.log"

echo ""
echo "See log file: "
echo "ssh $userid@$hostid \"tail -200f ~/boot/boot.log\""
echo ""
