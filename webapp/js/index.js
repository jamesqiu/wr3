/**
 * .js file for index.html 
 * @see ../index.html 
 */
$(function() {
	// ���������û���������
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
