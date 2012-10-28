/**
 * 通过<script src="foo1/foo2/main.js">取得js的目录("foo1/foo2/")
 */
var wr3path = function() {
	var pattern = /main\.js$/i, scripts = document.getElementsByTagName("head")[0].getElementsByTagName("script");
	for ( var i = 0; i < scripts.length; i++) {
		var src = scripts[i].src;
		if (src.search(pattern) >= 0)
			return src.replace(pattern, "");
	}
}();
// alert("wr3path=" + wr3path);

var g_var = {
	css : [ 'main.css',
	// jquery/ui
	'smoothness/jquery-ui-1.8.11.custom.css', 'uniform.default.css',
	// easyui
	'easyui/default/easyui.css', 'easyui/icon.css' ],
	js : [
	// jquery
	'jquery-1.6.3.min.js', 'jquery.draw.js', 'jquery.tablesorter.min.js', 'jquery.uniform.min.js', 'jquery.cookie.js',
	// easyui
	'jquery.easyui.min.js', 'jquery.easyui-lang-zh_CN.js',
	// jquery/ui (注：jqueryui和easyui的dialog方法和dragable方法冲突，放在后面的有效)
	'jquery-ui-1.8.11.custom.min.js', 'jquery.ui.datepicker-zh-CN.js' ]
}

// ==================================================== css
for (i in g_var.css) {
	document.write('<link type="text/css" href="' + wr3path + '../css/' + g_var.css[i] + '" rel="stylesheet" />')
}
//==================================================== js
for (i in g_var.js) {
	document.write('<script type="text/javascript" src="' + wr3path + g_var.js[i] + '"></script>');
}

// document.write('<link type="text/css" href="' + wr3path + '../css/main.css" rel="stylesheet" />');
// // ---------------- css: jquery/ui
// document.write('<link type="text/css" href="' + wr3path + '../css/smoothness/jquery-ui-1.8.11.custom.css" rel="stylesheet" />');
// document.write('<link type="text/css" href="' + wr3path + '../css/uniform.default.css" rel="stylesheet" />');
// // ---------------- css: easyui
// document.write('<link type="text/css" href="' + wr3path + '../css/easyui/default/easyui.css" rel="stylesheet" />');
// document.write('<link type="text/css" href="' + wr3path + '../css/easyui/icon.css" rel="stylesheet" />');
// ==================================================== js
// ---------------- js: jquery
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery-1.6.3.min.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.draw.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.tablesorter.min.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.uniform.min.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.cookie.js"></script>');
// // ---------------- js: easyui
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.easyui.min.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.easyui-lang-zh_CN.js"></script>');
// // ---------------- js: jquery/ui
// // (注：jqueryui和easyui的dialog方法和dragable方法冲突，放在后面的有效)
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery-ui-1.8.11.custom.min.js"></script>');
// document.write('<script type="text/javascript" src="' + wr3path + 'jquery.ui.datepicker-zh-CN.js"></script>');

// -----------------------------------------------------------------------------

/**
 * 公共函数。应用主导航界面onload()的设置，用于: wr3.clj.web/html-apps
 */
function app_onload() {
	/**
	 * 应用主导航界面多个应用div的hover效果设置
	 */
	$("div.app").slideDown("fast").hover(function() {
		$(this).css({
			background : "#9df",
			border : "1px solid blue"
		});
	}, function() {
		$(this).css({
			background : "#6af",
			border : ""
		});
	});
}

/**
 * 公共函数：缺省的eui搜索响应函数。 用于 wr3.clj.app.eui/index 和 wr3.clj.app.ems/layout
 * 中的搜索框event，及其他如：veg.clj、web.clj
 */
function eui_search1(v, n) {
	alert('查询 "' + n + '" 中的 "' + v + '"')
}

/**
 * 对某区域进行ajax内容转载。
 * 
 * @param jq_element
 * @param url
 * @param func
 * @see ajax_post
 */
function ajax_load(jq_element, url, func) {

	return jq_element.html('<img src="/img/loading3.gif" />').load(url, function(data) {
		$.parser.parse(jq_element) // easyui仅重绘ajax装载的内容
		if (func) {
			func(data)
		}
	})
}

/**
 * 公共函数：用于 app/veg 等
 * 
 * @param url
 */
function layout_load_center(url, func) {
	var div1 = $('div [region="center"]')
	// div1.html('<img src="/img/loading3.gif" />')
	// .load(url, function(data) {
	// $.parser.parse(div1) // easyui仅重绘ajax装载的内容
	// if (func) { func(data) }
	// })
	// .css('padding', '20px')
	ajax_load(div1, url, func).css('padding', '20px')
}

/**
 * 公共函数：点击 cross table 报表行头或者列头后绘制图形
 * 
 * @param url0 提交画图的页面
 */
function cross_table_chart(url0) {
	var type_name = $('h1').attr('type')
	$('th[group="dim_top"]').click(function() {
		var col_index = $(this).get(0).cellIndex
		var tb = $(this).parent().parent().get(0)
		var rows = tb.rows.length
		$(tb).find('td').css('background-color', '')
		for ( var i = 1; i < rows; i++) {
			$(tb.rows[i].cells[col_index]).css('background-color', 'yellow')
		}
		var url = encodeURI(url0 + '&dim-top=' + $(this).text() + '&dim-left='); // 有中文和空格，必须encodeURI
		$('#chart').html('<img src="/img/loading3.gif" />').load(url)
	})
	$('th[group="dim_left"]').click(function() {
		var row_index = $(this).parent().get(0).rowIndex
		var tb = $(this).parent().parent().get(0)
		var cols = tb.rows[row_index].cells.length
		$(tb).find('td').css('background-color', '')
		for ( var i = 1; i < cols; i++) {
			$(tb.rows[row_index].cells[i]).css('background-color', 'yellow')
		}
		var url = encodeURI(url0 + '&dim-top=' + '&dim-left=' + $(this).text()); // 有中文和空格，必须encodeURI
		$('#chart').html('<img src="/img/loading3.gif" />').load(url)
	})
}

/**
 * 公共函数：注销一个clj应用，登录后转到本clj应用. 可用于所有clj应用
 * 
 * @param url   本应用的url
 */
function app_exit(url) {
	$.get('/c/auth/logout', function(data) {
		alert(data) // 注销返回信息
		window.location.href = url
	})
}

/**
 * 公共函数：将layout框架右上角的 div#wr3user 替换成登录名
 */
function app_user(app_name) {
	// 显示用户登录信息
	var url = (app_name == 'esp') ? '/c/espreg/who' : '/c/auth/who';
	$.ajaxSetup({
		cache : false
	}) // 这行代码也是专门留给IE这个垃圾的
	$.get(url, function(data) {
		var json = $.parseJSON(data)
		if (json) {
			$('#wr3user').text(json.name)
		} else {
			$('#wr3user').text("未登录")
		}
	})
}

/**
 * 公共函数：改变被点击的 easyui linkbutton 样式
 */
function app_linkbutton_css() {
	var bts = $('a.easyui-linkbutton')
	bts.click(function(e) {
		bts.css("color", "")
		$(this).css("color", "red")
	})
}

// ------------------------ 3个通用函数：文件上传。@see esp.clj/fileupload,
// esp.clj/filesave
/**
 * 公共函数：打开进行文件上传的一个dialog，通过web.clj用于app/esp等应用
 * 
 * @param nam 文件上传字段的中文名
 * @param sid 保存文件上传结果（全文件url）的"hidden"字段的id或name
 */
function fileupload_dlg_open(nam, sid) {
	// 标记hidden字段的属性 uploading="1"，第一次增加该属性，之后改变；一个页面有多个上传按钮时还可标记识别
	$('form input[uploading="1"]').attr('uploading', "0")
	$('form input[name="' + sid + '"]').attr('uploading', '1')
	// 弹出对话框
	var dlg = $('#fileupload')
	var closed = dlg.dialog1('options').closed
	if (!closed) {
		alert('请先处理完已经打开的上传文件对话框！')
	} else {
		dlg.dialog1('open').dialog1('setTitle', '请选择本地' + nam + '文件进行上传：').dialog1('refresh', '/c/pub/fileupload')
	}
}

/**
 * 公共函数：文件选择后上传提交动作或者取消动作
 */
function fileupload_bt() {
	$('#fileupload_ok').click(function() {
		var fname = $('#f_fileupload').val()
		if (fname) {
			$('#fm_fileupload').submit()
		} else {
			alert('未选择文件')
		}
	})
	$('#fileupload_cancel').click(function() {
		if (confirm("确定要取消本次上传？")) {
			$('#fileupload').dialog1('close')
		}
	})
}

/**
 * 公共函数：文件上传完成后调用的函数，显示已上传文件的连接
 * 
 * @param fname
 */
function fileupload_ok(fname) {
	$('#fileupload').dialog1('close')
	$('form input[uploading="1"]').val(fname)
	$('form input[uploading="1"]').next('span').html('<a href="' + fname + '" target="_blank">查看</a> &nbsp; ')
}
// ------------------------ 文件上传</end>

/**
 * 共用函数，取textarea的值，并把\n转换为<br/>
 * 
 * @o 如 textarea字段的id如：'advice'
 * @returns
 */
function textarea_val(id) {
	return $('#' + id).val().replace(/\n/g, '<br/>')
}

/**
 * 共用函数，ajax post一个url并alert结果
 * 
 * @param url
 * @see ajax_load
 */
function ajax_post(url, func) {
	$.post(url, function(data) {
		alert('提示：' + data)
		if (func)
			func()
	})
}

/**
 * 共同函数，提交含大量内容，尤其是有textarea等控件，序列化后的url可能超过2k大小的表单。
 * 
 * @param fm form的jquery表达式如$('#fm1')
 * @param url 提交的action地址
 * @param func 提交后的客户化动作，可选
 */
function ajax_form(fm, url, func) {
	$.post(url, fm.serializeArray(), function(data) {
		alert('提示：' + data)
		if (func)
			func()
	})
}

/**
 * 公共函数。 实现模糊id为"in"的搜索框[:input#in ..]的自动完成初始化。输入字符串超过2个的时候开始自动搜索
 * 
 * @url 自动完成service的url
 * @minLen 输入多少个字符后就开始搜索，无此参数则使用缺省值2
 */
function input_autocomplete(url, minLen) {
	// 输入框hover效果设置
	$("input#in").hover(function() {
		this.select();
	});

	$("input#in").autocomplete({
		source : url,
		select : function(event, ui) {
			$("input#in").val(ui.item.label);
			return false;
		},
		minLength : (minLen || 2)
	});
}
