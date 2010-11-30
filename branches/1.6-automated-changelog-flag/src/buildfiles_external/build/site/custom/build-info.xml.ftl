<?xml version="1.0"?>
<#escape x as x?xml>
<#compress>

<#-- 
 	Creates the XML file that holds information about a created build. 

 	Note that this file is not part of the project site, but is placed
 	here for consistency (file_types.ftl contains file types for files
 	that are listed in this XML).
 -->

<#setting url_escaping_charset='UTF-8'>

<#assign dist = ant["dist.dir"]>
<#assign build_path = ant["build.info.relative.build.path"]>

<#include "file_types.ftl"> 

<buildinfo date="${ant["build.info.date"]}" 
		   version="${ant["version.full"]}"
		   timestamp="${currentTimeMillis?c}">
    <projectversion>${ant["project.version"]}</projectversion>
    <projectname>${ant["project.name"]}</projectname>
    <projecturl>${ant["project.site.real.url"]}</projecturl>
    <files>
	    <#list artifact_list as id>
	    	<#assign files = listFiles("${dist}" "recursive") >
	    	<#assign regex = "${artifact_patterns[id]}">
	    	
		    <#list files as f>
				<#if f.toRelativeUrl()?matches(regex)>
			    	<#if artifact_labels[id]! != "">
			    		<#assign label=artifact_labels[id]>
			    	<#else>
			    		<#assign label=f.file.name>
					</#if>		
					
			    	<#if artifact_isfolder[id]! == "true">
			    		<#assign isfolder="true">
			    		<#assign filesize="">
			    	<#else>
			    		<#assign isfolder="false">
			    		<#assign filesize="${f.file.length()?c}">
					</#if>									
				
					<file name="${label}" 
						  type="${id}"
						  size="${filesize}"
						  url="${build_path}/${f.toRelativeUrl()}"
						  isfolder="${isfolder}"
						  />    
				</#if>
		    </#list>
	     </#list>
    </files>
</buildinfo>

</#compress>
</#escape>