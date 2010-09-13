#!/bin/sh
#
# Script to run Pax Runner as a daemon, which starts OSGi frameworks with applications.
#
#

SCRIPTS=`readlink $0`
if [ "${SCRIPTS}" != "" ]
then
  SCRIPTS=`dirname $SCRIPTS`
else
  SCRIPTS=`dirname $0`
fi

exec java $JAVA_OPTS -cp .:$SCRIPTS:$SCRIPTS/target/pax-runner-1.6.0-SNAPSHOT-jdk14.jar org.ops4j.pax.runner.daemon.DaemonLauncher "$@"
