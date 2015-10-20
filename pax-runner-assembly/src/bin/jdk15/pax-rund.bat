@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -cp ".;%_SCRIPTS_%;%_SCRIPTS_%\pax-runner-${project.version}.jar" org.ops4j.pax.runner.daemon.DaemonLauncher %*
