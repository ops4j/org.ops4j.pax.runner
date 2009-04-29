@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -cp "pax-runner-${project.version}.jar" org.ops4j.pax.runner.daemon.DaemonLauncher %1 %2 %3 %4 %5 %6 %7 %8 %9 