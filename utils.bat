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
echo   ru FontImage             文字^<--^>透明图
echo   ru Tokenizer             中文分词
echo   bin\cn.bsh               中文字符转码测试
echo   bin\md5.bsh              求字符串md5
echo   bin\dbconf.bsh           查询RDB的所有配置，及单个配置的详细信息
echo.  
echo   bsh bin\gorm.bsh         为Grails从数据库表构造domain类(例如abs, cust)
echo   bin\dbconfig.groovy      得到config xml中active的dbname，转换为Grails dataSource
echo   bin\genTestCode.py       生成指定带路径的TestCase类文件
echo.
echo   findJava.bat             查找含指定字符串的.java文件
echo.
echo ---------- web ----------
echo   ru FontImage             生成透明背景的文字图

@echo on
