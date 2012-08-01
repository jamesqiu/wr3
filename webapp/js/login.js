/**
 * .js file for login.html 
 * @see ../login.html 
 */
$(function() {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的

	// 如果要验证的是esp应用，并且是ie，跳到UKey验证页面
	$.get("/c/auth/url", function(data) {
		if (data.indexOf('/c/espfj/')==0) {
			window.top.location.href = '/c/espfj/login';
		}
		if ((data.indexOf('/c/esp/')==0) && ($.browser.msie)) {
			window.top.location.href = '/c/espreg/ca';
		}
	})

	// 便于输入用户名和密码
	$('input[name="uid"]').select().change(function() {
		var uid = $.trim($(this).val())
		if (uid=="" || uid=='在此输入用户名') {
			$(this).css('background-color', "yellow")
		} else {
			$(this).css('background-color', "")
		}
	})
	$("form input").mouseover(function() {
		this.select();
	});
	$(':submit').mouseover(function() {
		this.style.cursor = 'hand'
	})
	$(':reset').mouseover(function() {
		this.style.cursor = 'hand'
	})
	// 当前登录信息	
	$.get("/c/auth/who", function(data) {
		var json = $.parseJSON(data)
		var who = (json==null) ?  "尚未登录！" : ("当前用户："+json.uid+"("+json.name+")")
		$('#wr3user').text(who)
	})
});

function check() {
	var uid = $.trim($('input[name="uid"]').val())
	if (uid=="" || uid=='在此输入用户名') {
		$('input[name="uid"]').select().css('background-color', "yellow")
		return false;
	}
	var url = window.location.href;
	// 要提交验证form的action的地址缺省为 /c/auth/login，可通过 ?action=.. 来定制。
	var s = "?action="
	var i = url.indexOf(s)
	var action = (i==-1) ? "/c/auth/login?" : url.substring(i+s.length)
	$("form#fm1").attr("action", action)
	$.get(action+$('form#fm1').serialize(), function(data) {
		var json = $.parseJSON(data)
		if (json==null) {
			alert("错误：用户密码错误！")			 
		} else {
//			alert("前往："+json.url)
			if (json.url) {
				window.location.href = json.url
			} else {
				window.location.href = "/login.html"				
			}
		}
	})
	return false;
}
