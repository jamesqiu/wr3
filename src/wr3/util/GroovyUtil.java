package wr3.util;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/**
 * Groovy utility, 执行Groovy脚本.
 * <pre>
 * usage:
 * 1) .groovy file: 	// 执行脚本文件 
 *	gr = GroovyUtil.create()
 *	  .set("x", "100").set("y", new Integer(100));
 *	gr.run ("test.groovy");
 * 	gr.get("z");
 * 
 * 2) express string:	// 执行脚本片段
 * 	express = "z = \"id${x}: ${y**2}\", 'return value'";
 * 	gr = GroovyUtil.Express.create(express)
 * 	  .set ("x", "100").set ("y", new Integer(100));
 * 	rt = gr.eval ();
 * </pre>
 * @see http://groovy.codehaus.org/Embedding+Groovy
 * 
 * @author james
 * 
 * @see Script
 * @see TestGroovyUtil
 */
public class GroovyUtil {

	private Binding binding;			// store all vars' {name:values}
	private GroovyScriptEngine engine;
	
	public GroovyUtil () {
		initBinding();
		initEngine ();
	}
	
	private void initBinding() {
		binding = new Binding();
	}
	
	private void initEngine () {		
		try {
			engine = new GroovyScriptEngine("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get instance with GroovyScriptEngine
	 * for Express string eval, and .groovy file run.  
	 * @return
	 */
	public static GroovyUtil create() {
		
		return new GroovyUtil();
	}
	
	/**
	 * set var's {name:value}
	 * @param name	variable name
	 * @param value	variable value
	 */
	public GroovyUtil set (String name, Object value) {
		
		binding.setVariable(name, value);
		return this;
	}
		
	/**
	 * run groovy script file 
	 * @param express bsh script source string
	 * @return last var's value
	 */
	public Object run (String filename) {
		
		if (!Filex.has(filename)) {
			new FileNotFoundException().printStackTrace();
			return null;
		}
		
		Object rt = null;

		try {
			rt = engine.run (new File(filename).toURI().toString(), binding);
		} catch (ResourceException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return rt;
	}

	/**
	 * get all var's name (NOT value), like "x = 10"
	 * not include properties like "def x = 10"
	 * @return
	 */
	public String[] getVars () {
		
		Set<?> keys = binding.getVariables().keySet();
		String[] vars = (String[]) (keys.toArray(new String[keys.size()]));
		return vars;
	}
	
	/**
	 * get named var's value
	 * @param name variable name
	 * @return variable value
	 */
	public Object get (String name) {
	
		return binding.getVariable(name);
	}
	
	/**
	 * 执行片段
	 */
	public final static class Express {
		
		private Binding binding;			// store all vars' {name:values}
		private groovy.lang.Script script;	// for Express string
		
		private Express () {
			binding = new Binding();
		}

		/**
		 * get parsed express, ready for eval.
		 * @param express
		 * @return
		 */
		public static Express create(String express) {
			Express o = new Express();
			o.parse(express);
			return o;
		}
		
		private void parse (String express) {			
			GroovyShell shell = new GroovyShell (binding);
			script = shell.parse(express);
		}
		
		/**
		 * set var's (name, value)
		 * @param name
		 * @param value
		 */
		public Express set (String name, Object value) {
			binding.setVariable(name, value);
			return this;
		}
		
		/**
		 * eval groovy script 
		 * @param express bsh script source string
		 * @return last var's value
		 */
		public Object eval () {
			return script.run();
		}
	}
	
	
	//---------------------------- main() -----------------------------//
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			
		CLI cli1 = new CLI ()
			.set("e", "express", true, "[-e express string], 例如：-e \"x=3;y=5;x*5\"")
			.set("f", "file", true, "[-f groovy file], 例如：-f test\\GroovyUtil.groovy")
			.parse(args);
		if (args.length==0) {
			cli1.help("执行groovy文件或片段");
		}
		
		if (cli1.has("express")) {
			GroovyUtil.Express gr = GroovyUtil.Express.create(cli1.get("express"));
			System.out.println (gr.eval());
		}
		
		if (cli1.has("file")) {
			GroovyUtil gr = GroovyUtil.create();
			System.out.println (gr.run(cli1.get("file")));
			
		}
	}

}
