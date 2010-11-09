@echo off

REM =====================================================================
REM New build framework project creation script.
REM =====================================================================
REM Launches ant with the proper build file and proper classpath
REM Pass specific targets as arguments to this build file if needed
REM =====================================================================

setlocal

set CLASSPATH=
set BASE_FOLDER=%CD%
set ANT_CP=%BASE_FOLDER%\lib\ant\ant-launcher.jar
set PATH=%PLATFORM_DIR%\bin;%PATH%

if not defined JAVA_HOME goto :NoJavaFound
if "%1" == "/?" goto :Usage
if "%1" == "-h" goto :Usage

REM ===================================
REM = Get the rest of the parameters. =
REM ===================================
set ADD_PARAMS=
:CheckForMore
if "%1" == "" goto :ParamsDone
set ADD_PARAMS=%ADD_PARAMS% %1
shift
goto :CheckForMore

:ParamsDone

set LIBS=%BASE_FOLDER%\lib\ant
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\svn
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\commons
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\libs-coboc2
set LIBS=%LIBS%;%BASE_FOLDER%\platform\int
set LIBS=%LIBS%;%BASE_FOLDER%\platform\ext
set LIBS=%LIBS%;%BASE_FOLDER%\platform\orc

cd %PROJECT_DIR%
"%JAVA_HOME%\bin\java.exe" -cp "%BASE_FOLDER%\lib\buildtasks.jar;%BASE_FOLDER%\lib\commons\commons-cli-1.0.jar" com.cordys.coe.ant.project.NewProjectLauncher -sdk "%BASE_FOLDER%" %ADD_PARAMS%

goto :end

:NoJavaFound
echo JAVA_HOME environment variable is not set. Set it pointing to the Java installation root directory.
goto :end

:Usage
"%JAVA_HOME%\bin\java.exe" -cp "%BASE_FOLDER%\lib\buildtasks.jar;%BASE_FOLDER%\lib\commons\commons-cli-1.0.jar" com.cordys.coe.ant.project.NewProjectLauncher -u
goto :end

:end
endlocal