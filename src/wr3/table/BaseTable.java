package wr3.table;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.text.Template;
import wr3.util.ListMap;
import wr3.util.Stringx;
import wr3.web.Params;

/**
 * <pre>
 * ��Tableͨ�õ����Ժͷ�����
 * ����ı���Ϊ��;����
 *   {@link #initCodes()}
 *   {@link #initData()}
 *   {@link #processCodes(Row)}
 *   {@link #processData(Table)}
 *   {@link #processTemplate(Template)}
 * ע�⣺
 *   ���������Ϸ��������࣬���õ�ԭʼ����codes��data���ܻᱻ�ı䣬
 *   ��ε��� {@link #result()} ǰ���ȵ��� {@link #data(Table)} ����ԭʼ���ݡ�  
 * </pre>
 * @author jamesqiu 2009-8-27
 *
 */
public class BaseTable {
	
	/**
	 * ����id������<table id="foo">�С�
	 */
	private String id = "001";
	
	/**
	 * ����
	 */
	private Table data = new Table(); // �ɱ� initData(), processData() �ı�
	
	/**
	 * ԭʼ��ͷ����
	 */
	private Row codes = new Row();

	/**
	 * �ֶ�������û����ʹ��data��head�������2ѡ1��
	 */
	private Map<?, ?> metaMap;
	private List<?> metaList;
	
	/**
	 * ���������ֵ�. { "orgid": dd1, -1: dd2, "name": dd3 }
	 * key�Ǳ�ʾλ�õ��ֶ��������ֶ�index��value�������ֵ�Map.
	 */
	private ListMap dds = ListMap.create();
	
	/**
	 * ģ������
	 */
	private String view;
	
	/**
	 * �û��Զ���Filter����ÿ��Cell
	 */
	private CellFilter filter;
	
	/**
	 * ���ñ���id�������������ʹ�á�001����ģ���ϵ����Ϊdiv$id, table$id��
	 * @param id
	 * @return
	 */
	public BaseTable id(String id) {
		
		if (!Stringx.nullity(id)) this.id = id;
		return this;
	}
	
	/**
	 * ����table�����ݣ�ͷ��Сд��
	 * @pre: 
	 * @post: codes ������Ϊtable.head()��Сд
	 * @param table ����ԭʼ���ݵ�table�����ı�
	 * @return
	 */
	public BaseTable data(Table table) {
		
		if (table==null) return this;
		
		this.data = table.copy();  // ʹ��һ��copy�����ı�ԭtable
		lowercaseHead(); // head��codeת��ΪСд�ַ���

		needProcess = true;
		return this;
	}

	/**
	 * �ֶ�����, {�ֶ���, �ֶ�����}
	 * ���� metaMap ��key���ֶ�������ʹ��Сд�ַ�����������ƥ�䡣
	 * @param metaMap keyΪtable��head���ƣ�valueΪ����
	 * @return
	 */
	public BaseTable meta(Map<?, ?> metaMap) {
		
		this.metaList = null; // metaListʧЧ��metaMap��Ч
		this.metaMap = metaMap;
		needProcess = true;
		return this;
	}

	/**
	 * ʹ���ֶ�����
	 * @param metaList �������ֶ�������size���head�ĳ���һ��(�������Ĳ�����)��
	 * @return
	 */
	public BaseTable meta(List<?> metaList) {
		
		this.metaMap = null; // metaMapʧЧ��metaList��Ч
		this.metaList = metaList;
		needProcess = true;
		return this;
	}

	/**
	 * ����ĳ�е������ֵ�
	 * @param headCode ��ͷ�д���
	 * @param dd
	 * @return
	 */
	public BaseTable dd(String headCode, Map<?, ?> dd) {
	
		if (Stringx.nullity(headCode) || dd==null || dd.size()==0) return this;
		
		dds.put(headCode, dd);
		needProcess = true;
		
		return this;
	}

	/**
	 * @see #dd(String, Map)
	 * @param headIndex ��ͷ��λ��
	 * @param dd
	 * @return
	 */
	public BaseTable dd(int headIndex, Map<?, ?> dd) {
		
		if (dd==null || dd.size()==0) return this;
		
		dds.put(headIndex, dd);
		needProcess = true;

		return this;
	}

	/**
	 * ʹ���Զ����temple file
	 * @param templateFilename
	 * @return
	 */
	public BaseTable view(String templateFilename) {
		
		this.view = templateFilename;
		needProcess = true;
		return this;
	}
	
	/**
	 * ʹ���Զ����Filterȥ����ԭʼTable��Head��Cell
	 * @param filter
	 * @return
	 */
	public BaseTable filter(CellFilter filter) {
		
		this.filter = filter;
		needProcess = true;
		return this;
	}
	
	/**
	 * web�������request params
	 */
	private Params params;
	BaseTable params(Params params) {
		this.params = params;
		return this;
	}
	
	/**
	 * ����ԭʼdata�ı�ͷ��
	 * @return
	 */
	public final Row codes() {
		
		return codes;
	}
	
	/**
	 * ���ؾ���meta��dds������Table.
	 * @return
	 */
	public Table result() {
		
		processTable();
		
		return data;
	}
	
	/**
	 * ��ӡcodes��table
	 */
	public String toString() {

		processTable();
		
		StringBuilder rt = new StringBuilder();
		rt.append(codes);
		
		if (data!=null) rt.append('\n').append(data);
		
		return rt.toString();
	}

	/**
	 * �������ݣ����table��htmlƬ�Σ�����һϵ��ftl������
	 *  tableId:   String, <div>��<table>��id����div$tableId, table$tableId
	 * ���������Զ���ı���
	 * @return
	 */
	public String html() {
		
		processTable();
		
		Template t = getTemplate();
		
		processTemplateCommon(t);
		// ����ɶ��ƻ��÷���
		processTemplate(t);	
		
		return t.toString();
	}
	
	/**
	 * �� Template ����ͨ�ñ�������
	 * @param t
	 */
	private void processTemplateCommon(Template t) {
		
		// ����tableId����
		t.set("tableId", id);
		// ���ñ�ͷcodes
		t.set("codes$"+id, codes);
		// ���ñ���ͷmeta����rows��
		t.set("table$"+id, data);
		// ����request params
		if (params==null) return;
		for (Entry<String, String>e : params.toMap().entrySet()) {
			String k = e.getKey();
			String v = e.getValue();
			t.set(k, v);
		}
	}
	
	/*----------------- ���·������������ -----------------*/
	
	/*
	private String id() {
		
		return id;
	}
	
	Table data() {
		return data;
	}
	
	Map<?, ?> meta() {
		return metaMap;
	}
	
	List<?> metaList() {
		return metaList;
	}
	
	String view() {
		return view;
	}
	
	CellFilter filter() {
		return filter;
	}
	//*/

	private boolean needProcess = true;
	
	void needProcess() { needProcess = true; }
	
	/**
	 * ʹ��filter��meta��dds����Table
	 */
	private void processTable() {
		
		if (!needProcess || data==null) return;
		
		// ����ɶ��ƻ�����2������, ��������
		codes = initCodes();
		data = initData();
		
		// ��ʹ��Filter����ÿһ��Cell
		processFilter();
		// ʹ�������ֵ��滻data�е��ֵ�����С�
		processDds();
		// �滻��ͷmeta
		processNames();
		
		// ����ɶ��ƻ�����2�����������������
		codes = processCodes(codes);
		data = processData(data);
		
		needProcess = false;
	}
	
	/**
	 * �����า�Ǵ˷���������������֮ǰ��ʼ�� codes
	 * @return
	 */
	Row initCodes() {
		
		return codes;
	}
	
	/**
	 * �����า�Ǵ˷���������������֮ǰ��ʼ�� data
	 * @return
	 */
	Table initData() {
		
		return data;
	}
	
	/**
	 * �����า�Ǵ˷�������codes���д���Table��filter��meta��dds�����
	 * @param codes
	 * @return
	 */
	Row processCodes(Row codes) {
	
		return codes;
	}
	
	/**
	 * �����า�Ǵ˷�������ʹ��filter��meta��dds������Table���д���
	 * @param data ʹ��filter��meta��dds������Table
	 * @return ��һ��������table
	 */
	Table processData(Table data) {
		
		return data;
		// ����ɶ�table���д���
	}
	
	/**
	 * ���า�Ǵ˷�������Tempalate���ж��ƻ���������
	 * @param t
	 */
	void processTemplate(Template t) { }
	
	/**
	 * �õ�template����.
	 * @return ������view��õ�ָ��ftl�ļ�ģ�壬����õ�ȱʡģ�塣
	 */
	private Template getTemplate() {
		
		Template t;
		if (view==null) {
			t = Template.create(getClass(), defaultFtl());
		} else {
			t = Template.create(view);
		}	
		
		return t;
	}
	
	/**
	 * called by {@link #getTemplate()}
	 * �õ�ȱʡftl��Դ��. �磺"wr3/table/BaseTable"
	 * @return
	 */
	private String defaultFtl() {
		
		return Stringx.replaceAll(getClass().getName(), ".", "/") + ".ftl";
	}
	
	private void processFilter() {
		
		if (filter==null || data==null) return;
		
		// ����head
		for (int j = 0, m = data.head().size(); j < m; j++) {
			Cell cell0 = data.head(j);
			Cell cell1 = filter.process(j, cell0);
			if (cell1==null) cell1 = cell0;
			data.head(j, cell1);
		}
		
		// ����rows
		for (int i = 0, n = data.rows(); i < n; i++) {
			for (int j = 0, m = data.cols(); j < m; j++) {
				Cell cell0 = data.cell(i, j);
				Cell cell1 = filter.process(i, j, cell0);
				if (cell1==null) cell1 = cell0;
				data.cell(i, j, cell1);
			}
		}
	}

	/**
	 * �õ���ͷ�����������ƣ���ͷ�д���codeʹ��meta�����滻.
	 * ע�⣺������ {@link #codes()} ֮����ã����򽫶�ʧ��ͷcodes
	 * @return
	 */
	private void processNames() {
		
		if (data==null) return;
		if (metaMap==null && metaList==null) return;
			
		int n = data.cols();
		Row names = new Row(n);
		// ʹ��metaMap, code�ԱȲ����ִ�Сд
		if (metaMap != null) {
			for (int i = 0; i < n; i++) {
				String code = data.head(i).value();
				String name = code;
				if (metaMap.containsKey(code)) {
					name = "" + metaMap.get(code);
				}
				names.cell(i, Cell.create(name));
			}
		}
		// ʹ��metaList
		if (metaList != null) {
			int size = metaList.size();
			for (int i = 0; i < n; i++) {
				String name; 
				if (i < size) {
					name = "" + metaList.get(i);
				} else {
					name = data.head(i).value();
				}
				names.cell(i, Cell.create(name));
			}
		}
		
		data.head(names);
	}
	
	/**
	 * Ӧ��dds����data���滻
	 */
	private void processDds() {
		
		if (dds.size()==0 || data==null || data.height()==0) return;
		
		for (int i = 0, n = dds.size(); i < n; i++) {
			processDd(dds.key(i), (Map<?,?>)(dds.val(i)));
		}
	}
	
	/**
	 * ����ָ��key��dd���滻
	 * @param key
	 * @param val
	 */
	private void processDd(Object key, Map<?,?> dd) {
		
		int colIndex;
		
		// �ҵ�Ҫʹ�������ֵ����index
		if (key instanceof Integer) {
			colIndex = ((Integer)key).intValue();
		} else if (key instanceof String) {
			colIndex = data.head().asList().indexOf(key);
			if (colIndex == -1) return; // �Ҳ����������򷵻ء�
		} else {
			System.err.println("ddApply(key, dd): key is not int/String");
			return;
		}
		
		for (int i = 0, n = data.height(); i < n; i++) {
			Object v0 = data.cell(i, colIndex).data();
			if (dd.containsKey(v0)) {
				Object o = dd.get(v0);
				data.cell(i, colIndex, Cell.createByObject(o));
			}
		}
	}
	
	/**
	 * called by {@link #data(Table)}
	 * �� {@link #data}��headת��ΪСд�ַ���, ������һ����codes�С�
	 * @pre: code is empty
	 * @post: data.head() changed, code changed.
	 */
	private void lowercaseHead() {
		
		if (data==null) return;
		
		Row head = data.head();
		for (int i = 0, n = head.length(); i < n; i++) {
			String s0 = head.cell(i).value().toLowerCase();
			data.head(i, Cell.create(s0));
		}
		codes = data.head().copy();
	}
	
}
