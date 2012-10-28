// ------------------------------------ esp: 交通运输企业安全生产标准化系统
/**
 * esp 右上角点击搜索按钮后的动作
 */
function esp_search(v, n) {
	var v = encodeURI(v) // 这行代码是专门对付垃圾IE的
	if (n=="range_pn") {
		layout_load_center('/c/esp/pn-list/'+v)
	} else if (n=="range_en") {
		layout_load_center('/c/esp/en-list/'+v)
	} else if (n=="range_org") {
		layout_load_center('/c/esp/org-list/'+v)
	} else {
		alert('\n搜索关键字：' + v + '\n搜索范围：' + n)
	}
}

function esp_onload() {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	app_linkbutton_css() // 所有link按钮点击后字变红色
// alert($.browser.mozilla) // msie opera safari
}

/**
 * 保存证书申请
 * 
 * @param form
 *            'pn' 'en' 'org'
 */
function esp_input_save(form) {
	var url = '/c/esp/input-save/'+form 
	ajax_form($('form'), url)
}

/**
 * 保存并提交证书申请
 * 
 * @param form
 */
function esp_input_submit(form) {
	var url = '/c/esp/input-submit/'+form 
	ajax_form($('form'), url)
}

function esp_report_save(form) {
	var url = '/c/esp/report-save/'+form 
	ajax_form($("form"), url)
}

/**
 * 考评机构、企业变更申请保存
 */
function esp_save_backup(id) {
	$.messager.confirm('确认提交', '提交变更备案？', function(r){
		if (r) {
			var url = '/c/esp/backup-save/'+id
			ajax_form($('#fm1'), url)
		}
	})		
}

/**
 * org聘用、解聘pn时弹出读取pn U盘的窗口，
 */
function esp_org_pn_ukey() {
	var rv = showModalDialog('/c/espreg/pn-ca-read', window, 
			"dialogHeight:400px; dialogWidth:500px; resizable:no; center:yes; help:no; status:no");
	if(typeof(rv) == 'undefined' || rv == 'undefined'){
		alert("请选择考评员证书！");
	} else {
// alert("获得考评员证书的唯一标识：" + rv);
		$('#pid').val(rv.substring(2))
	}
}

/**
 * 初始化证书列表，并隐藏登录org的证书，只显示pn的证书
 */
function esp_pn_ca_onload() {
	esp_bjca_onload()
	$.get('/c/espreg/container-name', function(data) {
		var ContainerName = $.trim(data)
		$('#UserList option').each(function() {
			if ($(this).val()==ContainerName) {
				$(this).remove() // 隐藏登录id
			}
		})
	})	
}

function esp_pn_ca_ok() {
	var uniqueid = XTXAPP.SOF_GetCertInfoByOid(SOF_ExportUserCert($("#UserList").val(),KEY_SIGNOREXCHANGE),"1.2.156.112562.2.1.1.24");
	if (uniqueid.indexOf("SF") > -1){
		window.returnValue = uniqueid;
		window.close();
	} else {
		alert("请从列表中选择考评员证书！");
	}
}

/**
 * 考评机构通过证书号查询要聘用的考评员情况 t 'cid' 'pid'
 */
function esp_org_hire(t) {
	var v = (t=='cid') ? $('#cid').val() : $('#pid').val()
	var msg = (t=='cid') ? '请输入资质证书号如：<br/> 2011-2-0471-07959' : '请通过考评员U盘获取身份证号'
	if (v=="") {
		$.messager.alert('提示', msg, 'warning')								
	} else {
		window.open('/c/esp/org-hire-view/' + v + '?type=' + t, "_blank")
	}
}

/**
 * 考评机构聘用考评员操作提交
 * 
 * @param cid
 */
function esp_hire(cid) {
	var fulltime = $('#fulltime').val()
	$.get('/c/esp/org-hire/'+cid+'?fulltime='+fulltime, function(data) {
		alert(data)
	})
}

/**
 * 考评机构解聘考评员操作提交
 * 
 * @param cid
 */
function esp_fire(cid) {
	$.get('/c/esp/org-fire/'+cid, function(data) {
		alert(data)
	})
}

/**
 * 企业提交选择的考评机构，必须是2个。
 */
function esp_en_select_org() {
	var sids = ""
	var sum = 0
	$('input[type="checkbox"]').each(function(i,e) {
		if ($(e).prop('checked')==true) {
			sids += $(e).attr('sid') + " "
			sum++;			
		}
	})
	if (sum==2) {
		$.get('/c/esp/en-select-org-save/'+sids, function(data) {
			alert(data)
		})
	} else {		
		alert('已选 '+sum+' 个考评机构，请选 2 个。')
	}
}

/**
 * org选择考评员进行企业考评
 */
function esp_org_select_pn(oid) {
	var sids = ""
	var sum = 0
	$('input[type="checkbox"]').each(function(i,e) {
		if ($(e).prop('checked')==true) {
			sids += $(e).attr('sid') + " "
			sum++;			
		}
	})
	if (sum==0) {
		alert('请打勾选择考评员')
	} else {
		ajax_post('/c/esp/org-select-pn-save/'+oid+'?sids='+sids)
	}
	
}

/**
 * 根据逗号分隔的值@sid给所有checkbox中value在其中的打勾。 如：标识出企业已经选择了的2个考评机构；标识org已经选中的多个考评员；……
 * 
 * @param sid
 *            选中的多个uid（orgid、pnids等），如 "pn1,pn-110114200002130012"
 */
function esp_mark_selected(sid) {
	$('input[type="checkbox"]').each(function(i,e) {
		if (sid.indexOf($(e).attr('sid')) != -1) {
			$(e).prop('checked', true)
		}
	})
}

function esp_hot_submit() {
	$.messager.confirm('请确认', '举报信息属实，进行提交？', function(r){
		if (r) $('#fm1').submit();
	});	
}

/**
 * 指标评分计算分值
 */
function esp_get_score() {
	// 校验所申请等级的打星是否全部满足
	var grade = $('#grade').val()
	var cond = 'input[group="score"]'
	switch(grade) {
	case "1": cond += '[star!="0"]'; break;
	case "2": cond += '[star!="0"][star!="1"]';break;
	case "3": cond += '[star="3"]';break;
	default: break;
	}
	var scores_grade = $(cond)
	var err = false
	scores_grade.each(function(i,e) {
		if ($(e).val() != $(e).attr('max')) {
			alert("提示：所申请等级中对应打星号的项必须全部满分。")
			$('#sum').val('0')
			err = true
			return false; // 停止循环
		}
	})	
	if (err) return;
	
	// 计算分数
	var scores = $('input[group="score"]')
	var s = ""
	var err = false
	scores.each(function(i,e) {
		var v = $(e).val()
		if (isNaN(parseInt(v))) {
			err = true
		} else {
			s += v+'+'
		}
	})
	if (err) {
		alert("有未打分项")
		$('#sum').val('0')
	} else {
		var sum = eval(s+"0")
		$('#sum').val(sum)
	}
}

/**
 * 指标评分选择申请级别（1 一级 2 二级 3 三级）
 */
function esp_stand_grade() {
	var tips = ["一级企业必须完全满足所有标<font color=red>★、★★、★★★</font>的项",
                "二级企业必须完全满足所有标<font color=red>★★、★★★</font>的项",
	            "三级企业必须完全满足所有标<font color=red>★★★</font>的项"]
	var grade = $('#grade').val()
	$('#tip').html(tips[grade-1])
}

function esp_stand_save(id) {
	var url = '/c/esp/stand-save/'+id
	ajax_form($('#fm1'), url)
}

/**
 * mot处理企业申请
 * 
 * @oid 文档object-id
 * @yes_or_no 'yes' or 'no'
 */
function esp_mot_en_apply(oid, yes_or_no) {
	var url = '/c/esp/mot-en-apply-resp/'+yes_or_no+'?oid='+oid+'&orgid='+$('#orgid').val()
			+'&advice='+$('#advice').val().replace(/\n/g,'<br/>')
	ajax_post(url)
}

/**
 * mot处理en,org,en申请
 * 
 * @type "pn" "org" "en"
 * @oid 文档object-id
 * @yes_or_no 'yes' or 'no'
 */
function esp_mot_apply(type, oid, yes_or_no) {
	var url = '/c/esp/mot-apply-resp/'+type
		+'?resp=' +yes_or_no
		+'&oid='+oid
		+'&advice='+$('#advice').val().replace(/\n/g,'<br/>')
	if (type=="en") { // 对于en，添加2选1的结果
		url += ('&orgid1='+$('#orgid').val())
	} else if (type=="pn") { // 对于pn，添加直接颁发的字段
		url += ('&pass-direct='+$('#pass-direct').prop('checked'))
		url += '&direct-name=' + $('#direct-name').val()
		url += '&direct-title=' + $('#direct-title').val()
	}
	ajax_post(url)
}

/**
 * mot对en的审核（org评估之后）
 */
function esp_mot_review(y_or_n, oid) {
	var advice = textarea_val('advice')
	var url = '/c/esp/mot-en-review-save/'+oid+'?resp-review='+y_or_n+'&advice-review='+advice
	ajax_post(url)
}

/**
 * org对en的考评
 * 
 * @param y_or_n
 * @param oid
 */
function esp_org_en_apply(y_or_n, oid) {
	var advice = textarea_val('advice')
	var url = '/c/esp/org-en-apply-save/'+oid+'?resp-eval='+y_or_n+'&advice-eval='+advice
	ajax_post(url)
}

/**
 * 保存考评员培训考试信息
 * 
 * @uid 考评员uid
 * @param uid
 */
function esp_pn_train_save(uid) {
	var url = '/c/esp/pn-train-save/' + uid
	ajax_form($('#fm1'), url)
}

/**
 * mot受理org、en变更备案
 */
function esp_backup(tb, oid) {
	var url = '/c/esp/backup-resp-save/'+tb+'/'+oid+'?advice='+textarea_val('advice')
	ajax_post(url)
}

function esp_pager(url) {
	var skip = $('#pagers').val()
	layout_load_center(url+'?skip='+skip)
}

function esp_pn_train_list(url) {
	var skip = $('#pagers').val()
	ajax_load($('#pn_train_list'), (url+'?skip='+skip))
}

function esp_mot_en_search() {
	ajax_load($('#result'), '/c/esp/mot-en-search?'+$('#fm1').serialize())
}

/**
 * en,org,pn 模糊搜索框的自动完成初始化
 * 
 * @typ 'en' 'org' 'pn'
 */
function esp_name_cid_autocomplete(typ) {
	// 输入框hover效果设置
	$("input#in").hover(function() {
		this.select();
	}, function() {
// this.blur();
	});

	$("input#in").autocomplete({
		source: "/c/esp/name-cid-autocomplete/"+typ,
		select: function (event, ui) {
			$("input#in").val(ui.item.label);
			return false;
		},
		minLength: 2
	});
}

/**
 * 直接颁发录入确认
 */
function esp_mot_pn_direct() {
	var pass1 = $('#pass-direct').prop('checked')
	var name1 = $('#direct-name').val()
	var title1 = $('#direct-title').val()
	var opener = window.opener
	opener.$('#pass-direct').prop('checked', pass1)
	opener.$('#direct-name').val(name1)
	opener.$('#direct-title').val(title1)
	window.close()
}

/**
 * bjca 登录初始化ukey
 */
function esp_bjca_onload() {
	$.ajaxSetup({cache:false}) // 这行代码也是专门留给IE这个垃圾的
	GetUserList("LoginForm.UserList");
}

/**
 * 响应拔下登录ukey的动作。 插入新ukey不理睬，拔下非登录ukey也不理睬
 */
function esp_bjca_onpull() {
	$.get('/c/espreg/container-name', function(data) {
		var containerName = $.trim(data)
		var strUserList = SOF_GetUserList();
		var index = strUserList.indexOf(containerName) 
// alert('strUserList='+strUserList+';\ncontainer-name='+containerName+';index='+index)
		if (data!="" && (-1 == index)) { // 所有正插入的证书中没有已登录证书
			alert('提示：您已经拔出登录所用证书U盘，当前用户将注销！')
			app_exit('/esp')			
		}
	})
}

/**
 * bjca提交前验证，无需bjca服务器的登录页面。 来源：bjca-XTXAPP.js 的Login函数中去除了和server相关的部分，并设置了
 * UserCert 和 ContainerName
 * 
 * @returns
 */
function esp_bjca_onsubmit_local() {
		
	var objForm = LoginForm;
	var strCertID = LoginForm.UserList.value // ContainerName
	var strPin = LoginForm.UserPwd.value
	if (objForm == null) {
		alert("表单错误");
		return false;
	}
	if (strCertID == null || strCertID == "") {
		alert("获取用户信息失败");
		return false;
	}
	if (strPin == null || strPin == "") {
		alert("请输入证书密码");
		return false;
	}
	if (strPin.length < 6 || strPin.length > 16) {
		alert("密码长度应该在4-16位之间");
		return false;
	}
	var ret = SOF_Login(strCertID, strPin);
	if (!ret) {
		var retryCount = SOF_GetPinRetryCount(strCertID);
		if (retryCount > 0) {
			alert("校验证书密码失败!您还有" + retryCount + "次机会重试!");
			return false;
		} else if (retryCount == 0) {
			alert("您的证书密码已被锁死,请联系\n数字证书办理机构或点击北京CA服务直通车\n进行解锁!");
			return false;
		} else {
			alert("登录失败!");
			return false;
		}
	}
	var userCert = SOF_ExportUserCert(strCertID, KEY_SIGNOREXCHANGE);
	if (userCert == null || userCert == "") {
		alert("导出用户证书失败!");
		return false;
	}
	if (!CheckValid(userCert)) {
		return false;
	}	

	var strSignItem = "<input type=\"hidden\" name=\"UserSignedData\" value=\"\">";
	if (objForm.UserSignedData == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strSignItem);
	}
	var strCertItem = "<input type=\"hidden\" name=\"UserCert\" value=\"\">";
	if (objForm.UserCert == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strCertItem);
	}
	var strContainerItem = "<input type=\"hidden\" name=\"ContainerName\" value=\"\">";
	if (objForm.ContainerName == null) {
		objForm.insertAdjacentHTML("BeforeEnd", strContainerItem);
	}
	objForm.UserCert.value = userCert;
	objForm.ContainerName.value = strCertID;
	
	return true;
}

/**
 * BJCA 提交前验证（登录页面需bjca服务器）
 * 
 * @returns {Boolean}
 */
function esp_bjca_onsubmit_server() {
	var strContainerName = LoginForm.UserList.value;
	var strPin = LoginForm.UserPwd.value;
	LoginForm.strRandom.value = strServerRan // 'NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0'
	var ret = Login("LoginForm", strContainerName, strPin);
	if(ret) {
		LoginForm.UserPwd.value = ""; // 不暴露密码
		return true;
	} else {
		return false;
	}
}



