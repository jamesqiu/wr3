
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
