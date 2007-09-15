@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -jar "%_SCRIPTS_%\pax-runner-${project.version}.jar %1 %2 %3 %4 %5 %6 %7 %8 %9 