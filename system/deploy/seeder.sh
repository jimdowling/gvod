#!/bin/bash
# -XX:MaxDirectMemorySize - puts an upper limit on the size of direct memory, and triggers GC when you reach that limit
nohup java -Xms256m -Xmx1g -jar gvod.jar -torrent topgear.mp4 -movie ./topgear.mp4 -nogui  > gvod.log &
echo $! > ./gvod.pid
