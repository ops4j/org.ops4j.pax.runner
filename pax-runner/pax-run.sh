#!/bin/sh
#
# Script to run Pax Runner, which starts OSGi frameworks with applications.
#
#

SCRIPTS=`readlink $0`
if [ "${SCRIPTS}" != "" ]
then
  SCRIPTS=`dirname $SCRIPTS`
else
  SCRIPTS=`dirname $0`
fi

java $JAVA_OPTS -jar $SCRIPTS/target/pax-runner-0.7.1-SNAPSHOT.jar "$@"