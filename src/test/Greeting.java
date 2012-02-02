package test;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@SuppressWarnings("serial")
@Entity
//@Table(name="table1", catalog="test")
public class Greeting implements Serializable {
	
	@Id @GeneratedValue private int id;
	@Basic private String message;
	@Basic private String language;

	public Greeting() {
	}

	public Greeting(String message, String language) {
		this.message = message;
		this.language = language;
	}
	
	public int getId() {
		return id;
	}

	public String toString() {
		return "Greeting id=" + id + ", message=" + message + ", language="
				+ language;
	}
}
