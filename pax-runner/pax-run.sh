#!/bin/sh
#
# Script to run Pax Runner, which starts OSGi frameworks with applications.
#
#

SCRIPTS=`readlink $0`
SCRIPTS=`dirname $SCRIPTS`

java $JAVA_OPTS -jar $SCRIPTS/target/pax-runner-0.6.0-SNAPSHOT.jar "$@"