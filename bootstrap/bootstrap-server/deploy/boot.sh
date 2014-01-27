#!/bin/bash
# -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n 
nohup java -Xms256m -Xmx2g -Xdebug -jar boot.jar -jdbcurl jdbc:mysql://cloud11.sics.se/gvod -user kthfs -pwd kthfs > boot.log &
echo $! > ./boot.pid
