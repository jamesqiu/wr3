package tool;

import wr3.text.LineFilter;
import wr3.text.TextFile;

import static wr3.util.Stringx.between;
import static wr3.util.Stringx.leftback;
import static wr3.util.Stringx.replaceAll;

/**
 * ��java -verbose������ı����ҳ�ת�ص����Լ�������jar�е�λ��.
 * 
 * @author jamesqiu 2009-6-18
 *
 */
public class VerboseFile {

	/**
	 * 
	 * @param file
	 */
	public void parse(String file) {
		
		TextFile.create(new LineHandler1()).process(file);
	}
	
	class LineHandler1 implements LineFilter {

		public String process(String line) {
			
			if (is_rt_jar(line)) {
				// rt.jar
				String classname = get_class_name(line);
				out("xcopy/y common\\rt.jar\\" + classname + ".class" +
						" out\\lib\\rt_jar\\" + leftback(classname, "\\") + "\\");
			} else if (is_charsets_jar(line)) {
				// charsets.jar
				String classname = get_class_name(line);
				out("xcopy/y common\\charsets.jar\\" + classname + ".class" +
						" out\\lib\\charsets_jar\\" + leftback(classname, "\\") + "\\");
			} else if (is_other_jar(line)) {
				// other *.jar
				err("�޷�����: " + line);
			} else if (is_file(line)) {
				// class file
				String classname = get_class_name(line);
				String classpath = get_class_path(line);
				out("xcopy/y " + classpath + classname + ".class" +
						" out\\" + leftback(classname, "\\") + "\\");
				
			}
			
			return null;
		}		
			
		// ����jre����jdk��rt.jar
		boolean is_rt_jar(String line) {
			return 
				( line.startsWith("[Loaded ") ) &&
				( line.endsWith(" from shared objects file]") || line.endsWith("\\rt.jar]") );
		}
		
		// ���� charsets.jar
		boolean is_charsets_jar(String line) {
			return 
				( line.startsWith("[Loaded ") ) &&
				( line.endsWith("\\charsets.jar]") );
		}
		
		// �������� .jar
		boolean is_other_jar(String line) {
			return 
				( line.startsWith("[Loaded ") ) &&
				( line.endsWith(".jar]") );
		}
		
		// �����ļ�ϵͳ
		boolean is_file(String line) {
			return 
				( line.startsWith("[Loaded ") ) &&
				( line.indexOf(" from file:") > 0 );
			
		}
		
		/**
		 * �õ�����, ����.class
		 * @param line
		 * @return ��: java\lang\String
		 */
		String get_class_name(String line) {
			String s = between(line, "[Loaded ", " from ");
			return replaceAll(s, ".", "\\");
		}
		
		/**
		 * �õ���path, ��: f:\dev3\classes\
		 * @param line
		 * @return
		 */
		String get_class_path(String line) {
			String s = between(line, " from file:/", "]");
			return replaceAll(s, "/", "\\");
		}
		
	}
	
	static void out(String s) {
		System.out.println(s);
	}
	
	static void err(String s) {
		System.err.println(s);
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("����verbose.txt�е����ļ�, usage: \n" + "    VerboseFile verboase.txt\n");
			return;
		}
		
		String verboseFile = args[0];
		VerboseFile o = new VerboseFile();
		o.parse(verboseFile);
	}
}
