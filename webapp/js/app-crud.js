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
