@echo off
::
:: JMXEval execution script
::
:: Syntax: check_jmxeval.bat <config-xml-file> [<options>]
::
:: If JAVA_HOME environment variable is not set, uncomment the following
:: line and set the value as the path to root of the Java directory 
::
:: set JAVA_HOME="C:\path\to\jdk-1.7.0"
::
:: Execute JMXEval
::
set BASE_DIR=%~dp0
%JAVA_HOME%\bin\java -classpath %BASE_DIR%\jmxeval\lib\*.jar com.adahas.tools.jmxeval.App %*
