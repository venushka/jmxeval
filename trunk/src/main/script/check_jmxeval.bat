@echo off
::
:: JMXEval execution script
::
:: Syntax: check_jmxeval.bat <config-xml-file> [<options>]
::
:: If JAVA_HOME environment variable is not set, uncomment the following
:: line and set the value as the path to root of the Java directory 
::
:: JAVA_HOME="C:\path\to\jdk-1.5.0"
::
:: Executes JMXEval
::
set BASE_DIR=%~dp0
set LIB_DIR=%BASE_DIR%/lib
%JAVA_HOME%\bin\java -classpath %BASE_DIR%\jmxeval-1.2.5.jar;%LIB_DIR%/args4j-2.0.16.jar com.adahas.tools.jmxeval.App %*
