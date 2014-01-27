#!/bin/bash

for C in 1 2 3 4 5 6 7 
do
  echo "Starting gvod.jar on cloud$C.sics.se"
  ssh jdowling@cloud$C.sics.se "nohup java -jar gvod.jar &"
done
