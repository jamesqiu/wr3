package wr3.util;

import org.apache.commons.cli.*;

/**
 * @author jamesqiu 2007-8-27
 * <pre>
 * usage:<code>
	CLI cli1 = CLI.create()
	   .set("s", "sql", true, "[-S help here]")
	   .set("f", "file", true, "[-F help here]")
	   .parse(args);
	cli1.help("test2");	
	if (cli1.has("sql")) {
		System.out.println(cli1.get("sql"));
	}
 * </code></pre>
 */
public class CLI {

	Options options;
	CommandLine cli;
	
	public CLI() {
		options = new Options ();
	}
	
	/**
	 * CLI.create() 相当于 new CLI
	 * @return
	 */
	public static CLI create() {
		return new CLI();
	}
	
	/**
	 * set argments rule.
	 * @param opt1 命令简称，如: -i -a -n
	 * @param opt2 命令全称，如：-ignore -all -name
	 * @param hasArg 该命令是否必须，true：必须，false：可选
	 * @param descript 命令描述
	 * @return
	 */
	public CLI set (String opt1, String opt2, boolean hasArg, String descript) {
		options.addOption(opt1, opt2, hasArg, descript);
		return this;
	}
	
	/**
	 * call this after set(), before has() and get() .
	 */
	public CLI parse (String[] args) {

		try {
			cli = new PosixParser().parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * print help message.
	 */
	public void help (String head) {
		
		HelpFormatter help = new HelpFormatter();
		help.printHelp(head, options);
	}
	
	/**
	 * check if has defined option.
	 */
	public boolean has (String opt) {
		
		return cli.hasOption(opt);
	}
	
	/**
	 * check option's value.
	 * @param value expected opt's value
	 * @return get(opt)==value
	 */
	public boolean is (String opt, String value) {
		
		if (Stringx.nullity(opt) || Stringx.nullity(value)) return false;
		return value.equals(get (opt));
	}
	
	/**
	 * check option's int value.
	 * @param value expected opt's value
	 * @return get(opt)==value
	 */
	public boolean is (String opt, int value) {
		
		return value == getInt(opt);
	}
	
	/**
	 * get option's String value
	 */
	public String get (String opt) {
		
		return cli.getOptionValue(opt);
	}
	
	/**
	 * get options's int value.
	 */
	public int getInt (String opt) {

		String s = get (opt);
		return Numberx.toInt(s, -999);
	}

	//----------------- main() -----------------//
	public static void main(String[] args) {
		
		CLI cli1 = new CLI ()
			.set("s", "sql", true, "[-S help here]")
			.set("f", "file", false, "[-F help here]")
			.parse(args);
		cli1.help("-s need-this -f");	
		if (cli1.has("sql")) {
			System.out.println("-sql: " + cli1.get("sql"));
		}
		if (cli1.has("f")) {
			System.out.println("has option 'file'");
		}
	}
}
