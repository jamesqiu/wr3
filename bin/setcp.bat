@rem ------- ����classpath������setcp1.bat
@echo off

@rem ------- ϵͳ������set dev3=f:\dev3

set classpath=.;%wr3.home%\classes
for %%i in (%wr3.home%\lib\*.jar) do (
	call setcp1.bat %%i
)
echo %classpath%
@echo on
