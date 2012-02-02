package test.wr3.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static wr3.Row.createByTypes;
import static wr3.util.Tablex.asList;
import static wr3.util.Tablex.asMap;
import static wr3.util.Tablex.rotate;
import static wr3.util.Tablex.sum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.table.GroupFilter;
import wr3.util.Tablex;

public class TablexTest {

	Table table = new Table(5, 10);
	
	@Before
	public void init() {
	}
	
	@Test
	public void testAsListTable() {
		
		List<Object> l = asList(table);
		assertEquals(table.rows(), l.size());
		assertEquals(table.cell(0, 0).intValue(), l.get(0));
		assertEquals(table.cell(-1, 0).intValue(), l.get(l.size()-1));
	}

	@Test
	public void testAsListTableInt() {
		
		int colIndex;
		List<Object> l;

		colIndex = -1;
		l = asList(table, colIndex);
		assertEquals(table.rows(), l.size());
		assertEquals(table.cell(0, colIndex).intValue(), l.get(0));
		assertEquals(table.cell(-1, colIndex).intValue(), l.get(l.size()-1));
		
		colIndex = 2;
		l = asList(table, colIndex);
		assertEquals(table.rows(), l.size());
		assertEquals(table.cell(0, colIndex).intValue(), l.get(0));
		assertEquals(table.cell(-1, colIndex).intValue(), l.get(l.size()-1));
	}

	@Test
	public void testAsMapTable() {
		
		Map<Object, Object> m;
		m = asMap(table);
		assertTrue(table.rows() >= m.size()); // Table可能有重的key
		assertTrue(m.containsKey(table.cell(0, 0).intValue()));
		assertTrue(m.containsKey(table.cell(-1, 0).intValue()));
	}

	@Test
	public void testAsMapTableIntInt() {

		Map<Object, Object> m;
		m = asMap(table, -2, -1);
		assertTrue(table.rows() >= m.size()); // Table可能有重的key
		assertTrue(m.containsKey(table.cell(0, -2).intValue()));
		assertTrue(m.containsKey(table.cell(-1, -2).intValue()));
	}

	@Test
	public void testRotate() {
		
		Table t2 = rotate(table);
		assertEquals(t2.rows(), table.cols());
		assertEquals(t2.cols(), table.rows() + 1);
		assertEquals(t2.cell(0, 0), table.head(0));
		assertEquals(t2.cell(-1, 0), table.head(-1));
		assertEquals(t2.cell(0, 1), table.cell(0, 0));
		assertEquals(t2.cell(0, -1), table.cell(-1, 0));
		assertEquals(t2.cell(-1, 1), table.cell(0, -1));
		assertEquals(t2.cell(-1, -1), table.cell(-1, -1));
	}

	@Test
	public void testSum() {
		
		Row sum = sum(table);
		int s0 = 0, s1 = 0, si = 0;
		for (int i = 0; i < table.rows(); i++) {
			s0 += table.cell(i, 0).intValue();
			s1 += table.cell(i, 1).intValue();
			si += table.cell(i, -1).intValue();
		}
		assertEquals(sum.cell(0).intValue(), s0);
		assertEquals(sum.cell(1).intValue(), s1);
		assertEquals(sum.cell(-1).intValue(), si);
		
		// 特殊情况测试
		sum = sum(new Table());
		assertEquals(0, sum.size());

		// 空结果集, 得到["","","","",""]
		sum = sum(new Table(5, 0)); // c0,c1,c2,c3,c4
		assertEquals(5, sum.size());
		assertTrue(sum.cell(0).isString());
		assertEquals("", sum.cell(0).value());
		
		// 0列
		sum = sum(new Table(0, 5)); //
		assertEquals(0, sum.size());
		
		// 类型
		Table t2 = new Table(5, 3);
		t2.cell(0, 0, new Cell(10.5)); // 第1列增加Double类型
		t2.cell(0, 1, new Cell("string")); // 第2列增加String类型
		assertTrue(sum(t2).cell(0).type()==Double.class);

		assertEquals(String.class, sum(t2).cell(1).type());
		assertEquals("", sum(t2).cell(1).value());

				
	}
	
	@Test
	public void sumWithFilter() {
		
		Row sum1 = sum(table);
		table.add(Row.createByTypes(null, 10, 10.0d, 0L, "cn"));
		Row sum2 = sum(table);
		// null不参与计算，但不影响其他cell的合计
		assertEquals(Long.class, sum1.cell(0).type());
		assertEquals(Long.class, sum2.cell(0).type());
		assertEquals(sum1.cell(0).longValue(), sum2.cell(0).longValue());
		
		assertEquals(Long.class, sum1.cell(1).type());
		assertEquals(Long.class, sum2.cell(1).type());
		assertEquals(sum1.cell(1).longValue()+10, sum2.cell(1).longValue());
		// 有double则结果变为double类型
		assertEquals(Long.class, sum1.cell(2).type());
		assertEquals(Double.class, sum2.cell(2).type());
		assertEquals(sum1.cell(2).doubleValue()+10d, sum2.cell(2).doubleValue(), 0d);
		
		assertEquals(Long.class, sum1.cell(3).type());
		assertEquals(Long.class, sum2.cell(3).type());
		assertEquals(sum1.cell(3).longValue(), sum2.cell(3).longValue());
		// 有String则该列不计算
		assertEquals(Long.class, sum1.cell(4).type());
		assertEquals(String.class, sum2.cell(4).type());
		assertEquals("", sum2.cell(4).value());
	}
	
	@Test
	public void sumRows() {
		
		Row sum = sum(table, Arrays.asList(0,-1));
		assertEquals(table.cell(0, 0).intValue()+table.cell(-1, 0).intValue(),
				sum.cell(0).intValue());
		assertEquals(table.cell(0, -1).intValue()+table.cell(-1, -1).intValue(),
				sum.cell(-1).intValue());
	}
	
	@Test
	public void avg() {
		
		// 特殊情况测试
		Row avg = Tablex.avg(new Table());
		assertEquals(0, avg.size());

		// 空结果集, 得到 ["","","","",""]
		avg = Tablex.avg(new Table(5, 0)); // c0,c1,c2,c3,c4
		assertEquals(5, avg.size());
		assertTrue(avg.cell(0).isString());
		assertEquals("", avg.cell(0).value());
		
		// 0列
		avg = sum(new Table(0, 5)); 
		assertEquals(0, avg.size());				
	}
	
	@Test
	public void max_min() {

		table = new Table()
			.head(Row.createHead(7))
			.add(createByTypes(1,   2, 3,    4L, 5.0, null, "r1"))
			.add(createByTypes(5.0, 4, 3,    2,  1,   null, "r2"))
			.add(createByTypes("9", 4, null, 2,  1,   null, "r3"))
		;
		Row max = Tablex.max(table);
		Row min = Tablex.min(table);
		// String类型不参与比较
		assertEquals(Double.class, max.cell(0).type());
		assertEquals(Integer.class, min.cell(0).type());
		assertEquals(5.0, max.cell(0).doubleValue(), 0d);
		assertEquals(1, min.cell(0).intValue());
		
		assertEquals(Integer.class, max.cell(1).type());
		assertEquals(Integer.class, min.cell(1).type());
		assertEquals(4, max.cell(1).intValue());
		assertEquals(2, min.cell(1).intValue());
		// null不参与比较
		assertEquals(Integer.class, max.cell(2).type());
		assertEquals(Integer.class, min.cell(2).type());
		assertEquals(3, max.cell(2).intValue());
		assertEquals(3, min.cell(2).intValue());
		
		assertEquals(Long.class, max.cell(3).type());
		assertEquals(Integer.class, min.cell(3).type());
		assertEquals(4L, max.cell(3).longValue());
		assertEquals(2, min.cell(3).intValue());
		
		assertEquals(Double.class, max.cell(4).type());
		assertEquals(Integer.class, min.cell(4).type());
		assertEquals(5.0, max.cell(4).doubleValue(), 0d);
		assertEquals(1, min.cell(4).intValue());
		// 全null
		assertEquals(new Cell(), max.cell(5));
		assertEquals(new Cell(), min.cell(5));
		// 全String
		assertEquals(new Cell(), max.cell(6));
		assertEquals(new Cell(), min.cell(6));
	}
	
	@Test
	public void group() {
		
		table = new Table()
			.head(Row.createHead(2))
			.add(createByTypes("a", 5))
			.add(createByTypes("b", 10))
			.add(createByTypes("c", 85))
			.add(createByTypes("c", 60))
			.add(createByTypes("c", 100))
		;
		
		GroupFilter filter = new GroupFilter() {
			public Cell process(Cell cell) {
				return (cell.intValue()>=60) ?
					new Cell("pass") : new Cell("fail");
			}
		};
		
		Tablex.group(table, 1, filter);
		assertEquals(3, table.cols());
		assertEquals("分组列", table.head(0).value());
		assertEquals("fail", table.cell(0, 0).value());
		assertEquals("fail", table.cell(1, 0).value());
		assertEquals("pass", table.cell(2, 0).value());
		assertEquals("pass", table.cell(3, 0).value());
		assertEquals("pass", table.cell(4, 0).value());
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(TablexTest.class.getName());
	}
	
}
