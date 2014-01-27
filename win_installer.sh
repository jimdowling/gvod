#!/bin/bash
set -e

cd windows-installer

# remove the previous built folders
rm -rf target*

# copy the gvod executable file to the installer directory
if [ -e ../extension/daemon/gvodplayer.exe ]
then
    cp ../extension/daemon/gvodplayer.exe ./src/main/resources/gvod.exe
else
    echo
    echo "===> first build the gvodplayer.exe by running the plugin.sh!"
    echo
    exit
fi

# build the exe file
mvn install
cp target/gvod-1.exe ./gvod-installer.exe
echo
echo "===> gvod-installer.exe is ready in the windows-installer folder :)"
echo

# cleanup
rm -rf target*

