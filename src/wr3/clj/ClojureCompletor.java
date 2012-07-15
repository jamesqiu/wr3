package wr3.clj;

import java.util.List;

import jline.Completor;
import clojure.lang.MapEntry;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;

/**
 * A JLine Completor for
 */
public class ClojureCompletor implements Completor{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int complete(String buffer, int cursor, List candidates) {
		
		if(buffer == null)
			buffer = "";
		Namespace ns = (Namespace) RT.CURRENT_NS.get();

		String split[] = split(buffer);
		String head = split[0];
		String tail = split[1];
		
		boolean exist = false;
		
		for(Object it: ns.getMappings()){
			MapEntry entry = (MapEntry) it;

			if(entry.getKey() instanceof Symbol){
				Symbol symbol = (Symbol) entry.getKey();
				if(symbol.getName().startsWith(tail)){
					candidates.add(symbol.getName());
					exist = true;
				}
			}

		}
		return exist ? head.length() : -1;
	}
	
	String[] split(String buffer){
		int end = buffer.length() - 1;
		for(; end >= 0; end--){
			char ch = buffer.charAt(end);
			if(" \t,(".indexOf(ch)>0){
				break;
			}
		}
		String result[] = new String[2];
		if(end >=0){
			result[0] = buffer.substring(0, end+1);
			result[1] = buffer.substring(end+1);
		}
		else {
			result[0] = "";
			result[1] = buffer;
		}
		return result;
	}

}
