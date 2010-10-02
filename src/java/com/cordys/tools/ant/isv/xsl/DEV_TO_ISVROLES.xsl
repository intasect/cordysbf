<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the developer xml structure to isv format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="orgDN" />

  <xsl:param name="isvDN" />
  
  <xsl:variable name="everyoneInISV" select="concat('cn=everyoneIn',substring-after(substring-before($isvDN,','), '='),',cn=organizational roles,',$orgDN)" />

  <xsl:template match="/">
    <busorganizationalroles>
      <xsl:attribute name="loader">com.eibus.contentmanagement.ISVRoleManager</xsl:attribute>

      <xsl:attribute name="description">Roles</xsl:attribute>

<!-- Attach mandatory 'everyoneInISV' role if not available-->
      <busorganizationalrole>
        <entry>
          <cn>
            <string>
              <xsl:value-of select="concat('everyoneIn',substring-after(substring-before($isvDN,','),'='))" />
            </string>
          </cn>

          <description>
            <string>
              <xsl:value-of select="concat('Everyone In ',substring-after(substring-before($isvDN,','),'='))" />
            </string>
          </description>

          <objectclass>
            <string>top</string>

            <string>busorganizationalrole</string>

            <string>busorganizationalobject</string>
          </objectclass>
        </entry>
      </busorganizationalrole>

<!-- Attach other roles -->
      <xsl:apply-templates select="roles/role" />
    </busorganizationalroles>
  </xsl:template>

  <xsl:template match="role">
    <busorganizationalrole>
      <entry>
        <cn>
          <string>
            <xsl:value-of select="@name" />
          </string>
        </cn>

        <xsl:if test="string-length(string(@description)) != '0'">
          <description>
            <string>
              <xsl:value-of select="@description" />
            </string>
          </description>
        </xsl:if>

        <objectclass>
          <string>top</string>

          <string>busorganizationalrole</string>

          <string>busorganizationalobject</string>
        </objectclass>

        <role>
<!-- Attach 'everyoneInISV' role as super role for every role in ISV (except everyoneIn<ISV> role-->
          <xsl:if test="not(contains(@name, 'everyoneIn'))">
            <string>
              <xsl:value-of select="$everyoneInISV" />
            </string>
          </xsl:if>

          <xsl:apply-templates select="sub-role" />
        </role>

        <xsl:if test="menu">
          <menu>
            <xsl:apply-templates select="menu" />
          </menu>
        </xsl:if>
		
        <xsl:if test="toolbar">
          <toolbar>
            <xsl:apply-templates select="toolbar" />
          </toolbar>
        </xsl:if>		

        <xsl:if test="accesscontrolset">
          <accesscontrolset>
            <xsl:apply-templates select="accesscontolset" />
          </accesscontrolset>
        </xsl:if>

        <xsl:if test="busextention">
          <busextention>
            <xsl:apply-templates select="busextention" />
          </busextention>
        </xsl:if>
      </entry>

      <xsl:apply-templates select="acl" />

      <xsl:apply-templates select="menu" mode="externalmenu" />
      <xsl:apply-templates select="toolbar" mode="externaltoolbar" />	  
    </busorganizationalrole>
  </xsl:template>

  <xsl:template match="acl">
    <entry>
      <cn>
        <string>
          <xsl:value-of select="@name" />
        </string>
      </cn>

      <objectclass>
        <string>top</string>

        <string>busaccesscontrolset</string>
      </objectclass>

      <xsl:if test="string-length(string(@description)) != '0'">
        <description>
          <string>
            <xsl:value-of select="@description" />
          </string>
        </description>
      </xsl:if>

      <acl>
        <string>
          <xsl:choose>
            <xsl:when test="contains(@acl,'cn=organizational roles')">
              <xsl:value-of select="normalize-space(concat(substring-before(@acl,','),',cn=organizational roles,',$orgDN))" />
            </xsl:when>

            <xsl:otherwise>
              <xsl:value-of select="normalize-space(@acl)" />
            </xsl:otherwise>
          </xsl:choose>
        </string>
      </acl>

      <xsl:if test="string-length(string(@organizationalobject)) != '0'">
        <organizationalobject>
          <string>
            <xsl:value-of select="@organizationalobject" />
          </string>
        </organizationalobject>
      </xsl:if>

      <xsl:if test="acobjecttree">
        <acobjecttree>
          <xsl:apply-templates select="acobjecttree" />
        </acobjecttree>
      </xsl:if>

      <xsl:if test="acdomaintree">
        <acdomaintree>
          <xsl:apply-templates select="acdomaintree" />
        </acdomaintree>
      </xsl:if>
    </entry>
  </xsl:template>

  <xsl:template match="sub-role">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>

  <xsl:template match="menu">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>
  
  <xsl:template match="toolbar">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>  

  <xsl:template match="menu" mode="externalmenu">
    <xsl:variable name="key" select="." />

    <SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP:Body>
        <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
          <tuple version="isv" unconditional="true">
            <xsl:attribute name="key">
              <xsl:value-of select="$key" />
            </xsl:attribute>

            <new>
              <tobereplacedmenu>
	             <xsl:attribute name="key">
	               <xsl:value-of select="$key" />
	             </xsl:attribute>
	          </tobereplacedmenu>
            </new>
          </tuple>
        </UpdateXMLObject>
      </SOAP:Body>
    </SOAP:Envelope>
  </xsl:template>
  
  <xsl:template match="toolbar" mode="externaltoolbar">
    <xsl:variable name="key" select="." />

    <SOAP:Envelope xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP:Body>
        <UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
          <tuple version="isv" unconditional="true">
            <xsl:attribute name="key">
              <xsl:value-of select="$key" />
            </xsl:attribute>

            <new>
              <tobereplacedtoolbar>
	             <xsl:attribute name="key">
	               <xsl:value-of select="$key" />
	             </xsl:attribute>			 
	          </tobereplacedtoolbar>
            </new>
          </tuple>
        </UpdateXMLObject>
      </SOAP:Body>
    </SOAP:Envelope>
  </xsl:template>  

  <xsl:template match="accesscontrolset">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>

  <xsl:template match="busextention">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>

  <xsl:template match="acobjecttree">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>

  <xsl:template match="acdomaintree">
    <string>
      <xsl:value-of select="." />
    </string>
  </xsl:template>
</xsl:stylesheet>

