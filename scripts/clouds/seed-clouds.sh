#!/bin/bash

for C in 1 2 3 4 5 6 7 
do
  echo "seeding cloud$C.sics.se"
  `scp activeStreams jdowling@cloud$C.sics.se:~/`
  `scp *.mp4 jdowling@cloud$C.sics.se:~/`
  `scp *.flv jdowling@cloud$C.sics.se:~/`
  `scp *.data.pieces jdowling@cloud$C.sics.se:~/`
  `scp *.data jdowling@cloud$C.sics.se:~/`
done
echo "Finished seeding. Exiting."
exit 0
