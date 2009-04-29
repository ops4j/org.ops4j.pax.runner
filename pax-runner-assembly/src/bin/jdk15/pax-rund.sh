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

exec java $JAVA_OPTS -cp $SCRIPTS/pax-runner-${project.version}.jar:. org.ops4j.pax.runner.daemon.DaemonLauncher "$@"
