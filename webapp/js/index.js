/**
 * .js file for index.html 
 * @see ../index.html 
 */
$(function() {
	// 便于输入用户名和密码
	$('input[name="wr3user"]').select()
	$("form input").mouseover(function() {
		this.select();
	});
	$(':submit').mouseover(function() {
		this.style.cursor = 'hand'
	})
	$(':reset').mouseover(function() {
		this.style.cursor = 'hand'
	})
});

function check() {
	alert("no!");
	return false;
}
