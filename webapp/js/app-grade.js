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

function grade_exit() {
	app_exit('/c/grade/')
}
