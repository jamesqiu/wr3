<#include "common.ftl">

<#assign codes = .vars["codes$"+tableId]>
<#assign table = .vars["table$"+tableId]>
<#assign n = codes?size, m = 4> <#-- m: 每行显示m项 -->
<div id="div$${tableId}">
<form>
<table id="table$${tableId}" class="wr3table" border="1">
<#if n != 0>
<#list 0..n-1 as i>
  <#if i%m==0><tr></#if>
    <td class="label">${table.head[i]}: </td> <td id="${codes[i]}">${table.rows[0][i]}</td>
  <#if i%m==(m-1)></tr></#if>
</#list>
  <#if n%m != 0><td colspan=${(m-n%m)*2}></td></tr></#if>
</#if>
</table>
</form>
</div>
