Build Instructions
===

./build.sh
This will copy the contents of deploy to all the hosts in the leechers and seeders files.
To rebuild the jar, run:
./build.sh -assemble
To copy the video, run:
./build.sh -video

How to start some stun servers
===
./start-stunservers.sh

How to start a leecher
===
./start-leechers.sh

How to start a seeder
===
./start-seeders.sh <videoName>


How to start a client downloading the video
===
./client.sh
