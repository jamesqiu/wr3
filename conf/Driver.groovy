jdbc {
	// class driver
	mssql	 = "com.microsoft.jdbc.sqlserver.SQLServerDriver"
	oracle	 = "oracle.jdbc.driver.OracleDriver"
	db2		 = "COM.ibm.db2.jdbc.net.DB2Driver"
	mysql	 = "com.mysql.jdbc.Driver"
	postgre	 = "org.postgresql.Driver"
	sybase	 = "com.sybase.jdbc2.jdbc.SybDriver"
	informix = "com.informix.jdbc.IfxDriver"
	hsql	 = "org.hsqldb.jdbcDriver"
	h2		 = "org.h2.Driver"

	// url prefix
	url {
		mssql	 = "jdbc:microsoft:sqlserver://"
		oracle	 = "jdbc:oracle:thin:@"
		db2		 = "jdbc:db2://"
		mysql	 = "jdbc:mysql://"
		postgre	 = "jdbc:postgresql://"
		sybase	 = "jdbc:sybase:Tds:"
		informix = "jdbc:informix-sqli:"
		hsql	 = "jdbc:hsqldb:"
		h2		 = "jdbc:h2:"
	}
}

jndi {
	// 容器层面的设置, 如下为WebFirst
	factory = "com.nasoft.webfirst.jndi.interfaces.NamingContextFactory"
	url = "127.0.0.1:1099"
	prefix = "java:/jdbc/"
}
