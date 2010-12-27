@echo off
set SCRIPT_CURRENT_DIR=%%~dpz
ant -DfromShell=true -lib %SCRIPT_CURRENT_DIR%\..\eclipse-headless-builder\ant4eclipse\org.ant4eclipse_1.0.0.M4.jar -lib %SCRIPT_CURRENT_DIR%\..\eclipse-headless-builder\ant4eclipse\libs\ %*

