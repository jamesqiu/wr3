var zero = "0,*";
var rows_orig = "45,*";	
var cols_orig = "200,*";	

function toggle_main() {
	toggle_top();
	toggle_left();
}

function toggle_top() {
	var set_top = parent.document.getElementById("set_top");
	if (set_top.rows!=zero) {
		// ����
		set_top.rows = zero;
	} else {
		// �ָ�
		set_top.rows = rows_orig;
	}
}

function toggle_left() {
	var set_main = parent.document.getElementById("set_main");
	if (set_main.cols!=zero) {
		// ����
		set_main.cols = zero;
	} else {
		// �ָ�
		set_main.cols = cols_orig;
	}
}
