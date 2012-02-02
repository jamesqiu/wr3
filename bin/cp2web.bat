rem ------ 更好的方案, 是使用junction ------
rem ------ 把 wr3.* 的所有classes拷贝到tomcat的wr3应用中 ------
@echo off

rem xcopy/e/y/q classes\wr3\* e:\tomcat\webapps\wr3\WEB-INF\classes\
xcopy/e/y/q/i classes\wr3\* e:\tomcat\webapps\wr3\WEB-INF\classes\wr3

echo cp2web.bat ^&^& tm 2 ^|^| java test.wr3.web.HttpTest "http://localhost:8080/wr3/App1?k1=cncn中文"

:end
@echo on
