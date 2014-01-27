#!/bin/bash

declare -a hosts=(4 5)

if [ ${#hosts[@]} -lt 2 ] ; then
 echo "You must define more than 1 host."
else 
 echo "${hosts[@]}"
fi

x=1
for i in ${hosts[@]}
do
 if [ $x -eq ${#hosts[@]} ] ; then
  y=0
 else
  y=$x
 fi
 ssh $USER@cloud$i.sics.se "cd vod ; ./kill.sh ; ./stunserver.sh $i ${hosts[$y]}@cloud${hosts[$y]}.sics.se"
 echo "ssh $USER@cloud$i.sics.se 'cd vod ; ./kill.sh ; ./stunserver.sh $i ${hosts[$y]}@cloud${hosts[$y]}.sics.se'"
 x=`expr $x + 1`
done

exit 0
