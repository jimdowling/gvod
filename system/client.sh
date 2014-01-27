#!/bin/bash
java -jar deploy/gvod.jar -port 3331 -mPort 4444 -cPort 8833 -torrent http://cloud7.sics.se/gvod/topgear.mp4.data -videoDir /tmp -dir /tmp -nogui
