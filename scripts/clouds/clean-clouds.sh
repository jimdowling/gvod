#!/bin/bash

for C in 1 2 3 4 5 6 7 
do
  echo "cleaning cloud$C.sics.se"
  `ssh jdowling@cloud$C.sics.se "rm activeStreams; rm *.data ; rm *.data.pieces; rm *.mp4 ; rm *.flv"`
done
