@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java -cp ".;%_SCRIPTS_%;%_SCRIPTS_%\target\pax-runner-1.7.0-RC2-SNAPSHOT.jar" org.ops4j.pax.runner.daemon.DaemonLauncher %*
