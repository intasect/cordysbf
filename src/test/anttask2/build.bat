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
set PATH=%PATH%;%CD%\sdk\svn
set ROOT_DIR=%CD%\..\..\..
set CLASSPATH=

set ANT_CP=%ROOT_DIR%\sdk\ant.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\classes
set ANT_CP=%ANT_CP%;%ROOT_DIR%\sdk\optional.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\sdk\junit.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\sdk\tools.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\sdk\svn\svnant.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\sdk\svn\svnClientAdapter.jar
set ANT_CP=%ANT_CP%;%ROOT_DIRD%\sdk\svn\svnjavahl.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\lib\jldap.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\lib\wcp.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\lib\coelib.jar
set ANT_CP=%ANT_CP%;%ROOT_DIR%\lib\xerces.jar

%JAVA_HOME%\bin\java.exe -cp "%ANT_CP%" org.apache.tools.ant.Main %*
goto :end

:NoJavaFound
echo JAVA_HOME environment variable is not set. Set it pointing to the Java installation root directory.
goto :end

:NoCordysFound
echo CORDYS_HOME environment variable is not set. Set it pointing to the Cordys installation root directory.
goto :end

:end
endlocal