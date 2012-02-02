var v1 = {head: ["说明", "url"],
	data:[["数据表单", "Test3/form"],
	      ["数据列表", "Test3/list"],
	      ["旋转表", "Test3/rotate"],
	      ["数据聚合表", "Test3/aggre"],
	      ["分组报表", "Test3/group"],
	      ["框架报表", "Test3/frame"],
	      ["交叉表", "Test3/cross"],
	      ["多维表", "Test3/cube"],
	      ["多维表自定义", "DragDrop.html"]]	
};

var v2 = {head: ["说明", "url"],
	data:[["演示", "Chart2"]
		]
};

$(function() {
	/* 测试clone	
	var s = $("#accordion div").html();
	$("#accordion").prepend(s);
	//*/
	
	// 从json对象设置列表内容
	var ul1 = $("#accordion div div ul").eq(0);
	setList(ul1, v1);
	
	var ul2 = $("#accordion div div ul").eq(1);
	setList(ul2, v2);
	
	// Accordion 构建
	$("#accordion").accordion( {
		header : "h3",
		animated: false
	});
});

/**
 * 给 accordion 的＜ｕｌ＞设置连接
 * @param {} o ＜ｕｌ＞对象
 * @param {} v 含说明标签和url的json对象
 */
function setList(o, v) {
	
	o.empty();
	for (i=0;i<v.data.length;i++) {
		var s = "<li><a href='" + v.data[i][1] + "'>" + v.data[i][0] + "</a></li>";
		o.append(s);		
	}
}