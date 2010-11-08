@echo off

REM =====================================================================
REM Upgrade build framework project script.
REM =====================================================================
REM Launches ant with the proper build file and proper classpath.
REM The antscript will determine on the fly what the current version of
REM the framework is and will upgrade the scripts so that it fits the
REM current version of the SDK.
REM =====================================================================

setlocal

set CLASSPATH=
set BASE_FOLDER=%CD%
set ANT_CP=%BASE_FOLDER%\sdk\lib\ant\ant-launcher.jar

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
set LIBS=%BASE_FOLDER%\sdk\lib\ant

REM =====================================================================
REM There is one difficulty: What is the current folder? The basefolder
REM should point to the project root. So we need to determine what the 
REM base folder should do.
REM =====================================================================

REM =====================================================================
REM Scenario 1: It was started via sdk\upgrade.bat
REM =====================================================================
IF EXIST "%BASE_FOLDER%\build.bat" (
	IF EXIST "%BASE_FOLDER%\src" (
		echo Scenario 1
		GOTO :ProperBaseFolder
	) 
)

REM =====================================================================
REM Scenario 2: It was started in the sdk folder via upgrade.bat
REM =====================================================================
IF EXIST "%BASE_FOLDER%\build" (
	IF EXIST "%BASE_FOLDER%\config" (
		IF EXIST "%BASE_FOLDER%\lib" (
			echo Scenario 2
			CD ..
			FOR /F "TOKENS=1,2*" %%A IN ('CD') DO SET BASE_FOLDER=%%A
			GOTO :ProperBaseFolder
		)
	)
)

echo This command must be started from the project folder via sdk\upgrade.bat or in de sdk folder via upgrade.bat
GOTO :EndBatch

:ProperBaseFolder
echo Found the proper basefolder: %BASE_FOLDER%

set LIBS=%BASE_FOLDER%\sdk\lib\ant
@echo off

REM =====================================================================
REM Upgrade build framework project script.
REM =====================================================================
REM Launches ant with the proper build file and proper classpath.
REM The antscript will determine on the fly what the current version of
REM the framework is and will upgrade the scripts so that it fits the
REM current version of the SDK.
REM =====================================================================

setlocal

set CLASSPATH=
set BASE_FOLDER=%CD%
set PATH=%PLATFORM_DIR%\bin;%PATH%

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

REM =====================================================================
REM There is one difficulty: What is the current folder? The basefolder
REM should point to the project root. So we need to determine what the 
REM base folder should do.
REM =====================================================================

REM =====================================================================
REM Scenario 1: It was started via sdk\upgrade.bat
REM =====================================================================
IF EXIST "%BASE_FOLDER%\build.bat" (
	IF EXIST "%BASE_FOLDER%\src" (
		echo Scenario 1
		GOTO :ProperBaseFolder
	) 
)

REM =====================================================================
REM Scenario 2: It was started in the sdk folder via upgrade.bat
REM =====================================================================
IF EXIST "%BASE_FOLDER%\build" (
	IF EXIST "%BASE_FOLDER%\config" (
		IF EXIST "%BASE_FOLDER%\lib" (
			echo Scenario 2
			CD ..
			FOR /F "TOKENS=1,2*" %%A IN ('CD') DO SET BASE_FOLDER=%%A
			GOTO :ProperBaseFolder
		)
	)
)

echo This command must be started from the project folder via sdk\upgrade.bat or in de sdk folder via upgrade.bat
GOTO :EndBatch

:ProperBaseFolder
echo Found the proper basefolder: %BASE_FOLDER%

set LIBS=%BASE_FOLDER%\sdk\lib\ant
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\svn
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\commons
set LIBS=%LIBS%;%BASE_FOLDER%\sdk\lib\libs-coboc2
set LIBS=%LIBS%;%BASE_FOLDER%\platform\int
set LIBS=%LIBS%;%BASE_FOLDER%\platform\ext
set LIBS=%LIBS%;%BASE_FOLDER%\platform\orc

set ANT_CP=%BASE_FOLDER%\sdk\lib\ant\ant-launcher.jar

"%JAVA_HOME%\bin\java.exe" -cp "%BASE_FOLDER%\sdk\lib\buildtasks.jar;%BASE_FOLDER%\sdk\lib\commons\commons-cli-1.0.jar" com.cordys.coe.ant.bf.upgrade.UpgradeProjectLauncher -l "%BASE_FOLDER%" %ADD_PARAMS%

:EndBatch