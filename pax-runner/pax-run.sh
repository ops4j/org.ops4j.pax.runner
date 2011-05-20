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

java $JAVA_OPTS -cp .:$SCRIPTS:$SCRIPTS/target/pax-runner-1.7.3-SNAPSHOT.jar org.ops4j.pax.runner.Run "$@"