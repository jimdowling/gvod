#!/bin/bash
# -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n 
PWD=$1
shift

nohup java -Xms256m -Xmx1g -Xdebug -jar boot.jar -jdbcurl jdbc:mysql://cloud11.sics.se/gvod -user kthfs -pwd $PWD $@ > boot.log &
echo $! > ./boot.pid
