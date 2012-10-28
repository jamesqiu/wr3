
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
