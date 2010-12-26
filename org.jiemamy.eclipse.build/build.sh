#! /bin/sh

CURRENT=`pwd`

BASEDIR=`dirname $0`
BASEDIR=$CURRENT/$BASEDIR
ECLIPSE_HEADLESS_BUILDER_HOME="$BASEDIR/../eclipse-headless-builder"

cd $BASEDIR

ant -DfromShell=true \
	-lib $ECLIPSE_HEADLESS_BUILDER_HOME/ant4eclipse/org.ant4eclipse_1.0.0.M4.jar \
	-lib $ECLIPSE_HEADLESS_BUILDER_HOME/ant4eclipse/libs/ \
	$*

cd $CURRENT
