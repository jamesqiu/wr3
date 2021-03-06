var SGD_SM1_ECB = 0x00000101;
var SGD_SM1_CBC = 0x00000102;
var SGD_SSF33_ECB = 0x00000201;
var SGD_SSF33_CBC = 0x00000202;
var SGD_SMS4_ECB = 0x00000401;
var SGD_SMS4_CBC = 0x00000402;
var SGD_3DES_ECB = 0x00002001;
var SGD_3DES_CBC = 0x00002002;
var SGD_SM3_RSA = 0x00010001;
var SGD_SHA1_RSA = 0x00010002;
var SGD_SHA256_RSA = 0x00010004;
var SGD_SM3_SM2 = 0x00020101;
var SGD_SM3 = 0x00000001;
var SGD_SHA1 = 0x00000002;
var SGD_SHA256 = 0x00000004;
var KEY_SIGNOREXCHANGE = 0;
var KEY_SIGNATURE = 1;
var KEY_KEYEXCHANGE = 2;
var KEY_TYPE_RSA1024 = 1;
var KEY_TYPE_RSA2048 = 2;
var KEY_TYPE_SM2_256 = 3;
var DEVICE_TYPE_LABEL = 1;
var DEVICE_TYPE_FREESPACE = 2;
var DEVICE_TYPE_SERIALNUM = 3;
var DEVICE_TYPE_TYPE = 4;
var DEVICE_TYPE_KEY_TYPE = 115;
var DEVICE_TYPE_VID_PID = 116;
var CERT_VERSION = 1;
var CERT_SERIAL = 2;
var CERT_SIGN_METHOD = 3;
var CERT_ISSUER_C = 4;
var CERT_ISSUER_O = 5;
var CERT_ISSUER_OU = 6;
var CERT_ISSUER_ST = 7;
var CERT_ISSUER_CN = 8;
var CERT_ISSUER_L = 9;
var CERT_ISSUER_E = 10;
var CERT_NOT_BEFORE = 11;
var CERT_NOT_AFTER = 12;
var CERT_SUBJECT_C = 13;
var CERT_SUBJECT_O = 14;
var CERT_SUBJECT_OU = 15;
var CERT_SUBJECT_ST = 16;
var CERT_SUBJECT_CN = 17;
var CERT_SUBJECT_L = 18;
var CERT_SUBJECT_E = 19;
var CERT_PUBKEY = 20;
var CERT_SUBJECT_DN = 33;
var CERT_ISSUER_DN = 34;
var CERT_UNIQUEID = 35;
try {
	if (window.ActiveXObject) {
		document
				.writeln("<OBJECT classid=\"CLSID:3F367B74-92D9-4C5E-AB93-234F8A91D5E6\" height=1 id=XTXAPP style=\"HEIGHT: 1px; LEFT: 10px; TOP: 28px; WIDTH: 1px\" width=1 VIEWASTEXT>");
		document.writeln("</OBJECT>");
		XTXAPP.SOF_GetVersion();
	} else {
		document
				.writeln("<embed id=XTXAPP0 type=application/x-xtx-axhost clsid={3F367B74-92D9-4C5E-AB93-234F8A91D5E6} event_OnUsbkeyChange=OnUsbKeyChange width=1 height=1 />");
		XTXAPP = document.getElementById("XTXAPP0");
		XTXAPP.SOF_GetVersion();
	}
} catch (e) {
	alert("请检查证书应用环境是否正确安装！\n请在首页\"相关资源链接\"中下载证书应用环境。");
}
function GetUserList(strListID) {
	var objListID = eval(strListID);
	var strUserList = SOF_GetUserList();
	while (1) {
		var i = strUserList.indexOf("&&&");
		if (i <= 0) {
			break;
		}
		var strOneUser = strUserList.substring(0, i);
		var strName = strOneUser.substring(0, strOneUser.indexOf("||"));
		var strUniqueID = strOneUser.substring(strOneUser.indexOf("||") + 2,
				strOneUser.length);
		var objItem = new Option(strName, strUniqueID);
		objListID.options.add(objItem);
		var len = strUserList.length;
		strUserList = strUserList.substring(i + 3, len);
	}
	var objListID = null;
	return;
}
function RemoveUserList(strListID) {
	var objListID = eval(strListID);
	var i;
	var n = objListID.length;
	for (i = 0; i < n; i++) {
		objListID.remove(0);
	}
}
function ChangeUserList(strListID) {
	RemoveUserList(strListID);
	GetUserList(strListID);
}
function OnUsbKeyChange() {
	ChangeUserList("LoginForm.UserList");
}
function SOF_SetEncryptMethod(encMethod) {
	return XTXAPP.SOF_SetEncryptMethod(encMethod);
}
function SOF_SetSignMethod(signMethod) {
	return XTXAPP.SOF_SetSignMethod(signMethod);
}
function SOF_GetUserList() {
	return XTXAPP.SOF_GetUserList();
}
function SOF_ExportUserCert(CertID, certType) {
	if (certType == KEY_SIGNOREXCHANGE) {
		return XTXAPP.SOF_ExportUserCert(CertID);
	} else if (certType == KEY_SIGNATURE) {
		var signCert = XTXAPP.SOF_ExportUserCert(CertID);
		var encCert = XTXAPP.SOF_ExportExChangeUserCert(CertID);
		if (signCert != encCert) {
			return signCert;
		} else { /* 不存在签名证书 */
			return "";
		}
	} else if (certType == KEY_KEYEXCHANGE) {
		return XTXAPP.SOF_ExportExChangeUserCert(CertID);
	} else {
		return "";
	}
}
function SOF_Login(CertID, passwd) {
	var ret = XTXAPP.SOF_Login(CertID, passwd);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_GetPinRetryCount(CertID) {
	return XTXAPP.SOF_GetPinRetryCount(CertID);
}
function SOF_ChangePassWd(CertID, OldPassWd, NewPassWd) {
	var ret = XTXAPP.SOF_ChangePassWd(CertID, OldPassWd, NewPassWd);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function XTXAPP_GetCertDetail(sCert, type) {
	if (type == CERT_UNIQUEID) {
		var ret = XTXAPP.SOF_GetCertInfoByOid(sCert, "1.2.86.11.7.1.8");
		if (ret != "") {
			return ret;
		} else {
			ret = XTXAPP.SOF_GetCertInfoByOid(sCert, "2.16.840.1.113732.2");
			if (ret != "") {
				return ret;
			} else {
				return XTXAPP.SOF_GetCertInfoByOid(sCert,
						"1.2.156.112562.2.1.1.1");
			}
		}
	} else {
		return XTXAPP.SOF_GetCertInfo(sCert, type);
	}
}
function SOF_SignData(CertID, inData) {
	return XTXAPP.SOF_SignData(CertID, inData);
}
function SOF_VerifySignedData(sCert, inData, signValue) {
	var ret = XTXAPP.SOF_VerifySignedData(sCert, inData, signValue);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_SignFile(CertID, inFile) {
	return XTXAPP.SOF_SignFile(CertID, inFile);
}
function SOF_VerifySignedFile(sCert, InFile, SignValue) {
	var ret = XTXAPP.SOF_VerifySignedFile(sCert, InFile, SignValue);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_EncryptData(sCert, Indata) {
	return XTXAPP.SOF_EncryptData(sCert, Indata);
}
function SOF_EncryptDataEx(sCert, Indata) {
	return XTXAPP.SOF_EncryptDataEx(sCert, Indata);
}
function SOF_DecryptData(CertID, Indata) {
	return XTXAPP.SOF_DecryptData(CertID, Indata);
}
function SOF_SignMessage(dwFlag, CertID, InData) {
	return XTXAPP.SOF_SignMessage(dwFlag, CertID, InData);
}
function SOF_VerifySignedMessage(MessageData, InData) {
	var ret = XTXAPP.SOF_VerifySignedMessage(MessageData, InData);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_SymEncryptData(sKey, InData) {
	return XTXAPP.SOF_SymEncryptData(sKey, InData);
}
function SOF_SymDecryptData(sKey, InData) {
	return XTXAPP.SOF_SymDecryptData(sKey, InData);
}
function SOF_SymEncryptFile(sKey, InFile, OutFile) {
	var ret = XTXAPP.SOF_SymEncryptFile(sKey, InFile, OutFile);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_SymDecryptFile(sKey, InFile, OutFile) {
	var ret = XTXAPP.SOF_SymDecryptFile(sKey, InFile, OutFile);
	if (ret) {
		return true;
	} else {
		return false;
	}
}
function SOF_PubKeyEncrypt(Cert, InData) {
	return XTXAPP.SOF_PubKeyEncrypt(Cert, InData);
}
function SOF_PriKeyDecrypt(CertID, InData) {
	return XTXAPP.SOF_PriKeyDecrypt(CertID, InData);
}
function SOF_HashFile(hashAlg, InFile) {
	return XTXAPP.SOF_HashFile(hashAlg, InFile);
}
function SOF_GetLastError() {
	return XTXAPP.SOF_GetLastError();
}
function SOF_GetLastErrorMsg() {
	var code = XTXAPP.SOF_GetLastError();
	var msg = XTXAPP.SOF_GetLastErrMsg();
	return "错误码[" + code + "] 错误描述[" + msg + "]";
}
function CheckValid(userCert) {
	var strNotBefore = XTXAPP_GetCertDetail(userCert, CERT_NOT_BEFORE);
	var strNotBefore_year = strNotBefore.substring(0, 4);
	var strNotBefore_month = strNotBefore.substring(4, 6);
	var strNotBefore_day = strNotBefore.substring(6, 8);
	var notBeforeDate = strNotBefore_year + "/" + strNotBefore_month + "/"
			+ strNotBefore_day;
	var nowDate = new Date().Format("yyyy/MM/dd");
	var days = (Date.parse(notBeforeDate) - Date.parse(nowDate))
			/ (1000 * 60 * 60 * 24);
	if (days > 0) {
		alert("您的证书尚未生效!距离生效日期还剩" + days + "天!");
		return false;
	}
	var strNotAfter = XTXAPP_GetCertDetail(userCert, CERT_NOT_AFTER);
	var strNotAfter_year = strNotAfter.substring(0, 4);
	var strNotAfter_month = strNotAfter.substring(4, 6);
	var strNotAfter_day = strNotAfter.substring(6, 8);
	var notAfterDate = strNotAfter_year + "/" + strNotAfter_month + "/"
			+ strNotAfter_day;
	var nowDate = new Date().Format("yyyy/MM/dd");
	days = (Date.parse(notAfterDate) - Date.parse(nowDate))
			/ (1000 * 60 * 60 * 24);
	if (days <= -45) {
		alert("您的证书已过期 " + -days + " 天，超过了最后使用期限！\n请到\"北京CA服务直通车\"查看更新手续。");
		return false;
	}
	if (days >= 0 && days <= 60) {
		alert("您的证书还有" + days + "天过期，\n请您尽快到\"北京CA服务直通车\"的查看更新手续。");
		return true;
	}
	if (days < 0) {
		alert("您的证书已过期 " + -days + " 天，\n请您尽快到\"北京CA服务直通车\"的查看更新手续。");
	}
	return true;
}
function Login(strFormName, strCertID, strPin) {
	var objForm = eval(strFormName);
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
	ret = SOF_VerifySignedData(strServerCert, strServerRan, strServerSignedData);
	if (!ret) {
		alert("验证服务器端信息失败!");
		return false;
	}
	var strClientSignedData = SOF_SignData(strCertID, strServerRan);
	if (strClientSignedData == null || strClientSignedData == "") {
		alert("客户端签名失败!");
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
	objForm.UserSignedData.value = strClientSignedData;
	objForm.UserCert.value = userCert;
	objForm.ContainerName.value = strCertID;
	return true;
}