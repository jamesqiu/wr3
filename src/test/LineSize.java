package test;

import wr3.text.LineFilter;
import wr3.text.TextFile;

/**
 * 打印文件中大于MAX字节的行。
 * usage: java test.LineSize README.txt utils.bat
 * <pre>
--- Groovy:

def MAXLENGTH = 70
args.each { filename ->
    new File(filename).eachWithIndex { line, lineNumber ->
        def length = line.size()
        if (length > MAXLENGTH) {
            println "$filename line=$lineNumber chars=$length"
        }
    }
}

--- Ruby:

MAXLENGTH = 70

$*.each do |filename|
  File.open(filename) do |file|
    file.each_line do |line|
      length = line.chomp.length
      if length > MAXLENGTH
        puts "#{filename} line=#{$.} chars=#{length}"
      end
    end
  end
end


--- Scala:

import scala.io.Source, java.io.File
val args = List(new File("README.txt"), new File("utils.bat"))
val MAXLENGTH = 70
args.foreach { filename =>
  var file = Source.fromFile(filename)
  var counted = file.getLines.counted
  counted.foreach { line => 
    if (line.length - 1 > MAXLENGTH) {
      println(filename + " line=" + (counted.count+1) + " chars=" + line.length)
    }
  }
}

--- Python:

MAXLENGTH = 70 
def process(filename):
    for i,line in enumerate(open(filename)):      
        if len(line) > MAXLENGTH:
            print filename, "line=", i+1, "chars=", len(line)  
for f in sys.argv:
    process(f)

    </pre>
 * @author jamesqiu 2009-11-12
 */
public class LineSize {

	static void p(String filename, int line, int size) {
		System.out.printf("%s, line=%d, size=%d\n", filename, line, size);
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {		
		final int MAX = 70;
		for (final String filename : args) {
			TextFile.create(new LineFilter() {			
				int index = 0;
				public String process(String line) {
					index++;
					int size = line.getBytes().length;
					if (size>MAX) p(filename, index, size);
					return null;
				}
			}).process(filename);
		}
	}
}
