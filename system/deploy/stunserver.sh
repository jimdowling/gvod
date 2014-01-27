#!/bin/bash
nohup java -jar gvod.jar true $1 $2 -nogui  > gvod.log &
echo $! > ./gvod.pid

