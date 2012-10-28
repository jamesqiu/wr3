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
