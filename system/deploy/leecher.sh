#!/bin/bash
rm ~/.kompics/topgear*
rm ~/.kompics/activeStreams
nohup java -jar ./gvod.jar -torrent http://cloud7.sics.se/gvod/topgear.mp4.data -nogui &
echo $! > ./gvod.pid

