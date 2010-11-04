<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the developer xml format to update request format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="orgDN" />

  <xsl:template match="/">
    <roles>
      <xsl:apply-templates select="roles/role" />
    </roles>
  </xsl:template>

  <xsl:template match="role">
    <tuple>
      <new>
        <entry>
          <xsl:attribute name="dn">
            <xsl:value-of select="concat('cn=',@name,',cn=organizational roles,',$orgDN)" />
          </xsl:attribute>

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
			<xsl:if test="not(contains(@name, 'everyoneIn'))">          
            	<string>
              		<xsl:value-of
              			select="concat('cn=everyoneIn',substring-after(substring-before($orgDN,','),'o='),',cn=organizational roles,',$orgDN)" />
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
      </new>
    </tuple>

    <xsl:apply-templates select="acl" />
  </xsl:template>

  <xsl:template match="acl">
    <tuple>
      <new>
        <entry>
          <xsl:attribute name="dn">
            <xsl:value-of select="concat('cn=',@name,',cn=',../@name,',cn=organizational roles,',$orgDN)" />
          </xsl:attribute>

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

          <xsl:if test="string-length(string(@service)) != '0'">
            <service>
              <string>
                <xsl:value-of select="@service" />
              </string>
            </service>
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
      </new>
    </tuple>
  </xsl:template>

  <xsl:template match="sub-role">
    <string>
      <xsl:choose>
        <xsl:when test="contains(.,'cn=organizational roles')">
          <xsl:value-of select="concat(substring-before(.,','),',cn=organizational roles,',$orgDN)" />
        </xsl:when>

        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
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

