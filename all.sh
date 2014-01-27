#!/bin/bash

echo "Killing stun servers, seeder..."
cd system
./kill-stunservers.sh
./kill-seeders.sh

cd ../bootstrap/bootstrap-server

./kill.sh

mysql -u kthfs -pkthfs -h cloud11.sics.se gvod -e "delete from nodes; delete from overlays;"

sleep 2
echo "restarting bootstrap server"
./start.sh
cd ../..

echo "restarting stun servers"
./system/start-stunservers.sh
sleep 10
echo "restarting seeders"
cd system
./start-seeders.sh

sleep 10

ssh jdowling@cloud6.sics.se "tail -200 vod/gvod.log"
exit 0
