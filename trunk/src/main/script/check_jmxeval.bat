@echo off
::
:: JMXEval execution script
::
:: Syntax: check_jmxeval.bat <config-xml-file>
::
:: If JAVA_HOME environment variable is not set, uncomment the following
:: line and set the value as the path to root of the Java directory 
::
:: JAVA_HOME="C:\path\to\jdk-1.5.0"
::
:: Executes JMXEval
::
set LIB_DIR=%~dp0
%JAVA_HOME%\bin\java -jar %LIB_DIR%\jmxeval-1.2.2.jar %*