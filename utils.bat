@echo off

echo WebReport3 Util 应用：
echo.
echo   ru AllTests              运行所有TDD测试
echo   ru Currency              金额转换
echo   ru Exec                  从jvm执行外部命令
echo   ru IDUtil                身份证号码
echo   ru Pinyin                汉字全拼、简拼
echo   ru Goodname              中文^<--^>变量名称
echo   ru Dict                  中文^<--^>英文
echo   bin\dbconf.bat           查询RDB的所有配置，及单个配置的详细信息
echo.  
echo   bsh bin\cn.bsh           把指定字符串进行常见转码测试
echo   bsh bin\gorm.bsh         为Grails从数据库表构造domain类(例如abs, cust)
echo   bin\dbconfig.groovy      得到config xml中active的dbname，转换为Grails dataSource
echo   bin\genTestCode.py       生成指定带路径的TestCase类文件
echo.
echo   findJava.bat             查找含指定字符串的.java文件
echo.
echo ---------- web ----------
echo   ru FontImage             生成透明背景的文字图

@echo on
