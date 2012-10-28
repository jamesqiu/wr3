// cn中文
//----------------------------------------------- wr3.clj.tool
/**
 * 全部身份证应用 onload()的设置，用于：wr3.clj.tool/pid
 */
function pid_onload() {
	// input_hover(); select_date(); select_gender(); gen_pid();

	/**
	 * 输入框hover效果设置，用于: wr3.clj.tool/pid
	 */
	$("form.app input[type='text']").hover(function() {
		this.select();
	}, function() {
// this.blur();
	});

	/**
	 * 使区号输入框具备auto-complete功能
	 */
	$("input#areacode").autocomplete({
		source: "auto",
		select: function (event, ui) {
			$("input#areacode").val(ui.item.label.substring(0,6));
			return false;
		},
		minLength: 3
	});

	/**
	 * 初始化jquery-ui日期选择控件。用于：wr3.clj.tool/pid
	 */
	$('#datepicker').datepicker({
		dateFormat:'yymmdd', changeMonth:true, changeYear:true, yearRange:'1900:2011'});

	/**
	 * 进行性别选择时的效果：显示对应的sid序号选择下拉列表。用于：wr3.clj.tool/pid
	 */
	$("#male").change(function() {
		$("#sid2").hide();
		$("#sid1").show();
	});
	$("#female").change(function() {
		$("#sid1").hide();
		$("#sid2").show();
	});

	$("div.result").dialog({
		autoOpen: false,
		width: 525,
		height: 420,
		resizable: false,
		title: "模拟身份证显示"
	});

	/**
	 * 点击提交校验身份证的动作：用dialog显示18位身份证及其解析，用于：wr3.clj.tool/pid
	 */
	$("#check_pid").click(function() {
		$('div.result').load('./pid2/' + $.trim($('#pid').val())).dialog("open");
	});

	$("form.app").submit(function() {
		$("#check_pid").click();
		return false;
	});

	/**
	 * 点击提交生成身份证的动作：用dialog显示18位身份证及其解析，用于：wr3.clj.tool/pid
	 */
	$( "#gen_pid" ).button().click(function() {
		var areacode = $("#areacode").val();
		var datepicker = $("#datepicker").val();
		var sid = $("#male").get(0).checked ? $("#sid1").val() : $("#sid2").val();
		var s17 = (areacode + datepicker + sid);
		$('div.result').load('./gen-pid/' + s17).dialog("open");
	});
}

/**
 * 在中国地图上选中一个省份时的效果：显示相应的区县列表，用于：wr3.clj.tool/html-area 内部函数
 * 
 * @param code
 * @param name
 * @return
 */
function select_area(code, name) {
	$("h2#area1").text(name);
	$("div#area2").load("./area2/" + code);
}

/**
 * 选中一个区县时的效果：在对应input@text上显示区县代码，用于：wr3.clj.tool/f 内部函数
 * 
 * @param code
 * @param name
 * @return
 */
function select_area2(code, name) {
	$("#area1").text(name);
	$("#areacode").val(code);
}

/**
 * 测试jquery-ui dialog, 用于 wr3.clj.tool/dialog
 * 
 * @return
 */
function dialog() {
	$("#dialog").dialog({width:550, resizable:false});
}

/**
 * 输入区号、地名、简拼3个及以上时自动完成，用于 wr3.clj.tool/complete
 * 
 * @return
 */
function area_complete() {
	$("input#complete").autocomplete({
// source: [{value:"nasoft1", label:"nasoft"},
// {value: 001, label: "北京紫光北美科技开发有限公司"},
// {label: "北京汇金科技股份有限公司", value: 002},
// {value:"nasoft2", label:"Nasoft"}],

// source: function(request, response) {
// response([request.term + "-aaa", request.term+"-bbb"]);
// },
		source: "auto",
		select: function (event, ui) {
			$("input#complete").val(ui.item.label.substring(0,6));
			return false;
		},
		minLength: 3
	});
}

/**
 * dict, gname, pinyin, tax, money 等input-submit-app类型应用共同的处理函数, 用于
 * wr3.clj.tool/..
 * 
 * @param id
 * @return
 */
function isapp_onload(id) {
	$("input#ok").click(function() {
		var v = $("input#in").val();
		$("div#rt").html("<img src='"+wr3path+"../img/loading.gif'/>").load("./" + id + "2/" + v);
	});
	$("form.app").submit(function(){
		$("input#ok").click();
		return false; // 必须的
	});
}
