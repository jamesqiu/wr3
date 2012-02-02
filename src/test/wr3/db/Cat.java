package test.wr3.db;

import java.util.Date;

public class Cat {

		private String id;
		private String name;
		private boolean sex;
		private float weight;
		private double height = 30.0;
		private Date birth = new Date();
		
		public void setId(String id) {
			this.id = id;
		}
		public String getId() {
			return id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setSex(boolean sex) {
			this.sex = sex;
		}
		public boolean isSex() {
			return sex;
		}
		public void setWeight(float weight) {
			this.weight = weight;
		}
		public float getWeight() {
			return weight;
		}
		public void setHeight(double height) {
			this.height = height;
		}
		public double getHeight() {
			return height;
		}
		public void setBirth(Date birth) {
			this.birth = birth;
		}
		public Date getBirth() {
			return birth;
		}

//		public Cat() {
//		}
//
//		public String getId() {
//		return id;
//		}
//
//		@SuppressWarnings("unused")
//		private void setId(String id) {
//		this.id = id;
//		}
//
//		public String getName() {
//		return name;
//		}
//
//		public void setName(String name) {
//		this.name = name;
//		}
//
//		public boolean getSex() {
//		return sex;
//		}
//
//		public void setSex(boolean sex) {
//		this.sex = sex;
//		}
//
//		public float getWeight() {
//		return weight;
//		}
//
//		public void setWeight(float weight) {
//		this.weight = weight;
//		}
}
