#!/bin/sh
#
# Script to run Pax Runner, which starts OSGi frameworks with applications.
#
#

if [ -z "$PAX_HOME" ] ; then
  PAX_HOME=$HOME/.pax
fi

java -jar $PAX_HOME/runner/bin/runner-${project.version}.jar "$@"