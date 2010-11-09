
<#-- 
	Defines all artifact types that can be created by projects.
	This infomation is first put into the build-info.xml and
	later read by the versions.html.ftl to create the build
	file list.
-->

<#-- Defines all included artifact ID's -->
<#assign artifact_list = [ 
		"isvp", 
		"document", 
		"javadoc",
		"junit-reports",		
		"cobertura-reports",		
		"source-bundle",
		"jar"
	]>

<#-- Defines artifact descriptions for the versions page. -->
<#assign artifact_descriptions = { 
		"isvp" : "ISV Package",
		"document" : "Documentation",
		"javadoc" : "Java API Documentation",
		"junit-reports" : "JUnit Test Report",
		"cobertura-reports" : "Cobertura Test Report",
		"source-bundle" : "Project Source Bundle",
		"jar" : "Java Library",
		"unknown" : ""
	}>
	
<#-- Defines artifact labels for build-info.xml. These are shown in the description page. -->	
<#assign artifact_labels = { 
		"javadoc" : "[Javadocs]",
		"junit-reports" : "[Junit]",
		"cobertura-reports" : "[Cobertura]"
	}>	
	
<#-- Defines if artifact link points to a folder and should be opened in a new window. -->	
<#assign artifact_isfolder = { 
		"javadoc" : "true",
		"junit-reports": "true",
		"cobertura-reports" : "true"		
	}>		
	
<#-- Defines artifact file match regular expressions for build-info.xml -->
<#assign artifact_patterns = { 
		"isvp" : "^setup/[^/]+\\.isvp$",
		"document" : "^docs/[^/]+$",
		"javadoc" : "^docs/api/index\\.html$",
		"junit-reports" : "^docs/reports/junit/index\\.html$",
		"cobertura-reports" : "^docs/reports/cobertura/index\\.html$",		
		"source-bundle" : "^[^/]+-src\\.zip$",
		"jar" : "^jar/[^/]+\\.jar$"
	}>	
		
	
