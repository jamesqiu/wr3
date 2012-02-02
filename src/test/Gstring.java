package test;

import groovy.lang.GString;
import wr3.util.Stringx;

@SuppressWarnings("serial")
public class Gstring extends GString {
	
	private String[] strings;

	public Gstring(Object[] values) {
        this(values, new String[]{"Hello ", "!"});
	}

    public Gstring(Object[] values, String[] strings) {
        super(values);
        this.strings = strings;
    }
    
	@Override
	public String[] getStrings() {
		return strings;
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		Gstring o = new Gstring(new Object[] {"hello world"});
		System.out.println(Stringx.join(o.getStrings()));
		System.out.println(o.getValues()[0]);
		System.out.println(o.getValues());
		
	}
	
}
