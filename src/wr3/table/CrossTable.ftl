<#include "common.ftl">

<#assign codes = .vars["codes$"+tableId]>
<#assign table = .vars["table$"+tableId]>
<#assign n = table.rows?size>
<#function class i> <#-- 设定奇偶行不同底色 -->
	<#if i%2==0><#return "even"><#else><#return "odd"></#if>
</#function>

<div id="div$${tableId}">
<table id="table$${tableId}" class="wr3table" border="1">

  <caption>指标：${measureMeta!""}</caption>

  <#-- 表头meta描述 --> <#--
  <tr>
    <th>&nbsp;</th>
  <#list table.head as th>
    <th id="${codes[th_index]}" title="${codes[th_index]}">${th}</th>
  </#list> 
  </tr> -->

  <#-- 表数据 -->
<#if n!=0>
<#list 0..n-1 as i>
  <tr class="${class(i)}">
    <#-- <td><#if i &gt; 0 && i != (n-1)>${i}<#else></#if></td> -->
  <#list table.rows[i] as td>
  	<#if i==0 && td_index==0>
  	<td class="crossLine"><div class="dimTop">${topMeta}</div><div class="dimLeft">${leftMeta}</div></td>
  	<#elseif i==0 || td_index==0>
  	<td class="dim">${td!"null"}</td>
  	<#elseif i==(n-1) || td_index==(table.rows[i]?size-1)>
  	<td class="numberFormat sum">${td!"null"}</td>
  	<#else>
    <td class="numberFormat">${td!"null"}</td> <#-- 注意：Table中有null元素的情况。 -->
    </#if>
  </#list> 
  </tr>
</#list>
</#if>

</table>
</div>
