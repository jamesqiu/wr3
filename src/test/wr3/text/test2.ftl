cell: ${cell1!"null"}
row: <#list row1 as cell>${cell},</#list>
table: 
<#list table1.head as cell>${cell}<#if cell_has_next>, </#if></#list>
<#list table1.rows as row>
<#list row as cell>${cell}<#if cell_has_next>, </#if></#list>
</#list>