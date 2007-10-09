#!/bin/sh
#
# Script to run Pax Runner, which starts OSGi frameworks with applications.
#
#

java $JAVA_OPTS -jar ~/.m2/repository/org/ops4j/pax/runner/pax-runner/0.5.1-SNAPSHOT/pax-runner-0.5.1-SNAPSHOT-jdk14.jar "$@"