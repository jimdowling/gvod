#!/bin/bash
user="amir"

if [ $0 = "-h" ] ; then 
 echo "prog: [user]"
 exit 0 
fi

if [ $# -gt 0 ] ; then
   user=$1    
fi
dest=/home/$user/gvod

parallel-ssh -h clouds -l $user "kill `cat ${dest}gvod.pid`"


echo "Java progs running under your user are now: "
echo ""
parallel-ssh -h clouds -l $user "jps"
echo ""
