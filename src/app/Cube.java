package app;

import static wr3.util.Stringx.nullity;
import static wr3.util.Stringx.split;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.GroovyConfig;
import wr3.db.DbServer;
import wr3.table.CubeTable;
import wr3.text.Template;
import wr3.web.Appx;
import wr3.web.Params;
import wr3.web.Render;
import wr3.web.Session;

/**
 * ��Cube.groovy�����ã���request param��layout express������CubeTable
 * @author jamesqiu 2009-11-30
 */
public class Cube {

	public Params params;	// ����������Ϊ"params"
	public Session session;	// ����������Ϊ"session"
	
	// �����ڴ�������ݱ�
	private final static String dbname = "h2";
	private DbServer dbs = DbServer.create(dbname);
	static {
		DataGen.create(dbname);
	}
	
	/**
	 * ���CubeTable���
	 * @return
	 */
	public Render index() {

		String layout = params.get("layout");
		if (nullity(layout)) layout = config().getString("layout");
		
		CubeTable table = CubeTable.create(params)
			.layout(layout)
			.data(data())
			.debug(true)
			.meta(metas())
		;

		return Render.body(table.html());
	}

	// ִ��config�ļ��е�sql��ȡԭʼ���ݡ�
	private wr3.Table data() {
		
		DataGen.create(dbname);
		String sql = config().getString("sql");
		if (nullity(sql)) sql = "select * from deposit";
		return dbs.query(sql);
	}
	
	// ��config�ļ��л�ȡmeta
	private List<String> metas() {
		String ss = config().getString("metas");
		return Arrays.asList(split(ss, ","));
	}
	
	// �õ�config�ļ�
	private GroovyConfig config() {
		
		// // "f:/dev3/classes/app/Cube.groovy";
		String configfile = Appx.filepath(getClass(), ".groovy"); 
		return GroovyConfig.create(configfile);
	}


	/**
	 * todo: �Ƿ��ܹ����򵥣�
	 * Render.params("cn����");
	 * 
	 * �õ���action��Ӧ��ģ�����
	 * @return
	 */
	public Render view() {
	
		Template t = Appx.view(this); // ���ô˷�������params��
		if (t==null) {
			return Render.text("û����ͼģ���ļ�");
		} else {
			return Render.text(t.set("msg", "cn����").toString());			
		}
	}
	
	/**
	 * �õ����ݼ��ֶδ����meta
	 * @return
	 */
	public Render codes() {
		
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("month", "�·�");
		map.put("orgid", "����");
		map.put("name", "����");
		map.put("hq", "���ڶ�");
		map.put("dq", "���ڶ�");
		return Render.json(map); // json�����ܱ�֤˳��
		// for (p in json) println(p + ": " + json[p])
	}
}
