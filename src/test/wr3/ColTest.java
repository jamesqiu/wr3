package test.wr3;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Col;
import wr3.Row;


public class ColTest {

	Col col;
	
	@Test
	public void create() {
		col = new Col(new Row(5));
		col.head(new Cell("标题1"));
		assertNotNull(col);
		Row row = col.asRow();
		row.cell(0, new Cell("hello"));
		assertEquals(5, row.length());
	}
	
	@Test
	public void as() {
		Cell cell = new Cell("标题1");
		col = new Col(new Row(10)).head(cell);
		Row row = col.asRow();
		row.add(0, cell);
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(ColTest.class.getName());
	}
}
