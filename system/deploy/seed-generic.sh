#!/bin/bash
# -XX:MaxDirectMemorySize - puts an upper limit on the size of direct memory, and triggers GC when you reach that limit
nohup java -Xms256m -Xmx1g -jar gvod.jar -torrent $1 -movie ./$1 -nogui  > gvod.log &
echo $! > ./gvod.pid
