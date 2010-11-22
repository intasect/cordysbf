@echo off

REM =====================================================================
REM RTF Build script
REM =====================================================================
REM Launches ant with the proper build file and proper classpath
REM Pass specific targets as arguments to this build file if needed
REM =====================================================================

setlocal

if exist "%CD%\set-environment-vars.cmd" (
	call "%CD%\set-environment-vars.cmd"
)

if not defined JAVA_HOME goto :NoJavaFound
if not exist %JAVA_HOME%\lib\tools.jar (
	echo Warning: JAVA_HOME environment variable does not point to a JDK folder. You are not able to compile Java classes.
)
 
if exist %CD%\platform\bin (
    set PATH=%CD%\platform\bin
) else (
    set PATH=%CORDYS_HOME%\WCP\bin
)

if defined BF_SVN_VERSION (
	set BF_SVN_LIB_DIR=svn-%BF_SVN_VERSION%
) else (
	set BF_SVN_LIB_DIR=svn
)
set BF_SVN_HOME=%BF_SDK_HOME%\lib\%BF_SVN_LIB_DIR%

set CLASSPATH=
set BUILD_HOME=%CD%

set ANT_CP=%CD%\sdk\ant\ant-launcher.jar
set ANT_CP=%ANT_CP%;%JAVA_HOME%\lib\tools.jar
set ANT_CP=%ANT_CP%;%BF_SVN_HOME%\svnkit.jar

set LIBS=%CD%\sdk\ant
set LIBS=%LIBS%;%CD%\sdk
set LIBS=%LIBS%;%CD%\sdk\commons
set LIBS=%LIBS%;%CD%\platform\int
set LIBS=%LIBS%;%CD%\platform\ext
set LIBS=%LIBS%;%CD%\platform\orc

if defined BF_SVN_VERSION (
	set LIBS=%LIBS%;%CD%\sdk\svn-%BF_SVN_VERSION%
) else (
	set LIBS=%LIBS%;%CD%\sdk\svn
)

"%JAVA_HOME%\bin\java.exe" -Xmx256M -cp "%ANT_CP%" "-Droot.dir=%BUILD_HOME%" org.apache.tools.ant.launch.Launcher -lib "%LIBS%" %*
set RETVAL=%ERRORLEVEL%

rem "%JAVA_HOME%\bin\java.exe" -Xmx256M -cp "%ANT_CP%" org.apache.tools.ant.Main "-Droot.dir=%BUILD_HOME%" %*
goto :end

:NoJavaFound
echo JAVA_HOME environment variable is not set. Set it pointing to the Java installation root directory.
RETVAL = 10
goto :end

:end
endlocal & set SCRIPT_RETVAL=%RETVAL%

exit /B %SCRIPT_RETVAL%