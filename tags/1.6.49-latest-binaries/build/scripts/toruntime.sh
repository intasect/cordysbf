#!/bin/bash

if [ -f ./set-environment-vars.sh ]; then
	. ./set-environment-vars.sh
fi

. ./sdk/build/scripts/functions.sh

echo "Batch-file publishing the content to the Cordys runtime environment."

function usage() {
	echo "Usage: fromcordys [contenttype] [-antoptions]"
	echo "Types of content: "
	echo "    all"
	echo "    flows"
	echo "    xforms"
}

if [ "$1" == "-?" ]; then
	usage
	exit 1
fi

# Set the command line defaults
BUILD_PREFIX=toruntime

# Empty the classpath
CLASSPATH=
export CLASSPATH

"$JAVA_CMD" -cp "./sdk/lib/buildtasks.jar" com.cordys.coe.ant.bf.AntLauncher $BUILD_CMD $BUILD_PREFIX "$@"

