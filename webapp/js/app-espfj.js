
/**
 * 考评员申请表
 */
function espfj_onload(type_vals) {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	$('#type').combobox({multiple:true, width:250,
		onChange: function(v1, v0) {
			if (v1.length>2) {
				$('#typeText').val($('#type').combobox('getText'))
				alert("注意：最多只能选择两种类型！")
				$('#type').combobox('setValues', v0)
			} else {
				if (v1.length<2) $('#typeText').val('')				
			}
		},
		onSelect: function(r) {
			var typeText = $('#typeText').val()
			if (typeText != '') {
				$('#type').combobox('setText', typeText)				
			}
		}
	}).combobox('setValues', type_vals)	
}

function espfj_input_submit_check() {
	if ($.trim($('#name').val())=='') { 
		alert('姓名为必填字段')
		return false
	}
	var pidRegex = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[xX])$/;
	if (!pidRegex.test($('#pid').val())) {
		alert('请填写符合格式的18位身份证号')
		return false
	}
	return true;
/*
 * var ok = true $('[required="true"]').each(function(i) { if
 * ($.trim($(this).val())=='') { alert('有必填字段没有填写！请仔细检查') ok = false return
 * false } }) return ok;
 */	
}

/**
 * 福建考评员表单提交保存
 */
function espfj_input_submit(form) {
	if (espfj_input_submit_check()) {
		var url = '/c/espfj/input-submit/'+form 
		ajax_form($("form"), url, function() {
			window.location.href = '/c/espfj'			
		})
	}
}

function espfj_admin_resp() {
	$('#resp').change(function() {
		if ($(this).val()=="no") {
			$('#pass-direct').prop('checked',false).prop('disabled',true)
		} else {
			$('#pass-direct').prop('disabled',false)			
		}
	})
}

/**
 * 标记记录为删除
 * 
 * @param flag
 *            1表示删除，0表示恢复
 */
function espfj_admin_resp_del(flag) {
	var msg = (flag==1) ? '删除此申请记录，放入垃圾箱？' : '恢复此申请记录？'
	$.messager.confirm('请确认', msg, function(r){
		if (r) {
			var oid = $('#oid').val()
			ajax_post('/c/espfj/admin-resp-del/'+oid+'?flag='+flag)
		}
	});
}

/**
 * 考评员用户名密码登录校验
 * 
 * @param fm
 */
function espfj_input_login(fm) {
	$.post('/c/espfj/input-login-check', fm.serialize(), function(data) {
		var json = $.parseJSON(data)
		if (json==true) {
			window.location.href = '/c/espfj/input/' + $('#pid').val()
		} else {
			alert('登录失败，请联系相关负责人。')
		}
	})
}

/**
 * 
 * @param isPagers
 *            true表示是选不同页码；false表示选其他
 */
function espfj_pn_list_onchange(isPagers) {
	var skip = isPagers ? $('#pagers').val() : 0
	var url = '/c/espfj/pn-apply-list?skip=' + skip
	url += '&del='+ $('#del').val() + '&resp='+$('#resp').val()
	ajax_load($('#list'),  url)
}
