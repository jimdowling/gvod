#!/bin/bash
export PATH=$PATH:/
dateFormat1=$(date +%Y%m%d)
dateFormat2=$(date +%Y.%m)
dateFormat3=$(date +%Y)
CPATH="../src/main/resources/data"
#The time stamp
echo $dateFormat1 > $CPATH/timeStamp.txt
# download :
echo "Asking routeviews.org for route views info..."
fileNameToDownload=$(echo -e "Anonymous\n\n cd bgpdata/$dateFormat2/RIBS/ \n ls\n" | ftp -n archive.routeviews.org)
fileNameToDownload=$(echo -e "user Anonymous PASS \n cd bgpdata/$dateFormat2/RIBS/ \n ls\n" | ftp -n archive.routeviews.org| grep $dateFormat1 | head -n 1 | ./getFileName.pl)
echo "downloading file from routeviews.org: $fileNameToDownload"
wget -v ftp://archive.routeviews.org/bgpdata/$dateFormat2/RIBS/$fileNameToDownload -P $CPATH
echo "unzipping  $CPATH/$fileNameToDownload"
bzip2 -dc $CPATH/$fileNameToDownload | time ./zebra-dump-parser.pl >$CPATH/DUMP_$fileNameToDownload 2>$CPATH/DUMPERR
sort < $CPATH/DUMP_$fileNameToDownload | uniq | time ./aggregate-by-asn.pl > $CPATH/routes.dat

# Prefix is now done.. we need now do the pairs
echo "downloading from routviews monthly dump"
wget -v http://archive.routeviews.org/oix-route-views/$dateFormat2/oix-full-snapshot-latest.dat.bz2 -P $CPATH
perl infer_main.pl -c -o $CPATH $CPATH/ASsummary.txt $CPATH/ASconnlist.txt $CPATH/ASconnpairs.txt $CPATH/ASrelation.txt $CPATH/oix-full-snapshot-latest.dat.bz2

# we now get the as-Names
wget -v -N http://www.potaroo.net/bgp/iana/asn-ctl.txt -P $CPATH

# clean up
rm $CPATH/*.bz2
rm $CPATH/oix*.*
