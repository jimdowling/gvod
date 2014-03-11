#!/bin/bash
# -XX:MaxDirectMemorySize - puts an upper limit on the size of direct memory, and triggers GC when you reach that limit
VIDEO=$1
shift
nohup java -Xms256m -Xmx500m -jar gvod.jar -torrent $VIDEO -movie ./$VIDEO -nogui $@  > gvod.log &
echo $! > ./gvod.pid
