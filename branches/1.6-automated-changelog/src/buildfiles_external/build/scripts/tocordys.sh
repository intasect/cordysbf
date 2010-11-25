#!/bin/bash

if [ -f ./set-environment-vars.sh ]; then
	. ./set-environment-vars.sh
fi

. ./sdk/build/scripts/functions.sh

echo "Batch-file for copying content to the Cordys Server."

function usage() {
	echo "Usage: tocordys [contenttype] [-antoptions]"
	echo "Types of content: "
	echo "    all"
	echo "    applicationconnectors"
	echo "    classes"
	echo "    coboc"
	echo "    flows"
	echo "    menus"
	echo "    methodsets"
	echo "    roles"
	echo "    soapnodes"
	echo "    styles"
	echo "    toolbars"
	echo "    xas"
	echo "    xforms"
	echo "    xmlstore"
	echo "    web"
}

if [ "$1" == "-?" ]; then
	usage
	exit 1
fi

# Set the command line defaults
BUILD_PREFIX=tocordys

# Empty the classpath
CLASSPATH=
export CLASSPATH

"$JAVA_CMD" -cp "./sdk/lib/buildtasks.jar" com.cordys.coe.ant.bf.AntLauncher $BUILD_CMD $BUILD_PREFIX "$@"

