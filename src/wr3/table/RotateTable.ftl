<#include "common.ftl">

<#assign codes = .vars["codes$"+tableId]>
<#assign table = .vars["table$"+tableId]>
<#assign n = table.rows?size>
<#function class i> <#-- 设定奇偶行不同底色 -->
	<#if i%2==0><#return "even"><#else><#return "odd"></#if>
</#function>
<#function td0 i> <#-- 设定第1列为表头 -->
	<#if i==0><#return "class=\"td0\""><#else><#return ""></#if>
</#function>

<div id="div$${tableId}">
<table id="table$${tableId}" class="wr3table" border="1">

  <#-- 表数据 --> 
<#if n!=0>
<#list 0..n-1 as i>
  <tr class="${class(i)}">
  <#list table.rows[i] as td>
    <td ${td0(td_index)}>${td!"null"}</td>
  </#list> 
  </tr>
</#list>
</#if>

</table>
</div>
