@echo off
setlocal

if '%1'=='' goto L0
rem ------ ֱ�Ӱ�����ִ��
if %1==1 goto L1
if %1==2 goto L2
rem ------ ѡ���б�
:L0
echo WebReport3 Tools��
echo.
echo  1. �鿴DataSource
echo  2. ���ݿ�Meta��Ϣ
echo  0. �˳�
echo.
set /p answer=[ѡ�����ֻ���ĸ��س�]: 
rem ------ ����ѡ��ִ��
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
set /p dbname=[����dbname�鿴��ϸ]:
if '%dbname%'=='' (goto end)
:L1b
java tool.DbConfigTool %dbname%
goto end

:L2

goto end

rem ------ ����
:end
endlocal
@echo on
