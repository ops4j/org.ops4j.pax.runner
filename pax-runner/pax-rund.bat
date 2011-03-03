@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -cp ".;%_SCRIPTS_%;%_SCRIPTS_%\target\pax-runner-1.6.1.jar" org.ops4j.pax.runner.daemon.DaemonLauncher %*
