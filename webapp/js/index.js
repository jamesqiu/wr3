/**
 * .js file for index.html 
 * @see ../index.html 
 */
$(function() {
	// ���������û���������
	$("form input").mouseover(function() {
		this.select();
	});
});

function check() {
	alert("no!");
	return false;
}
