/**
 * .js file for index.html 
 * @see ../index.html 
 */
$(function() {

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
	$.ajaxSetup({cache:false})
	$.get("/c/auth/who", function(data) {
		var json = $.parseJSON(data)
		var who = (data==null) ?  "尚未登录！" : ("当前用户："+json.uid+"("+json.name+")")
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
	// 要提交验证form的action的地址，缺省为 /c/auth
	var s = "?action="
	var i = url.indexOf(s)
	var action = (i==-1) ? "/c/auth/login?" : url.substring(i+s.length)
	$("form#fm1").attr("action", action)
	$.get(action+$('form#fm1').serialize(), function(data) {
		var json = $.parseJSON(data)
		if (json==null) {
			alert("错误：用户密码错误！")			 
		} else {
			//alert("前往："+data.url)
			if (json.url) {
				window.location.href = json.url
			} else {
				window.location.href = "/login.html"				
			}
		}
	})
	return false;
}
