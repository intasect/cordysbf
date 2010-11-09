#!/bin/bash

# Test if we are running under Cygwin
if [ "`uname -o`" == "Cygwin" ]; then
	BF_IS_CYGWIN=true
	CYGPATH_CMD=`which cygpath`
else 
	BF_IS_CYGWIN=	
fi
export BF_IS_CYGWIN

# Check that the JAVA_HOME is set.
if [ -z "$JAVA_HOME" ]; then
	echo "JAVA_HOME environment variable is not set. Set it pointing to the Java installation root directory."
	exit 1
fi

# Set the JAVA_HOME to unix format if needed.
if [ "$JAVA_HOME" ]; then
	if [ "$BF_IS_CYGWIN" ]; then
		JAVA_HOME=`$CYGPATH_CMD -u "$JAVA_HOME"`
	fi	
fi

# Set the CORDYS_HOME to unix format if needed.
if [ "$CORDYS_HOME" ]; then
	if [ "$BF_IS_CYGWIN" ]; then
		CORDYS_HOME=`$CYGPATH_CMD -u "$CORDYS_HOME"`
	fi	
fi

# Set the java executable path
JAVA_CMD=$JAVA_HOME/bin/java

# Set the build.sh path
BUILD_CMD=./sdk/build/scripts/build.sh
if [ "$BF_IS_CYGWIN" ]; then
	# For Cygwin we need to pass the bash path as well (the build.sh script will be started from Java).
	BASH_CMD=`which bash`
	BASH_CMD=`$CYGPATH_CMD -aw $BASH_CMD`
	BUILD_CMD="-p2 \"$BASH_CMD\" \"$BUILD_CMD\""
fi	

# Ant scripts expect the USERNAME to be set.
if [ -z "$USERNAME" ]; then
	USERNAME=$USER
	export USERNAME
fi

		
	
	
