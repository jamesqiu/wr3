package test;

import java.util.List;

import wr3.db.Jpa;
import wr3.util.Datetime;
import wr3.util.Logx;
import domain.Person;
import domain.Person.GENDER;

public class TestJpa {

	/**
	 * ������postgres���ݿ��person���в��������С�
	 */
	public void postgre() {
		
		Jpa jpa = Jpa.create("postgre", Person.class);
//		JpaConfig conf = new JpaConfig("postgre")
//			.dbCreate(DBCREATE_TYPE.CREATE)
//			.addClass(Person.class);
//		Jpa jpa = Jpa.create(conf);
		
		jpa.save(new Person("qh", 20, Datetime.date(1994, 10, 15)));
		jpa.save(new Person("james", 30, Datetime.date(1984, 10, 15)));
		jpa.save(new Person("����", 40, Datetime.date(1964, 10, 15)));
		// ��һ����¼�ĸ���
		Person p = (Person) jpa.list().get(0);
		System.out.println(p);
		jpa.save(p.age(125).gender(GENDER.MALE));
		
		List<?> rt = jpa.query("select p from Person p");
		System.out.println(rt.size());
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		Logx.hibernate();
		TestJpa o = new TestJpa();
		o.postgre();
	}

}
