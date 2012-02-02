package test.wr3.db;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wr3.db.DbServer;

/**
 * Hibernate3.3 dialet测试 
 * @author jamesqiu 2009-1-17
 *
 */
public class Hb3Test {

	/**
	 * 设置hibernate日志输出级别.
	 */
	static {
		Logger logger = Logger.getLogger("org.hibernate");
		logger.setLevel(Level.WARNING);
	};
	
	@Test
	/**
	 * 从一个class和一个Document得到create DDL
	 */
	public void dialect() throws ParserConfigurationException {
		
		Dialect dialect = new H2Dialect();
		String s = dialect.getTypeName(Types.CHAR);
		assertEquals("char($l)", s);
		
		// 使用一个Connection来初始化Hibernate Configuration
		Configuration conf = new Configuration();
		conf.setProperty("hibernate.dialect", dialect.getClass().getName());
		Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
//		LoggerFactory.getLogger(Configuration.class).
		DbServer server = DbServer.create("h2");
		conf.buildSessionFactory().openSession(server.connection());		

		// 构建一个表示Cat.hbm.xml的dom
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element r = doc.createElement("hibernate-mapping");
        Element r_cls = doc.createElement("class");
        Element r_cls_id = doc.createElement("id");
        Element r_cls_prop1 = doc.createElement("property");
        Element r_cls_prop2 = doc.createElement("property");
        Element r_cls_prop3 = doc.createElement("property");
        Element r_cls_prop4 = doc.createElement("property");
        Element r_cls_prop5 = doc.createElement("property");

        doc.appendChild(r);
        r.appendChild(r_cls);
        r_cls.appendChild(r_cls_id);
        r_cls.appendChild(r_cls_prop1);
        r_cls.appendChild(r_cls_prop2);
        r_cls.appendChild(r_cls_prop3);
        r_cls.appendChild(r_cls_prop4);
        r_cls.appendChild(r_cls_prop5);
        
        r_cls.setAttribute("name", "test.wr3.db.Cat");
        r_cls.setAttribute("table", "CAT");
        
        r_cls_id.setAttribute("name", "id");
        r_cls_id.setAttribute("type", "string");
        
        r_cls_prop1.setAttribute("name", "name");
        r_cls_prop2.setAttribute("name", "sex");
        r_cls_prop3.setAttribute("name", "weight");
        r_cls_prop4.setAttribute("name", "height");
        r_cls_prop5.setAttribute("name", "birth");

        conf.addDocument(doc);

        // 得到建表的原生SQL
		String[] ddl = conf.generateSchemaCreationScript(dialect);
		assertEquals(1, ddl.length);
		String rt = "create table CAT (" +
				"id varchar(255) not null, " +
				"name varchar(255), " +
				"sex boolean, " +
				"weight float, " +
				"height double, " + 
				"birth timestamp, " +
				"primary key (id))";
		assertEquals(rt, ddl[0]);
		
		ddl = conf.generateSchemaCreationScript(new SQLServerDialect());
		rt = "create table CAT (" +
				"id varchar(255) not null, " +
				"name varchar(255) null, " +
				"sex tinyint null, " +
				"weight float null, " +
				"height double precision null, " +
				"birth datetime null, " +
				"primary key (id))";
		assertEquals(1, ddl.length);
		assertEquals(rt, ddl[0]);

		//...
		Table table = new Table("cust");
		
		String sql = table.sqlCreateString(new PostgreSQLDialect(), 
				conf.buildMapping(), null, null);
		assertEquals("create table cust ()", sql);
		sql = table.sqlDropString(new SQLServerDialect(), null, null);
		assertEquals("drop table cust", sql);
		
		server.close();
	}
	
	@Test
	/**
	 * 从一个annotation class得到create DDL
	 */
	public void ejb3() {
		
	}
	
	@Test
	/**
	 * 从一个表字段得到create DDL, 好象得不到? 
	 */
	public void table() {
		Table table = new Table("table1");
		
		table.setPrimaryKey(new PrimaryKey());
		
		Column c1 = new Column("c1");
		c1.setSqlTypeCode(Types.VARCHAR);
		c1.setLength(20);	
		c1.setValue(new SimpleValue(table));
		table.addColumn(c1);

		Column c2 = new Column("c2");
		c2.setSqlTypeCode(Types.INTEGER);
		Value v2 = new SimpleValue(table);
		c2.setValue(v2);
		
		table.setAbstract(true);
		String sql;
		sql = table.sqlDropString(new PostgreSQLDialect(), null, null);
		assertEquals("drop table table1", sql);
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(Hb3Test.class.getName());
	}
}
