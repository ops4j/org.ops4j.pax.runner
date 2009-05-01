@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -cp "target\pax-runner-0.19.1-SNAPSHOT-jdk14.jar" org.ops4j.pax.runner.daemon.DaemonLauncher %1 %2 %3 %4 %5 %6 %7 %8 %9