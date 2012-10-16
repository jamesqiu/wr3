/**
 * 通过<script src="foo1/foo2/main.js">取得js的目录("foo1/foo2/").
 */
var wr3path = function() {
	var pattern = /main\.js$/i;
	var scripts = document.getElementsByTagName("head")[0].getElementsByTagName("script");
	for (var i=0;i<scripts.length;i++) {
		var src = scripts[i].src;
		if (src.search(pattern) >=0 ) return src.replace(pattern, "");
	}
}();
// alert("wr3path=" + wr3path);
//==================================================== css
document.write('<link type="text/css" href="' + wr3path + '../css/main.css" rel="stylesheet" />');
// ---------------- css: jquery/ui
document.write('<link type="text/css" href="' + wr3path + '../css/smoothness/jquery-ui-1.8.11.custom.css" rel="stylesheet" />');
document.write('<link type="text/css" href="' + wr3path + '../css/uniform.default.css" rel="stylesheet" />');
// ---------------- css: easyui
document.write('<link type="text/css" href="' + wr3path + '../css/easyui/default/easyui.css" rel="stylesheet" />');
document.write('<link type="text/css" href="' + wr3path + '../css/easyui/icon.css" rel="stylesheet" />');
//==================================================== js
// ---------------- js:  jquery
document.write('<script type="text/javascript" src="' + wr3path + 'jquery-1.6.3.min.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.draw.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.tablesorter.min.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.uniform.min.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.cookie.js"></script>');
// ---------------- js:  easyui
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.easyui.min.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.easyui-lang-zh_CN.js"></script>');
// ---------------- js:  jquery/ui
// (注：jqueryui和easyui的dialog方法和dragable方法冲突，放在后面的有效)
document.write('<script type="text/javascript" src="' + wr3path + 'jquery-ui-1.8.11.custom.min.js"></script>');
document.write('<script type="text/javascript" src="' + wr3path + 'jquery.ui.datepicker-zh-CN.js"></script>');

// -----------------------------------------------------------------------------

/**
 * 公共函数。应用主导航界面onload()的设置，用于: wr3.clj.web/html-apps
 */
function app_onload() {
	/**
	 * 应用主导航界面多个应用div的hover效果设置
	 */
	$("div.app").slideDown("fast").hover(
		function() {
			$(this).css({background:"#9df",border:"1px solid blue"});
		},
		function() {
			$(this).css({background:"#6af",border:""});
		}
	);
}

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

//--------------------------------------------------- wr3.clj.dbm
/**
 * 用于 wr3.clj.dbm/rows
 * 
 * @return
 */
function dbm_onload() {
	$("table#t01").tablesorter();

	// ajax 得到各表记录数
	$("td.tbname").each(function(i){
		var td0 = $(this);
		var td1 = $(this).next();
		var td2 = $(this).next().next();
		var s = td0.text();
		var dbname = $("span#dbname").text();
		var url = wr3path + "../c/dbm/rows/" + dbname + "/" + s;
		$.get(url, function(data) {
			var dd = data.split(" ");
			var d0 = dd[0];
			if (d0=="0") {
				td0.css("color", "gray");
				td1.css("color", "gray");
				td2.css("color", "gray");
			}
			var d1 = dd[1];
			td1.text(dd[0]);
			td2.text(dd[1]);
		});
	});

	// 点击表名，显示列字段信息
	$("td.tbname").click(function() {
		var css0 = {"background-color": "white", "font-weight": "normal"};
		var css1 = {"background-color": "yellow", "font-weight": "bold"};
		$("td.tbname").css(css0);
		$(this).css(css1);
		var dbname = $("span#dbname").text();
		var tbname = $(this).text();
		$("div#cols").html("<img src='/img/loading3.gif' />").load(wr3path+"../c/dbm/cols/"+dbname+"/"+tbname);
	});

}

/**
 * 用于wr3.clj.dbm/quick
 * 
 * @return
 */
function quick_onload() {
	$("img.logo").hover(
		function() {
			$(this).css("border-bottom", "red solid 5px");
		},
		function() {
			$(this).css("border", "none");
		}
	);
	$("img.logo").click(function() {
		var ptype = $(this).attr("alt");
		$("div#in").load(wr3path+"../c/dbm/quickin/"+ptype);
	});
}

/**
 * 用于 wr3.clj.dbm/quickin
 * 
 * @return
 */
function quickin_submit() {
	$("button#submit").click(function() {
		var driver = $("#driver").text();
		var url = $("#url").val();
		var username = $("#username").val();
		var password = $("#password").val();
		var url2 = wr3path+"../c/dbm/quickin_dbname?"+
			"driver="+driver+"&url="+url+"&username="+username+"&password="+password;
		$.get(url2, function(data) {
			// alert(data);
			if (data==1) {
				$("#result").html("连接成功！").css("color","green");
			} else {
				$("#result").html("连接失败！").css("color","red");
			}
		});
	});
}

//------------------------------------------- wr3.clj.cdoc
/**
 * 用于 wr3.clj.cdoc clojure 编程文档
 * 
 * @return
 */
function cdoc_onload() {
	/**
	 * 输入框hover效果设置，用于: wr3.clj.tool/pid
	 */
	$("form.app input[type='text']").hover(function() {
		this.select();
	}, function() {
// this.blur();
	});

	$("input#in").autocomplete({
		source: wr3path+"../c/cdoc/auto",
		select: function (event, ui) {
			$("input#in").val(ui.item.label);
			return false;
		},
		minLength: 2
	});

	$("input#ok").click(function() {
		var v = $("input#in").val();
		$("div#rt").text("等待……").load(wr3path+"../c/cdoc/show/"+v);
	});
	$("form.app").submit(function(){
		$("input#ok").click();
		return false; // 必须的
	});

}

//------------------------------------------ wr3.clj.mdb
/**
 * 用于：wr3.clj.mdb/index
 * 
 * @return
 */
function mdb_onload() {

	$("a.db").click(function() {
		$('a.db').css('color', '')
		$(this).css('color', 'red')
		var dbname = $(this).text();
		$("div#collections").load(wr3path+"../c/mdb/db/"+dbname)
	});
}

/**
 * 用于：wr3.clj.mdb/db
 * 
 * @return
 */
function mdb_data(obj,db,coll) {
	$('a.coll').css('color', '')
	$(obj).css('color', 'red')
	$("div#data").load(wr3path+"../c/mdb/data/"+db+"/"+coll);
}

/**
 * 对mongodb中数据库meta信息的维护 用于：wr3.clj.mdb/dict，
 */
function dict_onload() {

	$("button.create").click(function(){
		var v = $(this).next().val();
		var url = wr3path+"../c/mdb/create/"+v;
		$.get(url, function(data){
			alert("完成增加：\n" + data);
		});
	});

	$("button.update").click(function(){
		var id = $(this).prev().prev().html();
		var v = $(this).next().val();
		var url = wr3path + "../c/mdb/update/" + id + "/" + v;
		$.get(url, function(data) {
			alert("完成更新：\n" + data);
			$(this).next().val(data);
		});
	});

	$("button.delete").click(function(){
		var id = $(this).prev().html();
		var v = $(this).next().next().val();
		var c = confirm ("删除: " + id + "\n\n" + v);
		if (c) {
			var url = wr3path + "../c/mdb/delete/" + id;
			$.get(url, function(data) {
				alert("成功删除：" + data);
			});
		}
	});

}

//---------------------------------------------- wr3.clj.crud
/**
 * 用于：wr3.clj.crud/form
 */
function crud_form_onload() {
    $("input, textarea, select, button").uniform();

	$(":submit").click(function() {
		var d = $("form.wr3form").serializeArray();
		$.ajax({
			type: "POST",
			url: wr3path+"../c/crud/save",
			data: d,
			success: function(msg) { 
				$("span#msg").html(msg).css("background-color", "yellow");
			}
		});
		return false; // 必须的，否则就去提交了
	});
	
	$(":button").click(function() {
		var rt = window.confirm("删除此文档？");
		if (rt==false) return false; 
		var d = $("form.wr3form").serializeArray();
		$.post(wr3path+"../c/crud/delete", d, function(msg) {
			alert(msg);
			window.location.href = wr3path+"../c/crud/view/"+$("input[name='form']").val();
		});
	});
}

/**
 * 用于：wr3.clj.crud/view
 */
function crud_view_onload() {
	$("table.wr3table").tablesorter();
	$("table.wr3table tr").hover(function() {
		$(this).css("background-color", "#ffffcc");
	},	function() {
		$(this).css("background-color", "white");
	});
	$("table.wr3table>tbody>tr").dblclick(function(){
		var v = $(this).children(".view").children("a").attr("href");
		window.location.href = v; 
	});
}

/*
 * 用于：wr3.clj.crud/view
 * 
 */
function crud_design_onload() {

    $("input, textarea, select, button").uniform();
	
	$("ul.sort").sortable({ // 不能拖动到定义了width和height的容器内
		connectWith: 'ul.sort', // 移动范围
		cursorAt: {left: -5},
		cursor: 'auto',
		stop: function(event, ui) { }
	 });
	$("button#add").click(function() {
		var f1 = $("div#add").clone().first().show();
		$("ul.sort").first().append(f1);
	});
	$("input#submit").click(function() {
		var form = $("form#design");
		var lis = $("form#design ul li"); // <li>下是每个元素的3个key(name,label,type)
		// 检查name/label是否为空
		var hasEmpty = false;
		lis.each(function(i) {
			var f = $(this).find(":input");
			if ($.trim(f.eq(0).val())=="" || $.trim(f.eq(1).val())=="" ) {
				hasEmpty = true;
				return false;
			}
		});
		if (hasEmpty) {
			alert("name和label不能为空，请填写，或删除此项。");
			return false;
		}
		// 每个字段的3个key设置name属性
		lis.each(function(i) { 
			var f = $(this).find(":input");
			f.eq(0).attr("name", "label_"+i);
			f.eq(1).attr("name", "name_" +i);
			f.eq(2).attr("name", "type_"+i);
		});
		// 设置字段的数目
		$("form#design input:hidden[name='size']").val(lis.size());
		// Ajax提交保存
		var url = wr3path+"../c/crud/design-save";
		var d = form.serializeArray();
		$.post(url, d, function(msg) { 
			alert("保存结果：" + msg);
		});		
		return false; // 必须的，否则就去提交了
	});
	$("button#clear").click(function () {
		$("ul.sort").last().html("垃圾箱：");
	});
}

//--------------------------------------------- wr3.clj.app.task
/**
 * 用于：wr3.clj.app.task/index
 */
function task_onload() {

	// -------- task 排序
	$(".all").sortable({
		connectWith: ".all",
		update: function(e,ui) { 
			// var t = $(this).find("td").attr("id")
			// task的顺序由变化时进行保存
			var t = $(".all tr")
			var s = ""
			t.each(function(i,e) {
				s += $(e).find("td.tname").attr("id") + " "
			})
			var url = wr3path+"../c/task/task-order-save/"+s
			$.get(url)
		}
	});
	var join = function(coll) { // 字符串连接函数
		var s = ""
		coll.each(function(i,e) { s += $(e).attr("id") + " " })
		return s
	}
	
	// --------- person 排序、移植其他task
	$(".task").sortable({ // 不能拖动到定义了width和height的容器内
		connectWith: '.task', // 移动范围
		cursorAt: {left: -5},
		cursor: 'auto',
		update: function(e, ui) {
			// task中person有变动时进行保存
			var tid = $(this).siblings("td.tname").attr("id") // 任务id
			var tname = $(this).siblings("td.tname").text() // 任务名称
			var tcount = $(this).siblings("td.tcount") // 任务人数
			
			var persons = join($(this).find("div"))
			var url = wr3path+"../c/task/task-save/"
			if (tid!="001") { // 001为可用person，不用保存
				$.post(url, {id: tid, name: tname, persons: persons})
			}
		},
		receive: function(e,ui) {
			var tcount = $(this).siblings("td.tcount")
			tcount.text("" + (parseInt(tcount.text()) + 1))
		},
		remove: function(e,ui) {
			var tcount = $(this).siblings("td.tcount")
			tcount.text("" + (parseInt(tcount.text()) - 1))
		}
	 });
	
	// --------- 双击弹出修改task名称的对话框
	$(".tname").dblclick(function() {
		var o = $(this)
		var id = o.attr("id")
		if (id=="001") return
		
		var v = o.text()
		var url = wr3path+'../c/task/change-task-name/'+id+'/'+v		
		$('div.result').load(url).dialog({
			modal: true,
			buttons: {
				"保存": function() {
					var v2 = $(this).find("input").val()
					$.get(wr3path+'../c/task/task-name-save/'+id+"/"+v2) // 修改后台数据
					o.text(v2) // 修改当前页面显示
					$(this).dialog("close")
				},
				"取消": function() {
					$(this).dialog("close")
				}
			}
			/*
			 * close: function(e, ui) { var v2 = $(this).find("input").val()
			 * $.get(wr3path+'../c/task/task-name-save/'+id+"/"+v2) o.text(v2) }
			 */
		})

	});
	
	$("button#add-task, button#add-person").uniform()

	// --------- 增加任务
	$('button#add-task').click(function() {
		var url = wr3path+'../c/task/change-task-name/001/任务x'
		$('div.result').load(url).dialog({
			modal: true,
			buttons: {
				"保存": function() {
					var v2 = $(this).find("input").val()
					$.get(wr3path+'../c/task/task-save', {name: v2})
					alert("已增加任务，请刷新页面")
					$(this).dialog("close")
				},
				"取消": function() {					
					$(this).dialog("close")
				}
			}
		})
	})
	
	// --------- 增加人员
	$('button#add-person').click(function() {
		var url = wr3path+'../c/task/change-person-name/001/人员x'
		$('div.result').load(url).dialog({
			modal: true,
			buttons: {
				"保存": function() {
					var v2 = $(this).find('input').val()
					$.get(wr3path+'../c/task/person-add/'+v2)
					alert("已增加人员，请刷新页面")
					$(this).dialog("close")
				},
				"取消": function() {
					$(this).dialog("close")
				}
			}
		})
	})
	
}


/**
 * 公共函数：用于 wr3.clj.app.eui/index 和 wr3.clj.app.ems/layout 中的搜索框event
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
	
	return jq_element
		.html('<img src="/img/loading3.gif" />')
	 	.load(url, function(data) {
	  		$.parser.parse(jq_element) // easyui仅重绘ajax装载的内容
	  		if (func) { func(data) }
	  	})
}

/**
 * 用于 wr3.clj.app.veg
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
 * 用于 wr3.clj.app.veg/index
 */
function veg_onload() {
	
	// 先load右边导航条
	var right_region = $('div [region="west"]')
	right_region.load('/c/veg/app-left-main', function() {		
		$.parser.parse()
	})	
	
	var menu2 = $('a.easyui-linkbutton')
	menu2.click(function(e) {
		menu2.css("color", "")
		$(this).css("color", "red")
		var id = $(this).attr("id")
		$('div [region="west"]').load("/c/veg/menu3list/"+id, function(data) {
			$.parser.parse()
			// 一级菜单“基础数据查看”点击内容 --- 开始
			var m = ['11-reg', '12-reg2', '13-trade', '14-variety']
			$.each(m, function(i,v) {
				$('a#'+v+'-count').click(function() {
					layout_load_center('/c/veg/rows/'+$(this).attr('id'))
				})
				$('a#'+v+'-cols').click(function() {
					layout_load_center('/c/veg/cols/'+$(this).attr('id'))
				})
				$('a#'+v+'-data').click(function() {
					layout_load_center('/c/veg/data/'+$(this).attr('id'))
				})
			})
			
			var m2 = {'22-scb-vars':'app1' , '22-scb-price':'app2', '22-scb-quot':'app3', 
			          '31-enter-from':'app4', '31-enter-dict':'app5', '31-enter-time':'app6',
			          '32-enter-dict':'app7', '33-other-top10':'app-top10', '33-other-trend':'app-trend',
			          '33-other-region':'app-region'};
			$.each(m2, function(k, v) {
				$('a#'+k).click(function() {
					layout_load_center('/c/veg/'+v)
				})
			})			
		})
	})		
}

function veg_price(date) { layout_load_center('/c/veg/app2?date='+date) }
function veg_quot(date) { layout_load_center('/c/veg/app3?date='+date) }
function veg_enter_dict(dim) { layout_load_center('/c/veg/app5?dim='+dim) }
function veg_top10(date) { layout_load_center('/c/veg/app-top10/'+date) }
function veg_region(date) { layout_load_center('/c/veg/app-region/'+date) }

function veg_app7() {
	$.get('/app7.js', function() {
		alert('load app7.js ok')
	})
}

/**
 * wr3.clj.app.test/coki
 */
function test_cookie() {
// $.cookie('wr3user', 'user用户1'); // 设置cookie
	alert($.cookie('wr3user'))
}

/**
 * 公共函数：点击 cross table 报表行头或者列头后绘制图形
 * @param url0 提交画图的页面
 */
function cross_table_chart(url0) {
	var type_name = $('h1').attr('type')
	$('th[group="dim_top"]').click(function() {
		var col_index = $(this).get(0).cellIndex
		var tb = $(this).parent().parent().get(0)
		var rows = tb.rows.length
		$(tb).find('td').css('background-color', '')
		for(var i=1; i<rows; i++) {
			$(tb.rows[i].cells[col_index]).css('background-color', 'yellow')
		}
		var url = encodeURI(url0 +'&dim-top='+$(this).text()+'&dim-left='); // 有中文和空格，必须encodeURI
		$('#chart').html('<img src="/img/loading3.gif" />').load(url)
	})
	$('th[group="dim_left"]').click(function() {
		var row_index = $(this).parent().get(0).rowIndex
		var tb = $(this).parent().parent().get(0)
		var cols = tb.rows[row_index].cells.length
		$(tb).find('td').css('background-color', '')
		for(var i=1; i<cols; i++) {
			$(tb.rows[row_index].cells[i]).css('background-color', 'yellow')
		}
		var url = encodeURI(url0 + '&dim-top='+'&dim-left='+$(this).text()); // 有中文和空格，必须encodeURI
		$('#chart').html('<img src="/img/loading3.gif" />').load(url)
	})
}

/**
 * wr3.clj.app.grade/index
 */
function grade_onload() {

// layout_load_center('/c/grade/hs300')
	
	$.ajaxSetup({cache:false})

	var bts = $('div[region="west"] a.easyui-linkbutton')
	bts.click(function() {
		bts.css("color", "")
		$(this).css("color", "red")
	})
	
	$('#hs300_bt').click(function() {
		layout_load_center('/c/grade/hs300/')
	})
	$('#corp_bt').click(function() {
		layout_load_center('/c/grade/corp')
	})	
	
	$('#indic0_bt').click(function() {
		var corp_code = $('#corp').attr('code')
		var year_month =$('span#year').text() + '-' + $('span#month').text() 
		grade_indic(corp_code, year_month)
	})
	$('#year_month_bt').click(function(e) {
		// 显示右键菜单
		$('#year_month_cm').menu1('show',{
			left: e.pageX,
			top: e.pageY
		});
	})
	// 显示站点设计图像文件
	$('#site_bt').click(function() {
		$('div [region="center"]')
		.html('<img src="/img/loading3.gif" />')
		.html('<img src="/img/grade/index-style.jpg" />')
		window.open('/img/grade/index-style.jpg', '_blank')
	})
	// 显示帮助图像文件
	$('#help_bt').click(function() {
		$('div [region="center"]')
		.html('<img src="/img/loading3.gif" />')
		.html('<img src="/img/grade/grade-help.png" style="margin-top: 30px" />')
	})
	// 简单报表
	$('#report1_bt').click(function() {
		layout_load_center('/c/grade/report-score/'+$('#corp').attr('code'))
	})
	$('#report2_bt').click(function() {
		layout_load_center('/c/grade/report-ranks/'+$('#year').text()+'-'+$('#month').text())
	})
	
	// 点击报表行头或者列头后绘制图形
	function dim_click() {
		var type_name = $('h1').attr('type')
		$('th[group="dim_top"]').click(function() {
			var col_index = $(this).get(0).cellIndex
			var tb = $(this).parent().parent().get(0)
			var rows = tb.rows.length
			$(tb).find('td').css('background-color', '')
			for(var i=1; i<rows; i++) {
				$(tb.rows[i].cells[col_index]).css('background-color', 'yellow')
			}
			var url = '/c/grade/report-chart?report-type='+type_name+'&year='
				+$('#year').text()+'&month='+$('#month').text()+'&dim-top='+$(this).text()+'&dim-left=';
			$('#chart').html('<img src="/img/loading3.gif" />').load(url)
		})
		$('th[group="dim_left"]').click(function() {
			var row_index = $(this).parent().get(0).rowIndex
			var tb = $(this).parent().parent().get(0)
			var cols = tb.rows[row_index].cells.length
			$(tb).find('td').css('background-color', '')
			for(var i=1; i<cols; i++) {
				$(tb.rows[row_index].cells[i]).css('background-color', 'yellow')
			}
			var url = '/c/grade/report-chart?report-type='+type_name+'&year='
				+$('#year').text()+'&month='+$('#month').text()+'&dim-top='+'&dim-left='+$(this).text();	
			// 碰到奇怪问题：直接调用load方法页面不绘制chart，
			url = encodeURI(url)
			$('#chart').html('<img src="/img/loading3.gif" />')
			$.get(url, function(data) { $('#chart').html(data) })
		})
	}
	// 分行业统计
	$('#report3_bt').click(function() {
		layout_load_center('/c/grade/report-industry/'+$('#year').text()+'/'+$('#month').text(), 
				dim_click)
	})
	// 分地域（省份）统计
	$('#report4_bt').click(function() {
		layout_load_center('/c/grade/report-province/'+$('#year').text()+'/'+$('#month').text(),
				dim_click)
	})
	// 分板块统计
	$('#report5_bt').click(function() {
		layout_load_center('/c/grade/report-board/'+$('#year').text()+'/'+$('#month').text(),
				dim_click)
	})
	// 显示用户登录信息
	$.get('/c/auth/who', function(data) {
		var json = $.parseJSON(data)
		if (json) {
			$('#wr3user').text(json.name)
		} else {
			$('#wr3user').text("未登录")
		}
	})
}

// 选择某个上市公司后的响应
function grade_corp(code,name) {
	var link = '<a target="_blank" href="http://stockchart.finance.cn.yahoo.com/b?s='+code+'.sz">yahoo 财经</a>';
	$.messager.confirm('请确认', '选择上市公司：'+name+'？<br/>'+link, function(r){
		if (r) {
			$('#corp').attr('code', code)
			$('#corp2').text(name)
		}
	});
}

// 返回综合评价指标表。
function grade_indic(corp_code, year_month) {
	layout_load_center('/c/grade/indic/'+corp_code+'/'+year_month, function() {
		
		$('td[group="score"]').click(function() {
			var td = $(this)
			var score = td.text()
			var title = '修改评价基本指标分值：'
			$.messager.prompt(title, '请输入新分值 [0～100]<br/>当前分值：'+score, function(r){
				var n = parseFloat(r)
				if (r && !isNaN(n) && n>=0 && n<=100) {
					if (n != parseFloat(score)) {
						td.text(r).css('background-color', '#ffffaa').attr('title', score+' --> '+n)
						td.attr('changed', '1')
						$('#score_save').linkbutton('enable')
					}
				}
			})		
		})
		
		$('td[group="advice"]').click(function() { 
			var td = $(this)
			var advice = td.text()
			var title = '修改专家委员会意见：' 
			$.messager.prompt(title, '请输入新意见（空格分隔分值和原因）<br/>当前值：'+advice.replace(/（|）/g,''), function(r){
				if (r && (r!=advice)) {
					td.text(r).css('background-color', '#ffffaa').attr('title', advice+' --> '+r)
					td.attr('changed', '1')
					$('#score_save').linkbutton('enable')
				}
			})		
		})
		
		$('#rank_advice').combobox ( {onSelect: function(r) {
			var corp_code = $('#corp').attr('code')
			var year = $('#year').text()
			var month = $('#month').text()
			var url = '/c/grade/save-rank-advice/'+corp_code+'/'+year+'/'+month+'/'+r.value+'/';
			$.get(url, function(data) {
				$.messager.alert('提示：', '人工评级的更改已保存！', 'info')
			})	
		}}).combobox('setValue', $('#rank_advice0').attr('value'))

	})	
}

function grade_rank_detail() {
	$('#panel1').panel('open')
}

function grade_year_month(year, month) {
	$('span#year').text(year)
	$('span#month').text(month)
}

function grade_score_save() {
	var scores = '';
	$('td[group="score"]').each(function(i,e) {
		if ($(e).attr('changed') == '1') {
			var code = $(this).attr('code')
			var score = $(this).text()
			scores += code+' '+score+' '
		}
	})
	var advices = ' ' // 空格是必须的
	$('td[group="advice"]').each(function(i,e) {
		if ($(e).attr('changed') == '1') {
			var code = $(this).attr('code')
			var advice = $(this).text()
			advices += code+' "'+advice+'" '
		}
	})	
	var corp_code = $('#corp').attr('code')
	var year = $('#year').text()
	var month = $('#month').text()
	var url = '/c/grade/save-score-advice/'+corp_code+'/'+year+'/'+month+'/'+scores+'/'+advices+'/';
	$.get(url, function(data) {
		$.messager.alert('提示：', '评价分值或专家委员会意见的更改已保存！', 'info')
		$('#score_save').linkbutton('disable')
		grade_indic(corp_code, year+"-"+month)
	})	
}

function grade_report_ranks(rank) {
	var year = $('#year').text()
	var month = $('#month').text()
	layout_load_center('/c/grade/corp-of-rank/'+year+'/'+month+'/'+rank)
}

/**
 * 用于 wr3.clj.app.grade/index 中的搜索框event n: 查询范围 v: 查询字符串
 */
function grade_search1(v, n) {
// alert('查询 "' + n + '" 中的 "' + v + '"')
	layout_load_center('/c/grade/corp-like/'+v)
}

/**
 * 注销一个clj应用，登录后转到本clj应用. 可用于所有clj应用
 * 
 * @param url
 *            本应用的url
 */
function app_exit(url) {
	$.get('/c/auth/logout', function(data) {
		alert(data) // 注销返回信息
		window.location.href = url
	})
}

function grade_exit() {
	app_exit('/c/grade/')
}

/**
 * 将 div#wr3user 替换成登录名
 */
function app_user(app_name) {
	// 显示用户登录信息
	var url = (app_name=='esp') ? '/c/espreg/who' : 'c/auth/who';
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
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
 * demo 点击搜索按钮后的动作
 */
function demo_search(v, n) {
	alert('\n搜索关键字：' + v + '\n搜索范围：' + n)
}

/**
 * 点击搜索按钮后的动作
 */
function bank_search(v, n) {
	if (n=='cust') {
		layout_load_center('/c/bank/cust/'+v)		
	} else {
		layout_load_center('/c/bank/mng/'+v)
	}
}

/**
 * 通用函数：改变被点击的 easyui linkbutton 样式
 */
function app_linkbutton_css() {
	var bts = $('a.easyui-linkbutton')
	bts.click(function(e) { 
		bts.css("color", "")
		$(this).css("color", "red")
	})	
}

/**
 * wr3.clj.app.bank
 */
function bank_onload() {
	
	app_linkbutton_css()
	
	var m2 = {'11-cust':'custs' , '12-mng':'mngs', '13-biz':'biz',
			  '21-hq':'olap-hq', '22-dq':'olap-dq', '23-ck':'olap-ck', '24-loan':'olap-loan', '25-time':'olap-time'};
	$.each(m2, function(k, v) {
		$('a#'+k).click(function() {
			layout_load_center('/c/bank/'+v)
		})
	})			
	
	$('a#11-cust').click()	
}

// ------------------------------------ esp: 交通运输企业安全生产标准化系统
/**
 * esp 右上角点击搜索按钮后的动作
 */
function esp_search(v, n) {
	var v = encodeURI(v) // 这行代码是专门对付垃圾IE的
	if (n=="range_pn") {
		layout_load_center('/c/esp/pn-list/'+v)
	} else if (n=="range_en") {
		layout_load_center('/c/esp/en-list/'+v)
	} else if (n=="range_org") {
		layout_load_center('/c/esp/org-list/'+v)
	} else {
		alert('\n搜索关键字：' + v + '\n搜索范围：' + n)
	}
}

function esp_onload() {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	app_linkbutton_css() // 所有link按钮点击后字变红色
// alert($.browser.mozilla) // msie opera safari
}

/**
 * 保存证书申请
 * 
 * @param form
 *            'pn' 'en' 'org'
 */
function esp_input_save(form) {
	var url = '/c/esp/input-save/'+form 
	ajax_form($('form'), url)
}

/**
 * 保存并提交证书申请
 * 
 * @param form
 */
function esp_input_submit(form) {
	var url = '/c/esp/input-submit/'+form 
	ajax_form($('form'), url)
}

function esp_report_save(form) {
	var url = '/c/esp/report-save/'+form 
	ajax_form($("form"), url)
}

// ------------------------ 3个通用函数：文件上传。@see esp.clj/fileupload,
// esp.clj/filesave
/**
 * 打开进行文件上传的一个dialog
 * 
 * @param nam
 *            文件上传字段的中文名
 * @param sid
 *            保存文件上传结果（全文件url）的"hidden"字段的id或name
 */
function fileupload_dlg_open(nam, sid) {
	// 标记hidden字段的属性 uploading="1"，第一次增加该属性，之后改变；一个页面有多个上传按钮时还可标记识别
	$('form input[uploading="1"]').attr('uploading', "0")
	$('form input[name="'+sid+'"]').attr('uploading', '1')
	// 弹出对话框
	var dlg = $('#fileupload')
	var closed = dlg.dialog1('options').closed 
	if (!closed) {
		alert('请先处理完已经打开的上传文件对话框！')
	} else {
		dlg.dialog1('open').dialog1('setTitle', '请选择本地'+nam+'文件进行上传：')
			.dialog1('refresh', '/c/pub/fileupload')
	}
}

/**
 * 文件选择后上传提交动作或者取消动作
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
 * 文件上传完成后调用的函数，显示已上传文件的连接
 * 
 * @param fname
 */
function fileupload_ok(fname) {
	$('#fileupload').dialog1('close')
	$('form input[uploading="1"]').val(fname)
	$('form input[uploading="1"]').next('span').html('<a href="'+ fname +'" target="_blank">查看</a> &nbsp; ')
}
// ------------------------ 文件上传</end>

/**
 * 考评机构、企业变更申请保存
 */
function esp_save_backup(id) {
	$.messager.confirm('确认提交', '提交变更备案？', function(r){
		if (r) {
			var url = '/c/esp/backup-save/'+id
			ajax_form($('#fm1'), url)
		}
	})		
}

/**
 * org聘用、解聘pn时弹出读取pn U盘的窗口，
 */
function esp_org_pn_ukey() {
	var rv = showModalDialog('/c/espreg/pn-ca-read', window, 
			"dialogHeight:400px; dialogWidth:500px; resizable:no; center:yes; help:no; status:no");
	if(typeof(rv) == 'undefined' || rv == 'undefined'){
		alert("请选择考评员证书！");
	} else {
// alert("获得考评员证书的唯一标识：" + rv);
		$('#pid').val(rv.substring(2))
	}
}

/**
 * 初始化证书列表，并隐藏登录org的证书，只显示pn的证书
 */
function esp_pn_ca_onload() {
	esp_bjca_onload()
	$.get('/c/espreg/container-name', function(data) {
		var ContainerName = $.trim(data)
		$('#UserList option').each(function() {
			if ($(this).val()==ContainerName) {
				$(this).remove() // 隐藏登录id
			}
		})
	})	
}

function esp_pn_ca_ok() {
	var uniqueid = XTXAPP.SOF_GetCertInfoByOid(SOF_ExportUserCert($("#UserList").val(),KEY_SIGNOREXCHANGE),"1.2.156.112562.2.1.1.24");
	if (uniqueid.indexOf("SF") > -1){
		window.returnValue = uniqueid;
		window.close();
	} else {
		alert("请从列表中选择考评员证书！");
	}
}

/**
 * 考评机构通过证书号查询要聘用的考评员情况 t 'cid' 'pid'
 */
function esp_org_hire(t) {
	var v = (t=='cid') ? $('#cid').val() : $('#pid').val()
	var msg = (t=='cid') ? '请输入资质证书号如：<br/> 2011-2-0471-07959' : '请通过考评员U盘获取身份证号'
	if (v=="") {
		$.messager.alert('提示', msg, 'warning')								
	} else {
		window.open('/c/esp/org-hire-view/' + v + '?type=' + t, "_blank")
	}
}

/**
 * 考评机构聘用考评员操作提交
 * 
 * @param cid
 */
function esp_hire(cid) {
	var fulltime = $('#fulltime').val()
	$.get('/c/esp/org-hire/'+cid+'?fulltime='+fulltime, function(data) {
		alert(data)
	})
}

/**
 * 考评机构解聘考评员操作提交
 * 
 * @param cid
 */
function esp_fire(cid) {
	$.get('/c/esp/org-fire/'+cid, function(data) {
		alert(data)
	})
}

/**
 * 企业提交选择的考评机构，必须是2个。
 */
function esp_en_select_org() {
	var sids = ""
	var sum = 0
	$('input[type="checkbox"]').each(function(i,e) {
		if ($(e).prop('checked')==true) {
			sids += $(e).attr('sid') + " "
			sum++;			
		}
	})
	if (sum==2) {
		$.get('/c/esp/en-select-org-save/'+sids, function(data) {
			alert(data)
		})
	} else {		
		alert('已选 '+sum+' 个考评机构，请选 2 个。')
	}
}

/**
 * org选择考评员进行企业考评
 */
function esp_org_select_pn(oid) {
	var sids = ""
	var sum = 0
	$('input[type="checkbox"]').each(function(i,e) {
		if ($(e).prop('checked')==true) {
			sids += $(e).attr('sid') + " "
			sum++;			
		}
	})
	if (sum==0) {
		alert('请打勾选择考评员')
	} else {
		ajax_post('/c/esp/org-select-pn-save/'+oid+'?sids='+sids)
	}
	
}

/**
 * 根据逗号分隔的值@sid给所有checkbox中value在其中的打勾。 如：标识出企业已经选择了的2个考评机构；标识org已经选中的多个考评员；……
 * 
 * @param sid
 *            选中的多个uid（orgid、pnids等），如 "pn1,pn-110114200002130012"
 */
function esp_mark_selected(sid) {
	$('input[type="checkbox"]').each(function(i,e) {
		if (sid.indexOf($(e).attr('sid')) != -1) {
			$(e).prop('checked', true)
		}
	})
}

function esp_hot_submit() {
	$.messager.confirm('请确认', '举报信息属实，进行提交？', function(r){
		if (r) $('#fm1').submit();
	});	
}

/**
 * 指标评分计算分值
 */
function esp_get_score() {
	// 校验所申请等级的打星是否全部满足
	var grade = $('#grade').val()
	var cond = 'input[group="score"]'
	switch(grade) {
	case "1": cond += '[star!="0"]'; break;
	case "2": cond += '[star!="0"][star!="1"]';break;
	case "3": cond += '[star="3"]';break;
	default: break;
	}
	var scores_grade = $(cond)
	var err = false
	scores_grade.each(function(i,e) {
		if ($(e).val() != $(e).attr('max')) {
			alert("提示：所申请等级中对应打星号的项必须全部满分。")
			$('#sum').val('0')
			err = true
			return false; // 停止循环
		}
	})	
	if (err) return;
	
	// 计算分数
	var scores = $('input[group="score"]')
	var s = ""
	var err = false
	scores.each(function(i,e) {
		var v = $(e).val()
		if (isNaN(parseInt(v))) {
			err = true
		} else {
			s += v+'+'
		}
	})
	if (err) {
		alert("有未打分项")
		$('#sum').val('0')
	} else {
		var sum = eval(s+"0")
		$('#sum').val(sum)
	}
}

/**
 * 指标评分选择申请级别（1 一级 2 二级 3 三级）
 */
function esp_stand_grade() {
	var tips = ["一级企业必须完全满足所有标<font color=red>★、★★、★★★</font>的项",
                "二级企业必须完全满足所有标<font color=red>★★、★★★</font>的项",
	            "三级企业必须完全满足所有标<font color=red>★★★</font>的项"]
	var grade = $('#grade').val()
	$('#tip').html(tips[grade-1])
}

function esp_stand_save(id) {
	var url = '/c/esp/stand-save/'+id
	ajax_form($('#fm1'), url)
}

/**
 * 共用函数，取textarea的值，并把\n转换为<br/>
 * 
 * @o 如 textarea字段的id如：'advice'
 * @returns
 */
function textarea_val(id) {
	return $('#'+id).val().replace(/\n/g,'<br/>')
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
		if (func) func()
	})
}

/**
 * 共同函数，提交含大量内容，尤其是有textarea等控件，序列化后的url可能超过2k大小的表单。
 * 
 * @param fm
 *            form的jquery表达式如$('#fm1')
 * @param url
 *            提交的action地址
 * @param func
 *            提交后的客户化动作，可选
 */
function ajax_form(fm, url, func) {
	$.post(url, fm.serializeArray(), function(data) {
		alert('提示：' + data)
		if (func) func()
	})
}

/**
 * mot处理企业申请
 * 
 * @oid 文档object-id
 * @yes_or_no 'yes' or 'no'
 */
function esp_mot_en_apply(oid, yes_or_no) {
	var url = '/c/esp/mot-en-apply-resp/'+yes_or_no+'?oid='+oid+'&orgid='+$('#orgid').val()
			+'&advice='+$('#advice').val().replace(/\n/g,'<br/>')
	ajax_post(url)
}

/**
 * mot处理en,org,en申请
 * 
 * @type "pn" "org" "en"
 * @oid 文档object-id
 * @yes_or_no 'yes' or 'no'
 */
function esp_mot_apply(type, oid, yes_or_no) {
	var url = '/c/esp/mot-apply-resp/'+type
		+'?resp=' +yes_or_no
		+'&oid='+oid
		+'&advice='+$('#advice').val().replace(/\n/g,'<br/>')
	if (type=="en") { // 对于en，添加2选1的结果
		url += ('&orgid1='+$('#orgid').val())
	} else if (type=="pn") { // 对于pn，添加直接颁发的字段
		url += ('&pass-direct='+$('#pass-direct').prop('checked'))
		url += '&direct-name=' + $('#direct-name').val()
		url += '&direct-title=' + $('#direct-title').val()
	}
	ajax_post(url)
}

/**
 * mot对en的审核（org评估之后）
 */
function esp_mot_review(y_or_n, oid) {
	var advice = textarea_val('advice')
	var url = '/c/esp/mot-en-review-save/'+oid+'?resp-review='+y_or_n+'&advice-review='+advice
	ajax_post(url)
}

/**
 * org对en的考评
 * 
 * @param y_or_n
 * @param oid
 */
function esp_org_en_apply(y_or_n, oid) {
	var advice = textarea_val('advice')
	var url = '/c/esp/org-en-apply-save/'+oid+'?resp-eval='+y_or_n+'&advice-eval='+advice
	ajax_post(url)
}

/**
 * 保存考评员培训考试信息
 * 
 * @uid 考评员uid
 * @param uid
 */
function esp_pn_train_save(uid) {
	var url = '/c/esp/pn-train-save/' + uid
	ajax_form($('#fm1'), url)
}

/**
 * mot受理org、en变更备案
 */
function esp_backup(tb, oid) {
	var url = '/c/esp/backup-resp-save/'+tb+'/'+oid+'?advice='+textarea_val('advice')
	ajax_post(url)
}

function esp_pager(url) {
	var skip = $('#pagers').val()
	layout_load_center(url+'?skip='+skip)
}

function esp_pn_train_list(url) {
	var skip = $('#pagers').val()
	ajax_load($('#pn_train_list'), (url+'?skip='+skip))
}

function esp_mot_en_search() {
	ajax_load($('#result'), '/c/esp/mot-en-search?'+$('#fm1').serialize())
}

/**
 * en,org,pn 模糊搜索框的自动完成初始化
 * 
 * @typ 'en' 'org' 'pn'
 */
function esp_name_cid_autocomplete(typ) {
	// 输入框hover效果设置
	$("input#in").hover(function() {
		this.select();
	}, function() {
// this.blur();
	});

	$("input#in").autocomplete({
		source: "/c/esp/name-cid-autocomplete/"+typ,
		select: function (event, ui) {
			$("input#in").val(ui.item.label);
			return false;
		},
		minLength: 2
	});
}

/**
 * 通用函数。 实现模糊id为"in"的搜索框[:input#in ..]的自动完成初始化。输入字符串超过2个的时候开始自动搜索
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
		source: url,
		select: function (event, ui) {
			$("input#in").val(ui.item.label);
			return false;
		},
		minLength: (minLen || 2)
	});
}

/**
 * 直接颁发录入确认
 */
function esp_mot_pn_direct() {
	var pass1 = $('#pass-direct').prop('checked')
	var name1 = $('#direct-name').val()
	var title1 = $('#direct-title').val()
	var opener = window.opener
	opener.$('#pass-direct').prop('checked', pass1)
	opener.$('#direct-name').val(name1)
	opener.$('#direct-title').val(title1)
	window.close()
}

/**
 * bjca 登录初始化ukey
 */
function esp_bjca_onload() {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	GetUserList("LoginForm.UserList");
}

/**
 * 响应拔下登录ukey的动作。 插入新ukey不理睬，拔下非登录ukey也不理睬
 */
function esp_bjca_onpull() {
	$.get('/c/espreg/container-name', function(data) {
		var containerName = $.trim(data)
		var strUserList = SOF_GetUserList();
		var index = strUserList.indexOf(containerName) 
// alert('strUserList='+strUserList+';\ncontainer-name='+containerName+';index='+index)
		if (data!="" && (-1 == index)) { // 所有正插入的证书中没有已登录证书
			alert('提示：您已经拔出登录所用证书U盘，当前用户将注销！')
			app_exit('/esp')			
		}
	})
}

/**
 * bjca提交前验证，无需bjca服务器的登录页面。 来源：bjca-XTXAPP.js 的Login函数中去除了和server相关的部分，并设置了
 * UserCert 和 ContainerName
 * 
 * @returns
 */
function esp_bjca_onsubmit_local() {
		
	var objForm = LoginForm;
	var strCertID = LoginForm.UserList.value // ContainerName
	var strPin = LoginForm.UserPwd.value
	if (objForm == null) {
		alert("表单错误");
		return false;
	}
	if (strCertID == null || strCertID == "") {
		alert("获取用户信息失败");
		return false;
	}
	if (strPin == null || strPin == "") {
		alert("请输入证书密码");
		return false;
	}
	if (strPin.length < 6 || strPin.length > 16) {
		alert("密码长度应该在4-16位之间");
		return false;
	}
	var ret = SOF_Login(strCertID, strPin);
	if (!ret) {
		var retryCount = SOF_GetPinRetryCount(strCertID);
		if (retryCount > 0) {
			alert("校验证书密码失败!您还有" + retryCount + "次机会重试!");
			return false;
		} else if (retryCount == 0) {
			alert("您的证书密码已被锁死,请联系\n数字证书办理机构或点击北京CA服务直通车\n进行解锁!");
			return false;
		} else {
			alert("登录失败!");
			return false;
		}
	}
	var userCert = SOF_ExportUserCert(strCertID, KEY_SIGNOREXCHANGE);
	if (userCert == null || userCert == "") {
		alert("导出用户证书失败!");
		return false;
	}
	if (!CheckValid(userCert)) {
		return false;
	}	

	var strSignItem = "<input type=\"hidden\" name=\"UserSignedData\" value=\"\">";
	if (objForm.UserSignedData == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strSignItem);
	}
	var strCertItem = "<input type=\"hidden\" name=\"UserCert\" value=\"\">";
	if (objForm.UserCert == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strCertItem);
	}
	var strContainerItem = "<input type=\"hidden\" name=\"ContainerName\" value=\"\">";
	if (objForm.ContainerName == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strContainerItem);
	}
	objForm.UserCert.value = userCert;
	objForm.ContainerName.value = strCertID;
	
	return true;
}

/**
 * BJCA 提交前验证（登录页面需bjca服务器）
 * 
 * @returns {Boolean}
 */
function esp_bjca_onsubmit_server() {
	var strContainerName = LoginForm.UserList.value;
	var strPin = LoginForm.UserPwd.value;
	LoginForm.strRandom.value = strServerRan // 'NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0'
	var ret = Login("LoginForm", strContainerName, strPin);
	if(ret) {
		LoginForm.UserPwd.value = ""; // 不暴露密码
		return true;
	} else {
		return false;
	}
}

/**
 * 考评员申请表
 */
function espfj_onload(type_vals) {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	$('#type').combobox({multiple:true, width:250,
		onChange: function(v1, v0) {
			if (v1.length>2) {
				$('#typeText').val($('#type').combobox('getText'))
				alert("注意：最多只能选择两种类型！")
				$('#type').combobox('setValues', v0)
			} else {
				if (v1.length<2) $('#typeText').val('')				
			}
		},
		onSelect: function(r) {
			var typeText = $('#typeText').val()
			if (typeText != '') {
				$('#type').combobox('setText', typeText)				
			}
		}
	}).combobox('setValues', type_vals)	
}

function espfj_input_submit_check() {
	if ($.trim($('#name').val())=='') { 
		alert('姓名为必填字段')
		return false
	}
	var pidRegex = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[xX])$/;
	if (!pidRegex.test($('#pid').val())) {
		alert('请填写符合格式的18位身份证号')
		return false
	}
	return true;
/*
 * var ok = true $('[required="true"]').each(function(i) { if
 * ($.trim($(this).val())=='') { alert('有必填字段没有填写！请仔细检查') ok = false return
 * false } }) return ok;
 */	
}

/**
 * 福建考评员表单提交保存
 */
function espfj_input_submit(form) {
	if (espfj_input_submit_check()) {
		var url = '/c/espfj/input-submit/'+form 
		ajax_form($("form"), url, function() {
			window.location.href = '/c/espfj'			
		})
	}
}

function espfj_admin_resp() {
	$('#resp').change(function() {
		if ($(this).val()=="no") {
			$('#pass-direct').prop('checked',false).prop('disabled',true)
		} else {
			$('#pass-direct').prop('disabled',false)			
		}
	})
}

/**
 * 标记记录为删除
 * 
 * @param flag
 *            1表示删除，0表示恢复
 */
function espfj_admin_resp_del(flag) {
	var msg = (flag==1) ? '删除此申请记录，放入垃圾箱？' : '恢复此申请记录？'
	$.messager.confirm('请确认', msg, function(r){
		if (r) {
			var oid = $('#oid').val()
			ajax_post('/c/espfj/admin-resp-del/'+oid+'?flag='+flag)
		}
	});
}

/**
 * 考评员用户名密码登录校验
 * 
 * @param fm
 */
function espfj_input_login(fm) {
	$.post('/c/espfj/input-login-check', fm.serialize(), function(data) {
		var json = $.parseJSON(data)
		if (json==true) {
			window.location.href = '/c/espfj/input/' + $('#pid').val()
		} else {
			alert('登录失败，请联系相关负责人。')
		}
	})
}

/**
 * 
 * @param isPagers
 *            true表示是选不同页码；false表示选其他
 */
function espfj_pn_list_onchange(isPagers) {
	var skip = isPagers ? $('#pagers').val() : 0
	var url = '/c/espfj/pn-apply-list?skip=' + skip
	url += '&del='+ $('#del').val() + '&resp='+$('#resp').val()
	ajax_load($('#list'),  url)
}



