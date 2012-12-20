//------------------------------------------ wr3.clj.mdb
/**
 * 用于：wr3.clj.mdb/index
 * 
 * @return
 */
function mdb_onload() {

	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
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
