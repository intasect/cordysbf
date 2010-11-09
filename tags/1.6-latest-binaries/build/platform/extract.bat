@echo off

REM ==================================================
REM Extract Cordys BCP jars for the plaform externals
REM ==================================================

setlocal

if "%1"=="/?" goto displayUsage
if "%1"=="" goto displayUsage
set PROP_CORDYS_HOME=-Dcordys.home=%1
shift

if "%1"=="" (
	set PROP_SVN_URL=
) else (
	set PROP_SVN_URL=-Dsvn.externals.base.url=%1
)
shift

if "%1"=="" (
	set PROP_SVN_USER=
) else (
	set PROP_SVN_USER=-Dsvn.username=%1
)
shift

set ROOT_DIR=%CD%
set BUILD_FILE=sdk/build/platform/extract-jars.xml

set CLASSPATH=

set ANT_CP=%ROOT_DIR%\sdk\lib\ant\ant-launcher.jar

set LIBS=%ROOT_DIR%\sdk\lib\ant
set LIBS=%LIBS%;%ROOT_DIR%\sdk\lib

if "%PROP_SVN_URL%"=="" (
	"%JAVA_HOME%\bin\java.exe" -Xmx512M -cp "%ANT_CP%" "%PROP_CORDYS_HOME%" org.apache.tools.ant.launch.Launcher -lib "%LIBS%" -f %BUILD_FILE%
) else (
	"%JAVA_HOME%\bin\java.exe" -Xmx512M -cp "%ANT_CP%" "%PROP_CORDYS_HOME%" "%PROP_SVN_URL%" "%PROP_SVN_USER%" org.apache.tools.ant.launch.Launcher -lib "%LIBS%" -f %BUILD_FILE%	
)

goto :endLocal

:displayUsage
echo Extracts Cordys BCP jars for the plaform externals
echo Usage: extract [Cordys home] [SVN externals base URL] [SVN username]
echo Example (commits the jars to Subversion):
echo    extract "C:\\Cordys" http://server/svn/repos/Cordys/config/platform svnuser
echo Example (leaves jars in the build folder):
echo    extract "C:\\Cordys"
:endLocal
endlocal