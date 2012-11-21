;; 数据源定义
;; dbconfig.clj == conf/Driver.groovy + conf/DataSource.groovy
(ns wr3.clj.dbconfig)

;;------------------------ 各种常用JDBC Drivers
(def drivers {
  :sqlserver    "com.microsoft.sqlserver.jdbc.SQLServerDriver" ; sqlserver 2005+
  :sqlserver2   "net.sourceforge.jtds.jdbc.Driver" ; sqlserver 2000~2005
  :oracle       "oracle.jdbc.driver.OracleDriver"
  :db2          "COM.ibm.db2.jdbc.net.DB2Driver"
  :mysql        "com.mysql.jdbc.Driver"
  :postgresql   "org.postgresql.Driver"
  :sybase       "com.sybase.jdbc2.jdbc.SybDriver"
  :informix     "com.informix.jdbc.IfxDriver"
  :hsql         "org.hsqldb.jdbcDriver"
  :h2           "org.h2.Driver"
})

;;------------------------ 各种常用JDBC url pattern
(def urls {
  :sqlserver    "jdbc:sqlserver://${ip};databaseName=${db}" 
  :sqlserver2   "jdbc:jtds:sqlserver://${ip};DatabaseName=${db}"
  :db2          "jdbc:db2://${ip}/${db}"
  :mysql        "jdbc:mysql://${ip}/${db}"
  :postgresql   "jdbc:postgresql://${ip}/${db}"
  :oracle       "jdbc:oracle:thin:@${ip}:${db}"
  :sybase       "jdbc:sybase:Tds:${ip}/${db}"
  :informix     "jdbc:informix-sqli:${ip}/${db}:informixserver=${server}"
  :hsql         "jdbc:hsqldb:${db}"
  :h2           "jdbc:h2:mem:${db}"
})
;;------------------------ J2EE容器JNDI pattern
(def jndi-webfirst {
	; 容器层面的设置, 如下为WebFirst
	:factory "com.nasoft.webfirst.jndi.interfaces.NamingContextFactory"
	:url     "127.0.0.1:1099"
	:prefix  "java:/jdbc/${dbsource}"
})

;;------------------------ 用户自定义数据源
(use 'wr3.clj.s)
(def dbnames 
  {
  :esp {:driver (:sqlserver2 drivers) ; 规划院试行db
           :url (gstring (:sqlserver2 urls) {:ip "localhost:1433" :db "userregister"}) ; db名从wabacusdemo3改为userregister
           :username "sa"
           :password "password"}

  :espdev {:driver (:sqlserver drivers) ; 服务器本地db
        :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "wabacusdemo3"})
        :username "sa"
        :password "passdb"}

  :abs {:driver (:sqlserver drivers)
        :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "abs"})
        :username "sa"
        :password "passdb"}

  :mssql {:driver (:sqlserver drivers)
        :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "vmarket"})
        :username "sa"
        :password "passdb"}

  :cnggc {:driver (:sqlserver drivers)
          :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "cnggc"})
          :username "sa"
          :password "passdb"}

  :bank  {:driver (:sqlserver drivers)
          :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "km_gf"})
          :username "sa"
          :password "passdb"}

  :gbms {:driver (:oracle drivers)
         :url (gstring (:oracle urls) {:ip "10.60.108.58:1521" :db "gbms"})
         :username "gbmsdb"
         :password "gbmsdb"}

  :gold {:driver (:oracle drivers)
         :url (gstring (:oracle urls) {:ip "10.80.1.48:1521" :db "ALLDB"})
         :username "gold"
         :password "gold"}

  :db2 {:driver (:db2 drivers)
	      :url (gstring (:db2 urls) {:ip "10.80.1.46:50000" :db "CBS400"})
	      :username  "db2admin"
	      :password "pass"}

  :testdb {:driver (:mysql drivers)
           :url (gstring (:mysql urls) {:ip "127.0.0.1:3306" :db "webreport_example"})
           :username "root"
           :password "root"}

  :postgre {:driver (:postgresql drivers)
	          :url (gstring (:postgresql urls) {:ip "localhost:5432" :db ""})
	          :username "postgres"
	          :password "passdb"}

  :sybase1 {:driver (:sybase drivers)
	          :url (gstring (:sybase urls) {:ip "10.80.1.210:5000":db "WebReport"})
	          :username "sa"
	          :password "123456"}

  :informix1 {:driver (:informix drivers)
	            :url (gstring (:informix urls) {:ip "127.0.0.1:1527":db "db_test" :server "tulua2"})
	            :username "sa"
	            :password "ifx"}

  :hsqlLocal {:driver (:hsql drivers)
	            :url (gstring (:hsql urls) {:db "webreportdb"})
	            :username "sa"
	            :password ""}

  :h2 {:driver (:h2 drivers)
	     :url (gstring (:h2 urls) {:db "testdb"})
	     :username ""
	     :password ""}

  :apmis {:driver 'jndi
          :factory (:factory jndi-webfirst)
          :url (:url jndi-webfirst)
          :name (gstring (:prefix jndi-webfirst) {:dbsource "demoPool"})}

  :jndiTest {:driver 'jndi
             :factory (:factory jndi-webfirst)
             :url (:url jndi-webfirst)
             :name (gstring (:prefix jndi-webfirst) {:dbsource "webreport_db"})}
})

; 缺省使用的数据源
(def dataname (:abs dbnames))


