rem ------ ���õķ���, ��ʹ��junction ------
rem ------ �� wr3.* ������classes������tomcat��wr3Ӧ���� ------
@echo off

rem xcopy/e/y/q classes\wr3\* e:\tomcat\webapps\wr3\WEB-INF\classes\
xcopy/e/y/q/i classes\wr3\* e:\tomcat\webapps\wr3\WEB-INF\classes\wr3

echo cp2web.bat ^&^& tm 2 ^|^| java test.wr3.web.HttpTest "http://localhost:8080/wr3/App1?k1=cncn����"

:end
@echo on
