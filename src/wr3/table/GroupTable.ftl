<#include "common.ftl">

<#assign codes = .vars["codes$"+tableId]>
<#assign table = .vars["table$"+tableId]>
<#assign n = table.rows?size>
<#function class i> 
	<#-- 设定总计行的样式 -->
	<#if i==0><#return "groupTotal"></#if>
	<#-- 设定分组小计行的样式 -->
	<#if groups?seq_contains(i)><#return "groupSum"></#if>
	<#-- 设定奇偶行不同底色 -->
	<#if i%2==0><#return "even"><#else><#return "odd"></#if>
</#function>

<div id="div$${tableId}">
<table id="table$${tableId}" class="wr3table" border="1">

  <#-- 表头meta描述 -->
  <tr>
    <th>&nbsp;</th>
  <#list table.head as th>
    <th id="${codes[th_index]}" title="${codes[th_index]}">${th}</th>
  </#list> 
  </tr>

  <#-- 表数据 -->
<#if n!=0>
<#assign index=0>
<#list 0..n-1 as i> <#assign classi=class(i)>
  <tr class="${classi}">
    <td><#if classi=="groupTotal" || classi=="groupSum"><#assign index=0>&nbsp;<#else><#assign index=index+1>${index}</#if></td>
  <#list table.rows[i] as td>
    <td <#if td?is_number>class="numberFormat"</#if> >${td!"null"}</td>
  </#list> 
  </tr>
</#list>
</#if>

</table>
</div>
