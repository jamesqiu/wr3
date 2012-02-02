package test.wr3.table;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Table;
import wr3.table.CellFilter;

/**
 * 
 * @author jamesqiu 2009-9-27
 * @see TableFilter
 */
public class CellFilterTest {

	@Test
	public void process() {
		
		CellFilter filter = new CellFilter() {
			// ��0��2��4��������Ԫ�غ�ӡ���
			public Cell process(int row, int col, Cell cell) {
				if (row%2==0) {
					return Cell.create(""+cell.intValue()+"��");
				} else {
					return cell;
				}
			}
			// ��ͷ������
			public Cell process(int col, Cell cell) {
				return null;
			}
		};
		
		CellFilter filter2 = new CellFilter() {
			// ��ͷUpperCase����"("��")"
			public Cell process(int col, Cell cell) {
				return Cell.create("("+cell.value().toUpperCase()+")");
			}

			public Cell process(int row, int col, Cell cell) {
				return null;
			}
		};
			
		Table t = new Table(5, 3);
//		System.out.println(t);
		String s0 = t.cell(0, 0).value();
		String s1 = t.cell(1, 0).value();
		
		// ����Table��head
		for (int i = 0, n = t.cols(); i < n; i++) {
			Cell cell = t.head(i);
			t.head(i, filter2.process(i, cell));
		}
		// ����Tableÿһ��cell
		for (int i = 0, n = t.rows(); i < n; i++) {
			for (int j = 0, m = t.cols(); j < m; j++) {
				Cell cell = t.cell(i, j);
				t.cell(i, j, filter.process(i, j, cell));
			}
		}
		
//		System.out.println(t);
		assertEquals(3, t.rows());
		assertEquals(5, t.cols());
		assertEquals("(C0)", t.head(0).value());
		
		assertEquals(s0+"��", t.cell(0, 0).value());
		assertEquals(s1, t.cell(1, 0).value());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(CellFilterTest.class.getName());
	}
}
