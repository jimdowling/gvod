#!/bin/bash
set -e

if [ $# -eq 0 ] ; then
cd system
mvn assembly:assembly -DskipTests 
mvn launch4j:launch4j -DskipTests
if [ $? -ne 0 ] ; then
  echo "Problem assembling maven jars. Exiting."
  exit 1
fi
cp target/system-1.0-SNAPSHOT-jar-with-dependencies.jar ../extension/daemon/
if [ $? -ne 0 ] ; then
 echo "Exiting.."
fi
cp target/system-1.0-SNAPSHOT.exe ../extension/daemon/gvodplayer.exe 
if [ $? -ne 0 ] ; then
 echo "Exiting.."
fi
cd ..
fi
echo "Building plugin..."

STAGING=/tmp/gvod
SCP_URL=glassfish@snurran.sics.se:/var/www/gvod/
PLUGIN=gvod.xpi

rm -rf $STAGING
cp -r extension $STAGING
cd $STAGING
zip -9 -r $PLUGIN *
echo ""
echo ""
echo "Copying to $STAGING"
echo "Copying $PLUGIN to $SCP_URL"
#scp $PLUGIN $SCP_URL
