<#include "common.ftl">

<#assign codes = .vars["codes$"+tableId]>
<#assign table = .vars["table$"+tableId]>
<#assign n = table.rows?size>
<#function class i> <#-- 设定奇偶行不同底色 -->
	<#if i%2==0><#return "even"><#else><#return "odd"></#if>
</#function>

<div id="div$${tableId}">
<table id="cube$${tableId}" class="wr3table" border="1">

  <caption>指标：
  	<#list measureMeta as m>${m}${m_has_next?string(", ","")}</#list>
  </caption>
  ${cubeHtml}

</table>

<br/>
<#--
<table id="table$${tableId}" class="wr3table" border="1">
<#if n!=0>
<#list 0..n-1 as i>
  <tr class="${class(i)}">
    <td>${i+1}</td>
  <#list table.rows[i] as td>
    <td>${td!""}</td>
  </#list> 
  </tr>
</#list>
</#if>
</table>
-->
</div>

<#-- 画corner斜线 -->
<script type="text/javascript">
$(function() {
	var cornerTD = $('.corner')[0];
	var r = cornerTD.getBoundingClientRect(); // get absolute position
	$('.corner').drawPolyline([r.left, r.right-2], [r.top, r.bottom-2], {color: '#999'});	
});
</script>

<#-- 如果在iframe内，调节iframe的高度 -->
<script language="javascript">
if (window.parent.length>0) {
 window.parent.document.all.iframe.style.height=document.body.scrollHeight;
 window.parent.document.all.iframe.style.width=document.body.scrollWidth + 10;
}
</script>
