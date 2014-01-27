#!/bin/bash

for C in 1 2 3 4 5 6 7 
do
  echo "copying gvod.jar to cloud$C.sics.se"
  `scp gvod-system/target/gvod-system-1.0-SNAPSHOT-jar-with-dependencies.jar nwahlen@cloud$C.sics.se:~/gvod.jar`
done
