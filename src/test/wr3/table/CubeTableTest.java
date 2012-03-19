package test.wr3.table;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.table.CubeTable;
import wr3.util.Rowx;

public class CubeTableTest {

	Table data = new Table();
	Table rt;
	CubeTable cube = CubeTable.create();
	
	@Before
	public void setUp() throws Exception {
		data.head(Row.createByTypes("orgid", "month", "name", "hq", "dq"));
		data.add(Row.createByTypes("001", "1��", "aa", 10, 1000));
		data.add(Row.createByTypes("001", "1��", "bb", 30, 3000));
		data.add(Row.createByTypes("001", "2��", "cc", 50, 5000));
		data.add(Row.createByTypes("001", "3��", "dd", 70, 7000));
		data.add(Row.createByTypes("002", "1��", "ee", 90, 9000));
		data.add(Row.createByTypes("002", "2��", "ff", 0,  0)); 
		data.add(Row.createByTypes("002", "2��", "aa", 20, 2000));
		data.add(Row.createByTypes("002", "3��", "cc", 40, 4000));
		
		cube.meta(Arrays.asList("����","�·�","����","����","����"));
	}
	
	@Test
	public void ��ά�ȵ�ָ��() {
		
		rt = cube.data(data).layout("orgid\\month,[hq]").result();
		assertEquals(1+2, rt.rows());
		assertEquals(1+3, rt.cols());
		assertEquals(Row.createByTypes("","1��","2��","3��"), rt.row(0));
		assertEquals(Row.createByTypes("001", 10+30, 50, 70), rt.row(1));
		assertEquals(Row.createByTypes("002", 90, 0+20, 40), rt.row(2));
		
		rt = cube.data(data).layout("orgid, [hq]\\month").result();
		assertEquals(1+2, rt.rows());
		assertEquals(1+3, rt.cols());
		assertEquals(Row.createByTypes("","1��","2��","3��"), rt.row(0));
		assertEquals(Row.createByTypes("001", 10+30, 50, 70), rt.row(1));
		assertEquals(Row.createByTypes("002", 90, 0+20, 40), rt.row(2));
		
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
	}
	
	@Test
		public void ��ά�ȶ�ָ��() {
			
			rt = cube.data(data).layout("orgid \\ month, [dq, hq]").result();
			assertEquals(2+2, rt.rows());
			assertEquals(1+3*2, rt.cols());
			assertEquals(Row.createByTypes("",
					"1��","1��","2��","2��","3��","3��"), rt.row(0));
			assertEquals(Row.createByTypes("",
					"����","����","����","����","����","����"), rt.row(1));
			assertEquals(Row.createByTypes("","",
					"001","002"), rt.col(0).asRow());
			assertEquals(Row.createByTypes("001", 4000, 40, 5000, 50, 7000, 70),
					rt.row(2));
			assertEquals(Row.createByTypes("002", 9000, 90, 2000, 20, 4000, 40),
					rt.row(3));
			
			rt = cube.data(data).layout("month, [dq, hq] \\ orgid").result();
			assertEquals(2+2, rt.cols());
			assertEquals(1+3*2, rt.rows());
			assertEquals(Row.createByTypes("",
					"1��","1��","2��","2��","3��","3��"), rt.col(0).asRow());
			assertEquals(Row.createByTypes("",
					"����","����","����","����","����","����"), rt.col(1).asRow());
			assertEquals(Row.createByTypes("","",
					"001","002"), rt.row(0));
			assertEquals(Row.createByTypes("001", 4000, 40, 5000, 50, 7000, 70),
					rt.col(2).asRow());
			assertEquals(Row.createByTypes("002", 9000, 90, 2000, 20, 4000, 40),
					rt.col(3).asRow());
			
			//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		}

	@Test
	public void ��ά�ȵ�ָ��() {
		
		rt = cube.data(data).layout("orgid,month\\name,[hq]").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(2+6, rt.cols());
		assertEquals(1+2*3, rt.rows());

		rt = cube.data(data).layout("orgid,month,[hq]\\name").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(2+6, rt.cols());
		assertEquals(1+2*3, rt.rows());
	}
	
	@Test
	public void ��ά�ȶ�ָ��() {
		
		rt = cube.data(data).layout("orgid,month\\name,[dq,hq]").result();
		assertEquals(2+6, rt.rows());
		assertEquals(2+6*2, rt.cols());
		assertEquals(Row.createByTypes("","",
				"aa","aa",
				"bb","bb",
				"cc","cc",
				"dd","dd",
				"ee","ee",
				"ff","ff") , rt.row(0));
		assertEquals(Row.createByTypes("","",
				"����","����",
				"����","����",
				"����","����",
				"����","����",
				"����","����",
				"����","����") , rt.row(1));
		assertEquals(Row.createByTypes("","",
				"001","001","001",
				"002","002","002"), rt.col(0).asRow());
		assertEquals(Row.createByTypes("","",
				"1��","2��","3��",
				"1��","2��","3��"), rt.col(1).asRow());
		assertEquals(Row.createByTypes("aa","����",
				1000,null,null,null,2000,null), rt.col(2).asRow());
		assertEquals(Row.createByTypes("aa","����",
				10,null,null,null,20,null), rt.col(3).asRow());
		
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
	}

	@Test
	public void ����ά��() {

		rt = cube.data(data).layout("month,[hq]\\").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(3, rt.rows());
		assertEquals(1+1, rt.cols());
		assertEquals(Row.createByTypes("1��","2��","3��"), rt.col(0).asRow());
		assertEquals(new Cell(10+30+90), rt.cell(0,1));
		assertEquals(new Cell(50+0+20), rt.cell(1,1));
		assertEquals(new Cell(70+40), rt.cell(2,1));
		
		rt = cube.data(data).layout("orgid,name\\[hq,dq]").result();
		assertEquals(Row.createByTypes("", "", "����", "����"), rt.row(0));
		
		assertEquals(Row.createByTypes("001", "aa", 10, 1000), rt.row(1));
		assertEquals(Row.createByTypes("001", "bb", 30, 3000), rt.row(2));
		assertEquals(Row.createByTypes("001", "cc", 50, 5000), rt.row(3));
		assertEquals(Row.createByTypes("001", "dd", 70, 7000), rt.row(4));
		
		assertEquals(Row.createByTypes("002", "aa", 20, 2000), rt.row(5));
		assertEquals(Row.createByTypes("002", "cc", 40, 4000), rt.row(6));
		assertEquals(Row.createByTypes("002", "ee", 90, 9000), rt.row(7));
		assertEquals(Row.createByTypes("002", "ff",  0,    0), rt.row(8));
		
	}
	
	@Test
	public void ����ά��() {
		
		rt = cube.data(data).layout("\\month,[hq]").result();
		assertEquals(Row.createByTypes("1��","2��","3��"), rt.row(0));
		assertEquals(new Cell(10+30+90), rt.cell(1,0));
		assertEquals(new Cell(50+0+20), rt.cell(1,1));
		assertEquals(new Cell(70+40), rt.cell(1,2));

		rt = cube.data(data).layout("[hq,dq]\\orgid,name").result();
		assertEquals(2+1*2, rt.rows());
		assertEquals(1+2*4, rt.cols());
		
		assertEquals(Row.createByTypes("", "", "����", "����"), rt.col(0).asRow());
		
		assertEquals(Row.createByTypes("001", "aa", 10, 1000), rt.col(1).asRow());
		assertEquals(Row.createByTypes("001", "bb", 30, 3000), rt.col(2).asRow());
		assertEquals(Row.createByTypes("001", "cc", 50, 5000), rt.col(3).asRow());
		assertEquals(Row.createByTypes("001", "dd", 70, 7000), rt.col(4).asRow());
		
		assertEquals(Row.createByTypes("002", "aa", 20, 2000), rt.col(5).asRow());
		assertEquals(Row.createByTypes("002", "cc", 40, 4000), rt.col(6).asRow());
		assertEquals(Row.createByTypes("002", "ee", 90, 9000), rt.col(7).asRow());
		assertEquals(Row.createByTypes("002", "ff",  0,    0), rt.col(8).asRow());
		
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
	}
	
	// ����ûд�ϡ���ά��
	@Test
	public void ��ά��() {
		
		rt = cube.data(data).layout("[hq]").result();
		assertEquals(1, rt.rows());
		assertEquals(1, rt.cols());
		assertEquals(Rowx.sum(data.col(3).asRow()), rt.cell(0,0));
		
		rt = cube.data(data).layout("\\[hq]").result();
		assertEquals(1, rt.rows());
		assertEquals(1, rt.cols());
		assertEquals(Rowx.sum(data.col(3).asRow()), rt.cell(0,0));

		rt = cube.data(data).layout("[dq,hq]\\").result();
		assertEquals(2, rt.rows());
		assertEquals(2, rt.cols());
		assertEquals(Row.createByTypes("����",31000), rt.row(0));
		assertEquals(Row.createByTypes("����",310), rt.row(1));
		
		rt = cube.data(data).layout("\\[dq,hq]").result();
		assertEquals(2, rt.rows());
		assertEquals(2, rt.cols());
		assertEquals(Row.createByTypes("����",31000), rt.col(0).asRow());
		assertEquals(Row.createByTypes("����",310), rt.col(1).asRow());
		
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
	}
	
	// ����ûдָ��
	public void ��ָ��() {

		rt = cube.data(data).layout("month").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(3, rt.rows());
		assertEquals(1, rt.cols());
		assertEquals(Row.createByTypes("1��","2��","3��"), rt.col(0).asRow());
		
		rt = cube.data(data).layout("\\month,orgid").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(3*2, rt.cols());
		assertEquals(2, rt.rows());
		assertEquals(Row.createByTypes("1��","1��","2��","2��","3��","3��"), rt.row(0));
		assertEquals(Row.createByTypes("001","002","001","002","001","002"), rt.row(1));

		rt = cube.data(data).layout("month \\ orgid").result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(1+3, rt.rows());
		assertEquals(1+2, rt.cols());
		assertEquals(Row.createByTypes("","1��","2��","3��"), rt.col(0).asRow());
		assertEquals(Row.createByTypes("","001","002"), rt.row(0));
		assertNull(rt.cell(1, 1)); assertNull(rt.cell(1, 2));
		assertNull(rt.cell(2, 1)); assertNull(rt.cell(2, 2));
		assertNull(rt.cell(3, 1)); assertNull(rt.cell(3, 2));
	}
	 
	// ����ûд�ϡ���ά�Ⱥ�ָ��
	@Test
	public void ��ά����ָ��() {
		
		rt = cube.data(data).layout(null).result();
//		System.out.printf("data=%s-->rt=%s\n", data, rt);
		assertEquals(0, rt.rows());
		assertEquals(0, rt.cols());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(CubeTableTest.class.getName());
	}
}
