package domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import test.Greeting;
import wr3.util.Stringx;

/**
 * @see Greeting
 * @author jamesqiu 2009-12-21
 */
@Entity
public class Person implements Serializable {

	private static final long serialVersionUID = 4312672373794498623L;
	
	@Id @GeneratedValue int id;
	@Basic String name;
	@Basic int age;
	@Basic Date birthday;
//	@Basic String email = "anonymous@email.com";
	@Enumerated(EnumType.STRING) @Column(name="sex")
	GENDER gender = GENDER.NONE;
	
	public Person() {}
	
	public Person(String name, int age, Date birthday) {
		this.name = name;
		this.age = age;
		this.birthday = birthday;
	}
	
	public String name() {
		return name;
	}
	
	public int age() {
		return age;
	}
	
	public Person age(int age) {
		this.age = age;
		return this;
	}
	
	public Date birthday() {
		return birthday;
	}
		
	public int id() { return id; }
	
	public Person gender(GENDER gender) {
		this.gender = gender;
		return this;
	}
	
	@Override
	public String toString() {
		return Stringx.printf("%s, age=%d, birthday=%s, email=%s, gender=%s", 
				name, age, birthday, "none", gender);
	}
	
	public enum GENDER {
		MALE("ÄÐ"),
		FEMALE("Å®"),
		NONE("Î´Öª");
		
		private String s;
		GENDER(String s) { this.s = s; }
		public String toString() { return this.s; }
	};
}


