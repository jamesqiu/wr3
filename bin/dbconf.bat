@echo off
setlocal

if '%1'=='' goto L0
rem ------ 直接按参数执行
if %1==1 goto L1
if %1==2 goto L2
rem ------ 选项列表
:L0
echo WebReport3 Tools：
echo.
echo  1. 查看DataSource
echo  2. 数据库Meta信息
echo  0. 退出
echo.
set /p answer=[选择数字或字母后回车]: 
rem ------ 根据选项执行
if '%answer%'=='' (goto L0)
if '%answer%'=='0' (goto end)
if '%answer%'=='1' (goto L1) 
if '%answer%'=='2' (goto L2)
goto L0

:L1
if '%2'=='' goto L1a
set dbname=%2
goto L1b
:L1a
java tool.DbConfigTool
echo.
set /p dbname=[输入dbname查看明细]:
if '%dbname%'=='' (goto end)
:L1b
java tool.DbConfigTool %dbname%
goto end

:L2

goto end

rem ------ 结束
:end
endlocal
@echo on
