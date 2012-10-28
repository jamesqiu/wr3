
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
