@echo off
cd /d %~dp0
ant -DfromShell=true -lib ..\eclipse-headless-builder\ant4eclipse\org.ant4eclipse_1.0.0.M4.jar -lib ..\eclipse-headless-builder\ant4eclipse\libs\ %*


