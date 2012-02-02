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
 * 各Table通用的属性和方法。
 * 子类改变行为的途径：
 *   {@link #initCodes()}
 *   {@link #initData()}
 *   {@link #processCodes(Row)}
 *   {@link #processData(Table)}
 *   {@link #processTemplate(Template)}
 * 注意：
 *   重载了以上方法的子类，设置的原始数据codes和data可能会被改变，
 *   多次调用 {@link #result()} 前请先调用 {@link #data(Table)} 重置原始数据。  
 * </pre>
 * @author jamesqiu 2009-8-27
 *
 */
public class BaseTable {
	
	/**
	 * 报表id，用于<table id="foo">中。
	 */
	private String id = "001";
	
	/**
	 * 数据
	 */
	private Table data = new Table(); // 可被 initData(), processData() 改变
	
	/**
	 * 原始表头代码
	 */
	private Row codes = new Row();

	/**
	 * 字段描述，没有则使用data的head，下面的2选1；
	 */
	private Map<?, ?> metaMap;
	private List<?> metaList;
	
	/**
	 * 所有数据字典. { "orgid": dd1, -1: dd2, "name": dd3 }
	 * key是表示位置的字段名或者字段index，value是数据字典Map.
	 */
	private ListMap dds = ListMap.create();
	
	/**
	 * 模板名称
	 */
	private String view;
	
	/**
	 * 用户自定义Filter处理每个Cell
	 */
	private CellFilter filter;
	
	/**
	 * 设置报表id，如果不设置则使用“001”，模板上的输出为div$id, table$id。
	 * @param id
	 * @return
	 */
	public BaseTable id(String id) {
		
		if (!Stringx.nullity(id)) this.id = id;
		return this;
	}
	
	/**
	 * 设置table的数据，头变小写。
	 * @pre: 
	 * @post: codes 被设置为table.head()的小写
	 * @param table 代表原始数据的table，不改变
	 * @return
	 */
	public BaseTable data(Table table) {
		
		if (table==null) return this;
		
		this.data = table.copy();  // 使用一份copy，不改变原table
		lowercaseHead(); // head的code转换为小写字符串

		needProcess = true;
		return this;
	}

	/**
	 * 字段描述, {字段名, 字段描述}
	 * 放入 metaMap 的key（字段名）请使用小写字符串，否则不能匹配。
	 * @param metaMap key为table的head名称，value为描述
	 * @return
	 */
	public BaseTable meta(Map<?, ?> metaMap) {
		
		this.metaList = null; // metaList失效，metaMap生效
		this.metaMap = metaMap;
		needProcess = true;
		return this;
	}

	/**
	 * 使用字段描述
	 * @param metaList 包含个字段描述，size须和head的长度一致(否则后面的不处理)。
	 * @return
	 */
	public BaseTable meta(List<?> metaList) {
		
		this.metaMap = null; // metaMap失效，metaList生效
		this.metaList = metaList;
		needProcess = true;
		return this;
	}

	/**
	 * 设置某列的数据字典
	 * @param headCode 表头列代码
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
	 * @param headIndex 表头列位置
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
	 * 使用自定义的temple file
	 * @param templateFilename
	 * @return
	 */
	public BaseTable view(String templateFilename) {
		
		this.view = templateFilename;
		needProcess = true;
		return this;
	}
	
	/**
	 * 使用自定义的Filter去处理原始Table的Head和Cell
	 * @param filter
	 * @return
	 */
	public BaseTable filter(CellFilter filter) {
		
		this.filter = filter;
		needProcess = true;
		return this;
	}
	
	/**
	 * web程序传入的request params
	 */
	private Params params;
	BaseTable params(Params params) {
		this.params = params;
		return this;
	}
	
	/**
	 * 返回原始data的表头。
	 * @return
	 */
	public final Row codes() {
		
		return codes;
	}
	
	/**
	 * 返回经过meta和dds处理后的Table.
	 * @return
	 */
	public Table result() {
		
		processTable();
		
		return data;
	}
	
	/**
	 * 打印codes和table
	 */
	public String toString() {

		processTable();
		
		StringBuilder rt = new StringBuilder();
		rt.append(codes);
		
		if (data!=null) rt.append('\n').append(data);
		
		return rt.toString();
	}

	/**
	 * 处理数据，输出table的html片段，产生一系列ftl变量：
	 *  tableId:   String, <div>及<table>的id属性div$tableId, table$tableId
	 * 其他子类自定义的变量
	 * @return
	 */
	public String html() {
		
		processTable();
		
		Template t = getTemplate();
		
		processTemplateCommon(t);
		// 子类可定制化该方法
		processTemplate(t);	
		
		return t.toString();
	}
	
	/**
	 * 对 Template 进行通用变量设置
	 * @param t
	 */
	private void processTemplateCommon(Template t) {
		
		// 设置tableId变量
		t.set("tableId", id);
		// 设置表头codes
		t.set("codes$"+id, codes);
		// 设置表（表头meta及表rows）
		t.set("table$"+id, data);
		// 设置request params
		if (params==null) return;
		for (Entry<String, String>e : params.toMap().entrySet()) {
			String k = e.getKey();
			String v = e.getValue();
			t.set(k, v);
		}
	}
	
	/*----------------- 如下方法仅子类调用 -----------------*/
	
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
	 * 使用filter、meta、dds处理Table
	 */
	private void processTable() {
		
		if (!needProcess || data==null) return;
		
		// 子类可定制化如下2个方法, 生成数据
		codes = initCodes();
		data = initData();
		
		// 先使用Filter处理每一个Cell
		processFilter();
		// 使用数据字典替换data中的字典代码列。
		processDds();
		// 替换表头meta
		processNames();
		
		// 子类可定制化如下2个方法，最后处理数据
		codes = processCodes(codes);
		data = processData(data);
		
		needProcess = false;
	}
	
	/**
	 * 供子类覆盖此方法，在其他处理之前初始化 codes
	 * @return
	 */
	Row initCodes() {
		
		return codes;
	}
	
	/**
	 * 供子类覆盖此方法，在其他处理之前初始化 data
	 * @return
	 */
	Table initData() {
		
		return data;
	}
	
	/**
	 * 供子类覆盖此方法，对codes进行处理（Table经filter、meta、dds处理后）
	 * @param codes
	 * @return
	 */
	Row processCodes(Row codes) {
	
		return codes;
	}
	
	/**
	 * 供子类覆盖此方法，对使用filter、meta、dds处理后的Table进行处理。
	 * @param data 使用filter、meta、dds处理后的Table
	 * @return 进一步处理后的table
	 */
	Table processData(Table data) {
		
		return data;
		// 子类可对table进行处理
	}
	
	/**
	 * 子类覆盖此方法，对Tempalate进行定制化变量设置
	 * @param t
	 */
	void processTemplate(Template t) { }
	
	/**
	 * 得到template对象.
	 * @return 设置了view则得到指定ftl文件模板，否则得到缺省模板。
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
	 * 得到缺省ftl资源名. 如："wr3/table/BaseTable"
	 * @return
	 */
	private String defaultFtl() {
		
		return Stringx.replaceAll(getClass().getName(), ".", "/") + ".ftl";
	}
	
	private void processFilter() {
		
		if (filter==null || data==null) return;
		
		// 处理head
		for (int j = 0, m = data.head().size(); j < m; j++) {
			Cell cell0 = data.head(j);
			Cell cell1 = filter.process(j, cell0);
			if (cell1==null) cell1 = cell0;
			data.head(j, cell1);
		}
		
		// 处理rows
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
	 * 得到表头的列描述名称，表头列代码code使用meta进行替换.
	 * 注意：必须在 {@link #codes()} 之后调用，否则将丢失表头codes
	 * @return
	 */
	private void processNames() {
		
		if (data==null) return;
		if (metaMap==null && metaList==null) return;
			
		int n = data.cols();
		Row names = new Row(n);
		// 使用metaMap, code对比不区分大小写
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
		// 使用metaList
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
	 * 应用dds进行data的替换
	 */
	private void processDds() {
		
		if (dds.size()==0 || data==null || data.height()==0) return;
		
		for (int i = 0, n = dds.size(); i < n; i++) {
			processDd(dds.key(i), (Map<?,?>)(dds.val(i)));
		}
	}
	
	/**
	 * 进行指定key的dd的替换
	 * @param key
	 * @param val
	 */
	private void processDd(Object key, Map<?,?> dd) {
		
		int colIndex;
		
		// 找到要使用数据字典的列index
		if (key instanceof Integer) {
			colIndex = ((Integer)key).intValue();
		} else if (key instanceof String) {
			colIndex = data.head().asList().indexOf(key);
			if (colIndex == -1) return; // 找不到该列名则返回。
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
	 * 把 {@link #data}的head转换为小写字符串, 并备份一份在codes中。
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
