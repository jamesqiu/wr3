/**
 * .js file for index.html 
 * @see ../index.html 
 */
$(function() {
	// 便于输入用户名和密码
	$("form input").mouseover(function() {
		this.select();
	});
});

function check() {
	alert("no!");
	return false;
}
