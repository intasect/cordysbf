<?xml version="1.0"?>
<#escape x as x?xml>
<#compress>

<#setting locale="en_US">
<#assign max_item_count = 10>
<#assign pubDate = parseDate()>
<#assign pubDateStr = pubDate?string("EEE, dd MMM yyyy HH:mm:ss")>
<#assign latestGuid = ant["project.name"] + "-" + ant["buildinfo.latest.project.version"]>
<#assign oldGuid = "">

${loadXml("old_version", ant["build.dir"] + "/old-builds/rss.xml", "abspath", "nofail")}
${loadXml("latest_changelog", "changelog.xml", "abspath", "nofail", 
				"xpath=/document/body/release[@version='" + ant["buildinfo.latest.project.version"] + "']")}
				
<#if old_version?has_content && old_version.rss.channel.item?has_content>
	<#assign oldGuid = old_version.rss.channel.item[0].guid.@@text>
</#if>

<rss version="2.0">
  <channel>
    <title>Notifications for ${ant["project.name"]}</title>
    <link>${ant["project.site.real.url"]}/index.html</link>
    <description>New versions for the ${ant["project.name"]} can be monitored via this feed.</description>
    <lastBuildDate>${pubDateStr}</lastBuildDate>
    
    <#if oldGuid != latestGuid>
    <#assign max_item_count = max_item_count - 1>
    <item>
       <title>${ant["project.name"]}: Version ${ant["buildinfo.latest.project.version"]} available</title>
       <description>
       		<![CDATA[
	       		Version ${ant["buildinfo.latest.project.version"]} of ${ant["project.name"]} is available. <br/>
	       		<br/>
	       		
				<#if latest_changelog?has_content>
					Release changelog:<br/>
		       		<table>
					    <#list latest_changelog.action as action>
					    <tr>
					    	<td width="70px">[${action.@type?cap_first}]</td>
					    	<td><#noescape>${action.@@nested_markup}</#noescape></td>
					    </tr>
					    </#list>
					</table>
				<#else>
					No changelog avaiblable.<br/>
				</#if>		
				
				<br/>
	       		Release URL: <a href="${ant["project.site.real.url"]}/versions.html">${ant["project.site.real.url"]}/versions.html</a><br/>
			]]>
		</description>
       <link>${ant["project.site.real.url"]}/versions.html</link>
       <guid>${latestGuid}</guid>
       <pubDate>${pubDateStr}</pubDate>
    </item>
    </#if>
    
    <#if old_version?has_content>
    <#list old_version.rss.channel.item as item>
    	<#if (item_index >= max_item_count)><#break></#if>
		<#noescape>${item.@@markup}</#noescape>
    </#list>
    </#if>
  </channel>
</rss>

</#compress>
</#escape>