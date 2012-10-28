
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

