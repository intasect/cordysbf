@echo off

REM =====================================================================
REM RTF Build script
REM =====================================================================
REM Launches ant with the proper build file and proper classpath
REM Pass specific targets as arguments to this build file if needed
REM =====================================================================

setlocal

if not defined JAVA_HOME goto :NoJavaFound
if not defined CORDYS_HOME goto :NoCordysFound

set PATH=%CORDYS_HOME%\WCP\bin
set CLASSPATH=

set ANT_CP=%CD%\..\..\sdk\ant.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\sdk\optional.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\sdk\junit.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\sdk\tools.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\sdk\rtftasks.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\sdk\optional.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\lib\jldap.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\lib\wcp.jar
set ANT_CP=%ANT_CP%;%CD%\..\..\lib\xerces.jar

%JAVA_HOME%\bin\java.exe -cp %ANT_CP% org.apache.tools.ant.Main %*
goto :end

:NoJavaFound
echo JAVA_HOME environment variable is not set. Set it pointing to the Java installation root directory.
goto :end

:NoCordysFound
echo CORDYS_HOME environment variable is not set. Set it pointing to the Cordys installation root directory.
goto :end

:end
endlocal