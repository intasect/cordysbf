<#escape x as x?html>
<#assign current_page = "changelog">
<#include "include/header.ftl">
<#include "custom/settings.ftl"> 

${loadXml("xml", "../../changelog.xml", "nofail")}
	 
<#include "include/menu.ftl">
<div id="content">
	 
<#if xml?has_content>
<h1>Change History</h1>
	
<#list xml.document.body.release as release>
	<#if release.@date?has_content>
		<#assign releaseDateStr = parseDate(release.@date, "yyyy-MM-dd")?string(DATE_FORMAT)>

		<h2 class="boxed">
			<a name="${release.@version}">Version ${release.@version} (released ${releaseDateStr})</a>
			<a style="font-size:small;padding-left:20px" href="versions.html#${release.@version}">Downloads</a>			
		</h2>
		<div class="section">
			<table height="100%" border="0" cellpadding="0" cellspacing="0" class="ForrestTable">		
				<#list release.action as action>
					<#if action_index % 2 == 0>
						<#assign rowclass="oddrow">
						<#assign overcolor="#ffefa1">
						<#assign outcolor="#f5f2e3">
					<#else>
						<#assign rowclass="evenrow">
						<#assign overcolor="#ffefa1">
						<#assign outcolor="#ffffff">
					</#if>
					
					<tr class="${rowclass}" onMouseOver='this.style.backgroundColor="${overcolor}"' onMouseOut='this.style.backgroundColor="${outcolor}"'>
						<td style="width: 70px"><p><b>${action.@type?cap_first}</b></p></td>
						<td>
							<p>
								<#noescape>${action.@@nested_markup}</#noescape>
							</p>						 	
						</td>
					</tr>
				</#list>
			</table>
		</div>
	</#if>
</#list>

<#else>
	No change log information defined for this project.
</#if>

<#include "include/footer.ftl">
</#escape>