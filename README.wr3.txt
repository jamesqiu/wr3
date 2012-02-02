------ WebReport3开发目录 ------
2008-11-23 by JamesQiu
@update 2011-07-07
Q: 为何叫wr3而非wr4, wr2
A: 主要为了区分第一、二版的WebReport，另一个原因是因为中指长一些，敲数字3最简单不易错 :)

0.------ todo list
  *** 做成REST和WebService的模式，分为data数据服务、table表格服务、chart图形服务;
  data数据服务输出格式为json，table、chart服务用jQuery或者Dojo来组合data服务

  把action加上参数，见src/app/package.html.
  更新util.bat, 把应用改为tool.*
  把chart及chart2.groovy移植过来
  重新设计wr3.web.AclFilter (参考dev下的)
  做一个wr3速查表(cheat-sheet), 开发人员可以快速参考.

1.------ 目录说明
  backup/     eclipse项目设置备份
  bin/        自动化及util脚本
  classes/    src编译的classes
  conf/       配置文件(数据库、encoding等)
  lib/        其他3rd依赖包
  test/       UnitTest .groovy代码及资源
    html/     html/js/css/ajax的静态测试
  src/        源码
  webapp/     wr3内置webapp,可把此目录junction到多个应用服务器的webapps/wr3，避免重复的copy和xml设置
  liftweb/    lift的webapp, 可把此目录junction到多个应用服务器（最好是jetty）的webapps/lieftweb，避免重复的copy和xml设置

  .classpath  eclipse34使用
  .project    eclipse34使用
  wr3.bsh     快速测试wr3的beanshell环境
  README.txt  本文档

2.------ wr3.home目录配置
  wr3.home 被 dev3.bat, ru.bat, bin\setcp.bat采用，也被wr3程序中采用。
  wr3.home 未配置的话，缺省为“.”， 如基本配置找 ./conf/base.properties
  在cli应用中设置wr3.home:
  		java -Dwr3.home=f:/dev3
  在web应用中设置wr3.home:
  		(WEB-INF/web.xml)
	<!-- AppFilter -->
	<filter>
		<filter-name>AppFilter</filter-name>
		<filter-class>wr3.web.AppFilter</filter-class>
		<init-param>
			 <param-name>wr3.home</param-name>
			 <param-value>e:/tomcat/webapps/wr3</param-value>
		 </init-param>
	</filter>
	<filter-mapping>
		<filter-name>AppFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

3.------ lib/下jar包说明
  groovy.jar               sql config, UnitTest (用Groovy 1.5，启动比1.6快近1s)
  commons-cli.jar          main(), CliUtil
  servlet-api.jar          Filter Servlet
  json.jar				   json字符串生成, toJson()
  freemarker.jar           模板生成, version2.3.15
  junit.jar                UnitTest
  httpunit.jar             http unit test, 可模拟servelt容器测试servlet
    -js.jar                httpunit 所需js解析器
    -nekohtml.jar          httpunit 所需html解析器
    -xercesImpl.jar        httpunit 所需xml解析器
  rdb-h2.jar               内存数据库,用于测试wr3.db.*
  rdb-sqlserver.jar        sqlserver2000 驱动
  rdb-postgresql.jar       postgresql 驱动
  rdb-mysql.jar            mysql 驱动
  hibernate.jar            wr3.db.Meta使用其dialect包, 3.3.1
    -slf4j.jar
	-slf4j-jdk14.jar
	-dom4j.jar
	-commons-collections.jar
	-jta.jar
  hibernate-entitymanager.jar           EJB3 JPA使用
  	-ejb3-persistence.jar
  	-hibernate-annotations.jar
  	-hibernate-commons-annotations.jar
  	-javassist.jar                      运行时需要
  	-antlr.jar                          运行时需要
  hibernate-validator.jar  JPA Validator
  je-analysis.jar          中文分词
    -lucene-core.jar
  cedict.jar               中英文字典
  pinyin.jar               汉字拼音对照表文本pinyin/pinyin.txt
  areacode.jar             身份证地区编码与地名对照文本areacode/areacode.txt
  com.nasoft.mt.jar        MT3.0 JNI
    -xSocket.jar           Mt4j使用的底层NIO类库
    -netty.jar             mt新版测试
    -grizzly-framework.jar mt新版测试
    -grizzly-http.jar
    -grizzly-nio-framework.jar
  wicket.jar               Wicket 测试
    -wicket-extensions.jar
    -wicket-datetime.jar
  hessian.jar              进行对象序列化Hessianx
  chart2.jar               已经解决了中文问题的chart2
  google-collect.jar       使用Multimap等jdk collection的扩展功能
  jai_core.jar             FontImage 使用本地字体生成文字图像（透明背景）
    -jai_codec.jar
    -commons-codec.jar
  scala-library.jar        使用scala写应用
    -scalaj-collection.jar 方便java-scala集合类转换
    -scala-compiler.jar    为了方便ant fsc运行而不必单独设置classpath，且放于此。
  swt.jar                  运行wr3.gui包内的swt应用，注意32位和64位的区别
  clojure.jar              clojure语言支持
    -clojure-contrib.jar   lib扩展
	-hiccup.jar            用Clojure语法生成html字符串，可用于CljServlet中
	-mongo-java-driver.jar mongodb的驱动
	-congomongo.jar        mongodb的wrapper
	-core.incubator.jar    mongodb的依赖包
	-data.json.jar         mongodb的依赖包

  jetty.jar                内嵌servlet/web服务器，把tool等做成web应用
    -jetty-util.jar        必须，否则编译出错
  javacsv.jar              读写csv文件，写简单，Csv.java就能实现；读采用该包

4.------ 其他
  控制Hibernate使用jdk logging时的log级别:
  a. 在 jre\lib\logging.properties(jdk16_u13是\lib\logging.properties) 中增加
     org.hibernate.level= WARNING
     或者在类中:
		static {
			Logger logger = Logger.getLogger("org.hibernate");
			logger.setLevel(Level.WARNING);
		};
     已经写在 wr3.util.Logx 中；

  b. 使用 java -Djava.util.logging.config.file=mylogging.properties
  // OFF > SEVERE > WARNING > INFO > CONFIG > FINE > FINER > FINEST > ALL

5.------ scalac
  - Project/clean 重新编译之前，确保关闭了使用dev3/classes的所有程序（主要是应用服务器）
  - 编译出奇怪错误的话，在项目属性中增/减junit3:
    先修改 wr3.scala.Tools.scala 后编译，
    再修改 test.scala.TestScala.java 后编译

6.------ 程序文档化
  把scala、java中所有可用的库和常用法都整合到wr3中，可不必另建word文档，在源文件中用tags标明。
  在Eclipse中，Ctrl-H可以进行全文搜索关键字，Ctrl-Shift-R可以收缩文件名
