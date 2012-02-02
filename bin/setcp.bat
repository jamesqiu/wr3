@rem ------- 设置classpath，调用setcp1.bat
@echo off

@rem ------- 系统变量：set dev3=f:\dev3

set classpath=.;%wr3.home%\classes
for %%i in (%wr3.home%\lib\*.jar) do (
	call setcp1.bat %%i
)
echo %classpath%
@echo on
