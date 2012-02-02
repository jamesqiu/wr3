package wr3.util.tuple;

/**
 * A tuple of 4 integers separated by periods.
 * 
 * Because it is a Tuple, a Version properly displays, compares, and sorts.
 * Use the constructor to set the 4 components of a version number.
 * @author Michael L Perry
 * 
 */
public class Version extends Quadruple<Integer, Integer, Integer, Integer> {
    
    // A version is a tuple of 4 integers separated by periods.
    public Version(int major, int minor, int release, int qfe) {
    	super(major, minor, release, qfe);
    }
    
    public String toString() {
    	return toString("", ".", "");
    }
    
}
