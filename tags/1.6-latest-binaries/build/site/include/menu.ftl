<#include "../custom/menu-items.ftl">

<div id="top" class="header">
	<img src="${ant["project.site.style.path"]}/images/logo.gif" alt="Logo main" width="134" height="47"/>
	<h1>${ant["project.site.downloadarea.title"]}</h1>
</div>
<h2 class="boxed">${ant["project.site.main.title"]}</h2>
<div id="publishedStrip">
	<div id="level2tabs">
		<a class="" href="${ant["project.site.project.index.path"]}/index.html">Project Index</a>
    </div>
	<script type="text/javascript"><!--
		document.write("<text>Last Published:</text> " + document.lastModified);
		//  -->
	</script>
	&nbsp; <a href="rss.xml"><img src="${ant["project.site.style.path"]}/images/rss.gif" width="12" height="12"/></a>
</div>
<div class="breadtrail"> &nbsp; </div>
<div id="menu">
	<div class="menuitemgroup"></div>
	<div class="selectedmenuitemgroup" style="display: block;">
	<#list menu_items as item>
		<#if current_page = item>
		<div class="menupage">
			<div class="menupagetitle">${menu_titles[item]}</div>
		</div>
		<#else>
		<div class="menuitem">
			<#if menu_link_targets[item]?has_content>
				<#assign targetattrib="target=\"${menu_link_targets[item]}\"">
			<#else>
				<#assign targetattrib="">
			</#if>
			<a href="${menu_links[item]}" title="${menu_titles[item]}" ${targetattrib}>${menu_titles[item]}</a>
		</div>
		</#if>
	</#list>
	</div>

	<div id="roundbottom">
		<img style="display: none" class="corner" height="15" width="15" alt="" src="skin/images/rc-b-l-15-1body-2menu-3menu.png"/>		</div>
	<div id="credit2"></div>
</div>	
