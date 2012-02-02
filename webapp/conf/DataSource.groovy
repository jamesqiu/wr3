// jdbc
abs_grails {
	driver = jdbc.mssql
	url = "${jdbc.url.mssql}localhost:1433;DatabaseName=abs_grails"
	username = "sa"
	password = "pass"
}

gbms {
	driver = jdbc.oracle
	url = "${jdbc.url.oracle}10.60.108.58:1521:gbms"
	username = "gbmsdb"
	password = "gbmsdb"
}

db2 {
	driver = jdbc.db2
	url = "${jdbc.url.db2}10.80.1.46:50000/CBS400"
	username = "db2admin"
	password = "pass"
}

testdb {
	driver = jdbc.mysql
	url = "${jdbc.url.mysql}127.0.0.1:3306/webreport_example"
	username = 'root'
	password = 'root'
}

postgre {
	driver = jdbc.postgre
	url = "${jdbc.url.postgre}10.80.1.9:5432/abs"
	username = 'postgres'
	password = 'pass'
}

sybase1 {
	driver = jdbc.sybase
	url = "${jdbc.url.sybase}10.80.1.210:5000/WebReport"
	username = 'sa'
	password = '123456'
}

informix1 {
	driver = jdbc.informix
	url = "${jdbc.url.informix}127.0.0.1:1527/db_test:informixserver=tulua2"
	username = 'sa'
	password = 'ifx'
}

hsqlLocal {
	driver = jdbc.hsql
	url = "${jdbc.url.hsql}webreportdb"
	username = 'sa'
	password = ''
}

h2 {
	driver = jdbc.h2
	url = "${jdbc.url.h2}mem:testdb"
	username = ""
	password = ""
}

// jndi
apmis {
	driver = "jndi"
	name = "${jndi.prefix}demoPool"	
}

jndiTest {
	driver = "jndi"
	name = "${jndi.prefix}webreport_db"
}

// 缺省使用的数据源, 在最后一行定义.
dataSource = abs_grails;

