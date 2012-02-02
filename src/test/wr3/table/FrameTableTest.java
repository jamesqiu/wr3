package test.wr3.table;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Col;
import wr3.Row;
import wr3.Table;
import wr3.table.FrameTable;

public class FrameTableTest {

	Table data1 = new Table().head(Row.createByTypes("org", "name", "amount"))
		.add(Row.createByTypes("01", "����", 10000))
		.add(Row.createByTypes("02", "����",  5000))
		.add(Row.createByTypes("03", "����", 20000))
	;
	
	Table data2 = new Table().head(Row.createByTypes("org", "hq", "dq"))
		.add(Row.createByTypes("01", 200, 1000))
		.add(Row.createByTypes("02", 300, 3000))
		.add(Row.createByTypes("03", 100, 2000))
		.add(Row.createByTypes("04", 500, 7000))
	;
	
	Table data3 = new Table().head(Row.createByTypes("org2", "hq2", "amount2"))
		.add(Row.createByTypes("001", 20, 1))
		.add(Row.createByTypes("002", 30, 3))
		.add(Row.createByTypes("003", 40, 5))
		.add(Row.createByTypes("004", 50, 7))
		.add(Row.createByTypes("005", 60, 9))
	;
	
	FrameTable frame = FrameTable.create();
	
	/**
	 * ����ͬʱ����top��left�����
	 */
	@Test
	public void byTopLeft() {
		
		Row top  = Row.createByTypes("org", "hq", "amount", "dq");
		Col left = Row.createByTypes("04", "02", "01").asCol();
		
		frame.top(top).left(left)
			.put(data1).put(data2);
		
		Table rt = frame.result();
		
		assertEquals(4, rt.cols());
		assertEquals(3, rt.rows());
		assertEquals(top, rt.head());
		assertTrue(Row.createByTypes("04", 500,  null, 7000).equals(rt.row(0)));
		assertTrue(Row.createByTypes("02", 300,  5000, 3000).equals(rt.row(1)));
		assertTrue(Row.createByTypes("01", 200, 10000, 1000).equals(rt.row(2)));
	}
	
	/**
	 * ����ֻ����top��������left�����
	 */
	@Test
	public void byTop() {
		
		Row top = Row.createByTypes("org", "hq", "amount", "dq");
		
		frame.top(top)
			.put(data1).put(data2);
		
		Table rt = frame.result();
//		System.out.printf("%s\n%s\n%s", data1, data2, rt);
		
		assertEquals(4, rt.cols());
		assertEquals(4, rt.rows());
		assertEquals(top, rt.head());
		assertTrue(Row.createByTypes("01", 200, 10000, 1000).equals(rt.row(0)));
		assertTrue(Row.createByTypes("02", 300,  5000, 3000).equals(rt.row(1)));
		assertTrue(Row.createByTypes("03", 100, 20000, 2000).equals(rt.row(2)));
		assertTrue(Row.createByTypes("04", 500,  null, 7000).equals(rt.row(3)));
	}
	
	@Test
	public void byLeft() {
		
		Col left = Row.createByTypes("04", "02", "01").asCol();

		frame.left(left)
			.put(data1)
			.put(data2)
		;
		
		Table rt = frame.result();
//		System.out.printf("%s\n%s\n%s", data1, data2, rt);
		
		assertEquals(5, rt.cols());
		assertEquals(3, rt.rows());
		assertEquals(Row.createByTypes("org", "name", "amount", "hq", "dq"), rt.head());
		assertTrue(Row.createByTypes("04", null, null, 500, 7000).equals(rt.row(0)));
		assertTrue(Row.createByTypes("02", "����", 5000, 300, 3000).equals(rt.row(1)));
		assertTrue(Row.createByTypes("01", "����", 10000, 200, 1000).equals(rt.row(2)));
	}
	
	@Test
	public void byData1() {
		
		Table data = new Table().head(Row.createByTypes("org", "hq", "amount", "dq"))
			.add(Row.createByTypes("04", null, null, null))
			.add(Row.createByTypes("02", null, null, null))
			.add(Row.createByTypes("01", null, null, null))
		;
		
		frame.data(data)
			.put(data1)
			.put(data2)
		;
		
		Table rt = frame.result();
//		System.out.printf("%s\n%s\n%s", data1, data2, rt);
		
		assertEquals(4, rt.cols());
		assertEquals(3, rt.rows());
		assertEquals(Row.createByTypes("org", "hq", "amount", "dq"), rt.head());
		assertTrue(Row.createByTypes("04", 500, null,  7000).equals(rt.row(0)));
		assertTrue(Row.createByTypes("02", 300, 5000,  3000).equals(rt.row(1)));
		assertTrue(Row.createByTypes("01", 200, 10000, 1000).equals(rt.row(2)));		
	}
	
	@Test
	public void byDatas() {
		// ��1����ͬ
		frame
			.put(data1)
			.put(data2)
		;
		Table rt = frame.result();
		assertByDatas(rt);
		// ��1�в�ͬ�����в���
		frame = FrameTable.create()
			.put(data1)
			.put(data3)
		;
		rt = frame.result();
		assertEquals(data1.cols()+data3.cols(), rt.cols());
		assertEquals(Math.max(data1.rows(), data3.rows()), rt.rows());
		assertEquals(data1.head().join(data3.head()), rt.head());
		
//		System.out.printf("%s\n%s\n%s", data1, data3, rt);
	}
	
	private void assertByDatas(Table rt) {
		
		assertEquals(5, rt.cols());
		assertEquals(4, rt.rows());
		assertEquals(Row.createByTypes("org", "name", "amount", "hq", "dq"), rt.head());
		assertTrue(Row.createByTypes("01", "����", 10000, 200, 1000).equals(rt.row(0)));
		assertTrue(Row.createByTypes("02", "����", 5000,  300, 3000).equals(rt.row(1)));
		assertTrue(Row.createByTypes("03", "����", 20000, 100, 2000).equals(rt.row(2)));		
		assertTrue(Row.createByTypes("04", null,   null,  500, 7000).equals(rt.row(3)));		
	}
	
	@Test
	// ��������߽����
	public void nullData() {
		
		// 1���������κ�����
		Table rt = frame.result();
		assertNullData(rt);
		
		// 2������ topCodes  Ϊ new Row(), --> result = []
		frame = FrameTable.create()
			.top(new Row())
			.put(data1)
			.put(data2)
		;
		rt = frame.result();
		assertNullData(rt);
		
		// 3������ leftCodes Ϊ new Col(), --> result ֻ�� head
		frame = FrameTable.create()
			.left(new Row().asCol())
			.put(data1)
			.put(data2)
		;
		rt = frame.result();
		assertEquals(0, rt.rows());
		assertEquals(5, rt.cols());
		assertEquals(Row.createByTypes("org", "name", "amount", "hq", "dq"), 
				rt.head());
		
		// 4������ data Ϊ new Table()
		frame = FrameTable.create()
			.data(new Table())
			.put(data1)
			.put(data2)
		;
		rt = frame.result();
		assertNullData(rt);
		
		// 5��put������Ϊ new Table(), ��Ӱ������
		frame = FrameTable.create()
			.put(new Table())
			.put(data1)
			.put(data2)
		;
		rt = frame.result();
		assertByDatas(rt);
		
		// 6��ֻ������ data(), û��put����
		frame = FrameTable.create()
			.data(data1)
		;
		rt = frame.result();
		assertEquals(data1.head(), rt.head());
		assertEquals(data1.rows(), rt.rows());
		for (int i = 0; i < data1.rows(); i++) {
			assertEquals(data1.row(i), rt.row(i));			
		}
		
//		System.out.println(rt);
	}
	
	private void assertNullData(Table rt) {
		
		assertNotNull(rt);
		assertEquals(0, rt.rows());
		assertEquals(0, rt.cols());
	}
	
	@Test
	// ���� leftCodes ��Ψһ�����
	public void leftCodesNotUniq() {
		
		Table data3 = new Table().head(Row.createByTypes("org", "hq", "dq"))
			.add(Row.createByTypes("01", 200, 1000))
			.add(Row.createByTypes("02", 300, 3000))
			.add(Row.createByTypes("01", 100, 2000))
			.add(Row.createByTypes("04", 500, 7000))
		;
		frame
			.put(data3)
		;
		Table rt = frame.result();
		// ���������ʵ��ʹ������ò�Ҫ��������3�л������1��
		assertEquals(data3.cols(), rt.cols());
		assertEquals(data3.rows(), rt.rows());
		assertEquals(data3.row(2), rt.row(0));
		assertEquals(data3.row(1), rt.row(1));
		assertEquals(Row.createByTypes(null,null,null), rt.row(2));
		assertEquals(data3.row(3), rt.row(3));
		
//		System.out.printf("%s\n%s\n", data3, frame.result());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(FrameTableTest.class.getName());
	}
}

