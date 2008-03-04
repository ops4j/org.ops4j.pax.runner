@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java %JAVA_OPTS% -jar "%_SCRIPTS_%\target\pax-runner-0.7.3-SNAPSHOT-jdk14.jar" %1 %2 %3 %4 %5 %6 %7 %8 %9