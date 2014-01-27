#!/bin/bash

bootHost=cloud4.sics.se
user=jdowling

# exit on error
set -e

parallel-ssh --help >> /dev/null 
if [ $? -ne 0 ] ; then
 echo "To run, install: " 
 echo "sudo apt-get install pssh -y"
fi

echo "Now launching.."
verbose=""

if [ $0 = "-h" ] ; then 
 echo "prog: [user] [-v]"
 exit 0 
fi

if [ $# -gt 0 ] ; then
   user=$1    
   verbose="-v"
fi

hosts="clouds"
src="target"
dest="/home/$user/vod"
videodest="$dest/video"
video="topgear.mp4"
torrent="http://${bootHost}/gvod/$video.data"
gvod="gvod-system-1.0-SNAPSHOT-jar-with-dependencies.jar"
prog="java -jar $dest/$gvod -torrent $torrent -nogui -videoDir $videodest -dir $dest > $dest/gvod.log &  ; echo $! > $dest/gvod.pid" 

echo ""
echo "java -jar $dest/$gvod -torrent $torrent -nogui -videoDir $videodest -dir $dest" 

# build the gvod dirs
parallel-ssh -h $hosts -l $user $verbose mkdir -p $dest
parallel-ssh -h $hosts -l $user $verbose mkdir -p $videodest

if [ ! -f  $src/$gvod ] ; then
echo "Jar file not found. Run ./build.sh first!"
exit 1
fi

# copy the jar file
parallel-rsync -az -h $hosts -l $user $verbose $src/$gvod $dest

# exec the gvod jar file
nohup parallel-ssh -h $hosts -l $user $verbose $prog &
