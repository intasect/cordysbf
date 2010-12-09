<#escape x as x?html>
<#assign current_page = "versions">
<#include "include/header.ftl">
<#include "custom/settings.ftl"> 
<#include "custom/file_types.ftl"> 

${loadXml("xml", "${ant_escaped.build_dir}/build-info-list.xml", "abspath")}

<#include "include/menu.ftl">
<div id="content">
	<h1>Project Releases</h1>
		
	<#list xml.buildinfolist.buildinfo as buildinfo>
		<#assign releaseDateStr = buildinfo.@date>
		
		<#if releaseDateStr?has_content>
			<#assign releaseDateStr = parseDate(releaseDateStr, "yyyy-MM-dd'T'HH:mm:ss")?string(DATE_FORMAT)>
		</#if>
			
		<h2 class="boxed">
			<a name="${buildinfo.@version}">Version ${buildinfo.@version} (released ${releaseDateStr})</a>
			<a style="font-size:small;padding-left:20px" href="changelog.html#${buildinfo.@version}">Changelog</a>			
		</h2>
		<div class="section">
			<table height="100%" border="0" cellpadding="0" cellspacing="0" class="ForrestTable">
		        <tr>
		        	<th><p>File</p></th>
		        	<th><p>Size</p></th>
		        	<th><p>Type</p></th>
		        </tr>
		
		        <tr class="evenrow">
		          <td height="2" nowrap></td>
		          <td></td>
		        </tr>			
		        
				<#list buildinfo.files.file as file>
					<#if file_index % 2 != 0>
						<#assign rowclass="evenrow">
						<#assign overcolor="#ffefa1">
						<#assign outcolor="#ffffff">
					<#else>
						<#assign rowclass="oddrow">
						<#assign overcolor="#ffefa1">
						<#assign outcolor="#f5f2e3">
					</#if>
									
			    	<#if file.@isfolder! == "true">
			    		<#assign target_attrib="target=\"_blank\"">
					</#if>					
				
				<tr class="${rowclass}" onMouseOver='this.style.backgroundColor="${overcolor}"' onMouseOut='this.style.backgroundColor="${outcolor}"'>
					<td><p><a href="${file.@url}" ${target_attrib!}>${file.@name}</a></p></td>
					<td><p>${file.@size}</p></td>
					<#if artifact_descriptions[file.@type]??>
						<td><p>${artifact_descriptions[file.@type]}</p></td>
					<#else>
						<td><p>${file.@name}</p></td>		
					</#if>					
				</tr>
				</#list>
			</table>
		</div>
	</#list>
</div>
<#include "include/footer.ftl">
</#escape>
