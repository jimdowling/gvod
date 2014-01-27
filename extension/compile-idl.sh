#!/bin/sh -x
#
# export XUL_VERSION=1.9.2.17
# xul_dir_version="/usr/lib/xulrunner-$XUL_VERSION"
# if [ -f $xul_dir_version/xpidl ]; then
#     echo "Found XULRUNNER directory $xul_dir_version"
#     export XULRUNNER_IDL=/usr/share/idl/xulrunner-$XUL_VERSION
#     export XULRUNNER_XPIDL=/usr/lib/xulrunner-$XUL_VERSION/xpidl
# else
# 	echo "|==============================================================================|"
# 	echo "| Failed to locate XULRUNNER directory, please modify the XUL_VERSION variable |"
# 	echo "|==============================================================================|"
# 	exit
# fi 
# # ----- Turn .idl into .xpt
# $XULRUNNER_XPIDL -m typelib -w -v -I $XULRUNNER_IDL -e components/gvodIChannel.xpt idl/gvodIChannel.idl
# $XULRUNNER_XPIDL -m typelib -w -v -I $XULRUNNER_IDL -e components/gvodITransport.xpt idl/gvodITransport.idl

# Requirements:
# sudo aptitude install libidl-dev:i386 -y

export XULRUNNER_IDL=/opt/xulrunner-sdk/idl
export XULRUNNER_XPIDL=/opt/xulrunner/xpidl

$XULRUNNER_XPIDL -m typelib -w -v -I $XULRUNNER_IDL -e components/gvodIChannel.xpt idl/gvodIChannel.idl
$XULRUNNER_XPIDL -m typelib -w -v -I $XULRUNNER_IDL -e components/gvodITransport.xpt idl/gvodITransport.idl
