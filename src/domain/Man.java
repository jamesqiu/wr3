package domain;

import static wr3.model.Annotationx.hasMeta;
import static wr3.model.Annotationx.isId;
import static wr3.model.Annotationx.meta;

import java.lang.reflect.Field;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotNull;

import wr3.Table;
import wr3.db.DbMeta;
import wr3.db.DbServer;
import wr3.db.Jpa;
import wr3.model.Meta;
import wr3.model.Order;
import wr3.util.Logx;

@Entity @Meta("伙计")
@Order({"name","age", "birth", "email"})
public class Man {

	@Id @GeneratedValue int id;
	@Meta("姓名") @NotNull String name;
	@Meta("年龄") int age;
	@Email String email = "anonymous@mail.com";
	@Meta("生日") Date birth = new Date();
	
	public Man(String name, int age) {
		this.name = name;
		this.age = age;
	}
	
	static void test2() {
		
		if (hasMeta(Man.class)) {
			System.out.printf("%s's meta: %s\n", Man.class.getName(), meta(Man.class));
		}
		
		for (Field f : Man.class.getDeclaredFields()) {			
			String name = f.getName();
			if (isId(f)) {
				System.out.printf("%s is id\n", name);
			}
			if (hasMeta(f)) {
				System.out.printf("%s's meta: %s\n", name, meta(f));
			} else {
				System.out.println(name);
			}
		}
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
	
		test2();
		
		Logx.hibernate();
		Jpa jpa = Jpa.create("h2", Man.class);
		jpa.save(new Man("qh", 20));
		jpa.save(new Man("james", 30));
		
		DbServer dbs = DbServer.create("h2");
		DbMeta meta = dbs.meta();
		System.out.println(meta.ddl("MAN"));
		Table t = dbs.query("select * from man");
		System.out.println(t);

		jpa.close();
		dbs.close();
	}
}
