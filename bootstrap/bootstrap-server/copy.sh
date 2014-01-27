#!/bin/bash
set -e
parallel-rsync -raz -h machines ./deploy/ ${HOME}/boot
exit $?
