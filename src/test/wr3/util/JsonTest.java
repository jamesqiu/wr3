package test.wr3.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Json;
import wr3.util.Numberx;
import wr3.util.Stringx;

public class JsonTest {

	String json;
	
	@Test
	public void jsonobject() throws JSONException {
		json = "{\r\n" + 
				"k1: null,\r\n" + 
				"k2: 3.14159,\r\n" + 
				"k3: \"cn中文\",\r\n" + 
				"k4: [66, 64, 87, 33, 47],\r\n" + 
				"k5: {head: [\"c0\", \"c1\", \"c2\"],\r\n" + 
				" data:[[55, 13, 39],\r\n" + 
				"       [77, 69, 89],\r\n" + 
				"       [65, 59, 85],\r\n" + 
				"       [26, 88, 35],\r\n" + 
				"       [33, 61, 46]]},\r\n" + 
				"k6: \"hello \\n world\",\r\n" + 
				"}";
		JSONObject o = new JSONObject(json);
		assertTrue(o.isNull("k1"));
		assertEquals(3.14159, o.get("k2"));
		assertEquals("cn中文", o.get("k3"));
		assertEquals(JSONArray.class, o.get("k4").getClass());
		assertEquals(JSONObject.class, o.get("k5").getClass());
		assertEquals("hello \n world", o.get("k6"));
	}
	

	@Test
	public void array() throws JSONException {
		
		assertEquals(0, Json.create(null).length());
		assertEquals(0, Json.create("").length());
		assertEquals(0, Json.create("[]").length());
		
		json = "[\"hello\\n\\\"james\\\"\", 1, 1111111111111111111, null, 3.1415]";
		JSONArray ja = Json.create(json);
		
		assertEquals(5, ja.length());
		
		assertEquals(String.class, ja.get(0).getClass());
		assertEquals(Integer.class, ja.get(1).getClass());
		assertEquals(Long.class, ja.get(2).getClass());
		assertEquals(JSONObject.NULL, ja.get(3));
		assertEquals(Double.class, ja.get(4).getClass());
		
		assertEquals("hello\n\"james\"", ja.getString(0));
		assertEquals(1, ja.getInt(1));
		assertEquals(1111111111111111111L, ja.getLong(2));
		assertTrue(ja.isNull(3));		
		assertTrue((ja.getDouble(4)-3.1415)==0.0);	
	}
	
	@Test
	public void array2() throws JSONException {

		json = "[\"hello world\", 1, 1111111111111111111, null, 3.1415]";
		JSONArray ja = Json.create(json);
		
		int n = ja.length();
		for (int i = 0; i < n; i++) {
			if (ja.isNull(i)) {
//				System.out.printf("[%d]: is null\n", i);
			} else {
				Object o = ja.get(i);
				if (Numberx.isNumber(o)) {
//					System.out.printf("[%d]: is number %s\n", i, o);
				} else if (Stringx.isString(o)) {
//					System.out.printf("[%d]: is String %s\n", i, o);
				} else {
//					System.out.printf("[%d] is unknown type", i);
				}
			}
		}
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(JsonTest.class.getName());
	}
}
