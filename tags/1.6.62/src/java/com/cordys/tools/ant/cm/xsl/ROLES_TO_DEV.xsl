<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the response format to developer xml format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="orgDN" />

  <xsl:template match="/">
    <roles>
      <xsl:apply-templates select="roles/entry" />
    </roles>
  </xsl:template>

  <xsl:template match="entry">
    <xsl:if test="objectclass/string[2]='busorganizationalrole'">
      <xsl:variable name="dn" select="@dn" />

      <role>
        <xsl:attribute name="name">
          <xsl:value-of select="cn/string" />
        </xsl:attribute>

        <xsl:attribute name="description">
          <xsl:value-of select="description/string" />
        </xsl:attribute>

        <xsl:apply-templates select="role" />

        <xsl:apply-templates select="menu" />
		
        <xsl:apply-templates select="toolbar" />		

        <xsl:apply-templates select="accesscontrolset" />

        <xsl:apply-templates select="busextention" />

        <xsl:for-each select="../entry">
          <xsl:if test="contains(@dn,$dn) and objectclass/string[2]='busaccesscontrolset'">
            <acl>
              <xsl:attribute name="name">
                <xsl:value-of select="cn/string" />
              </xsl:attribute>

              <xsl:attribute name="description">
                <xsl:value-of select="description/string" />
              </xsl:attribute>

              <xsl:attribute name="acl">
                <xsl:value-of select="acl/string" />
              </xsl:attribute>

              <xsl:attribute name="organizationalobject">
                <xsl:value-of select="organizationalobject/string" />
              </xsl:attribute>

              <xsl:attribute name="service">
                <xsl:value-of select="service/string" />
              </xsl:attribute>

              <xsl:apply-templates select="acobjecttree" />

              <xsl:apply-templates select="acdomaintree" />
            </acl>
          </xsl:if>
        </xsl:for-each>
      </role>
    </xsl:if>
  </xsl:template>

  <xsl:template match="role">
    <xsl:for-each select="string">
      <xsl:variable name="defaultOrganizationalRole" select="concat('everyoneIn',substring-after(substring-before($orgDN,','),'='))" />

<!-- Ignores the subrole if its a 'everyoneIn OrganizationName' -->
      <xsl:if test="$defaultOrganizationalRole != substring-after(substring-before(.,','),'=')">
        <sub-role>
          <xsl:value-of select="." />
        </sub-role>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="menu">
    <xsl:for-each select="string">
      <menu>
        <xsl:value-of select="." />
      </menu>
    </xsl:for-each>
  </xsl:template>
  
   <xsl:template match="toolbar">
    <xsl:for-each select="string">
      <toolbar>
        <xsl:value-of select="." />
      </toolbar>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="accesscontrolset">
    <xsl:for-each select="string">
      <accesscontrolset>
        <xsl:value-of select="." />
      </accesscontrolset>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="busextention">
    <xsl:for-each select="string">
      <busextention>
        <xsl:value-of select="." />
      </busextention>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="acobjecttree">
    <xsl:for-each select="string">
      <acobjecttree>
        <xsl:value-of select="." />
      </acobjecttree>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="acdomaintree">
    <xsl:for-each select="string">
      <acdomaintree>
        <xsl:value-of select="." />
      </acdomaintree>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>

