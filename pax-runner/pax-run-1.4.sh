#!/bin/sh
#
# Script to run Pax Runner, which starts OSGi frameworks with applications.
#
#

SCRIPTS=`readlink $0`
SCRIPTS=`dirname $SCRIPTS`

java $JAVA_OPTS -jar $SCRIPTS/target/pax-runner-0.5.5-SNAPSHOT-jdk14.jar "$@"