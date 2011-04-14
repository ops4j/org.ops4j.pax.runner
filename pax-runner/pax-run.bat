@echo off
SETLOCAL
set _SCRIPTS_=%~dp0

java %JAVA_OPTS% -cp ".;%_SCRIPTS_%;%_SCRIPTS_%\target\pax-runner-1.7.1.jar" org.ops4j.pax.runner.Run %*