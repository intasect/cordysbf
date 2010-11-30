<#escape x as x?html>
<#assign current_page = "index">
<#include "include/header.ftl">
<#include "include/menu.ftl">

<div id="content">
	<h1>Project description</h1>
	<div class="section">
		<#include "custom/project-description.ftl">	
	</div>
</div>
<#include "include/footer.ftl">
</#escape>