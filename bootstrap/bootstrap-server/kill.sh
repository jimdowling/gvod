#!/bin/bash
set -e

host=`cut -d " " -f 1 machines`
user=`cut -d " " -f 2 machines`
ssh $user@${host} "cd boot ; ./kill.sh "

echo ""
echo "See log file: $user@$host ~/boot"
echo ""
