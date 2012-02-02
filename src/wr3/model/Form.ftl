<form name="${formName}" action="${action}" class="wr3form">
<table border=0>
	<caption class="ui-state-default ui-corner-top">${title}</caption>
	<tbody>
	<#list fields as field>
		<tr>${field}</tr>
	</#list>
	</tbody>
</table>

</form>