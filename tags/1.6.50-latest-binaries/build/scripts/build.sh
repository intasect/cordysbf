#!/bin/bash

# =====================================================================
# RTF Build script
# =====================================================================
# Launches ant with the proper build file and proper classpath
# Pass specific targets as arguments to this build file if needed
# =====================================================================

if [ -f "./set-environment-vars.sh" ]; then
	. ./set-environment-vars.sh
fi

if [ -z "$BF_SDK_HOME" ]; then
	BF_SDK_HOME=$PWD/sdk
fi

if [ -z "$BF_PLATFORM_HOME" ]; then
	BF_PLATFORM_HOME=$PWD/platform
fi

. "$BF_SDK_HOME/build/scripts/functions.sh"

if [ -z "$CORDYS_HOME" ]; then
	echo "CORDYS_HOME environment variable is not set. Set it pointing to the Cordys installation root directory."
	exit 1
fi

if [ ! -f "$JAVA_HOME/lib/tools.jar" ]; then
	echo "Warning: JAVA_HOME environment variable does not point to a JDK folder. You are not able to compile Java classes."
fi

if [ -z "$BF_SVN_VERSION" ]; then
	BF_SVN_LIB_DIR=svn
else
	BF_SVN_LIB_DIR=svn-$BF_SVN_VERSION
fi
BF_SVN_HOME=$BF_SDK_HOME/lib/$BF_SVN_LIB_DIR

# Set up the library path
if [ "$LD_LIBRARY_PATH" ]; then
	LD_LIBRARY_PATH=":$LD_LIBRARY_PATH"
fi
if [ -d "$BF_PLATFORM_HOME/bin" ]; then
    LD_LIBRARY_PATH=$BF_PLATFORM_HOME/bin$LD_LIBRARY_PATH
else
    LD_LIBRARY_PATH=$CORDYS_HOME/WCP/bin$LD_LIBRARY_PATH
fi

LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BF_SVN_HOME
export LD_LIBRARY_PATH

CLASSPATH=
export CLASSPATH

ANT_CP=$BF_SDK_HOME/lib/ant/ant-launcher.jar
ANT_CP=$ANT_CP:$JAVA_HOME/lib/tools.jar

if [ "$BF_IS_CYGWIN" ]; then
	# Make it a Windows path.
	ANT_CP=`$CYGPATH_CMD -pw "$ANT_CP"`
fi
export ANT_CP

LIBS=$BF_SDK_HOME/lib/ant
LIBS=$LIBS:$BF_SDK_HOME/lib
LIBS=$LIBS:$BF_SVN_HOME
LIBS=$LIBS:$BF_SDK_HOME/lib/libs-coboc2
LIBS=$LIBS:$BF_SDK_HOME/lib/cobertura
LIBS=$LIBS:$BF_SDK_HOME/lib/commons
LIBS=$LIBS:$BF_PLATFORM_HOME/int
LIBS=$LIBS:$BF_PLATFORM_HOME/ext
LIBS=$LIBS:$BF_PLATFORM_HOME/orc

if [ "$BF_IS_CYGWIN" ]; then
	# Make it a Windows path.
	LIBS=`$CYGPATH_CMD -pw "$LIBS"`
	PATH=`$CYGPATH_CMD -pw "$LD_LIBRARY_PATH"`
	BF_SDK_HOME=`$CYGPATH_CMD -w "$BF_SDK_HOME"`
	BF_PLATFORM_HOME=`$CYGPATH_CMD -w "$BF_PLATFORM_HOME"`
fi
export LIBS PATH BF_SDK_HOME BF_PLATFORM_HOME

"$JAVA_CMD" -Xmx512M "-Dsdk.real.dir=$BF_SDK_HOME" "-Dplatform.real.dir=$BF_PLATFORM_HOME" "-Dsvn.lib.home=$BF_SVN_LIB_DIR" -cp "$ANT_CP" org.apache.tools.ant.launch.Launcher -lib "$LIBS" "$@"
