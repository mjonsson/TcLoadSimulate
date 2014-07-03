@echo off

:: Set the SOA_CLIENT to the path of your Teamcenter Java SOA Client libraries, for example D:\App\Teamcenter_10_1\soa_client\java\libs
set SOA_CLIENT=D:\App\Teamcenter_10_1\soa_client\java\libs

:: !!! Do not perform any changes below this line !!!

if x%SOA_CLIENT%==x goto NO_SOA
if not exist "%SOA_CLIENT%\xerces.jar" goto NO_SOA

:: Find out system architecture, 32- or 64-bit
if %PROCESSOR_ARCHITECTURE%==x86 set SWT_LIB=%~dp0\swt_x86.jar
if %PROCESSOR_ARCHITECTURE%==AMD64 set SWT_LIB=%~dp0\swt_x86_64.jar

:: Verify that SWT_LIB exist
if x%SWT_LIB%==x goto NO_SWT
if not exist "%SWT_LIB%" goto NO_SWT

:: Start TcLoadSimulate
start "" /b java -cp "%~dp0\TcLoadSimulate.jar;%SWT_LIB%;%SOA_CLIENT%\*" com.teamcenter.TcLoadSimulate.TcLoadSimulate

goto EXIT

:NO_SOA
echo Please set the SOA_CLIENT variable to point to your SOA Client java libraries.

goto EXIT

:NO_SWT
echo Could not locate valid SWT libraries for the current system architecture.

goto EXIT

:EXIT
exit /b