<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the response format to developer xml format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:template match="methodsets">
    <methodsets>
      <xsl:for-each select="entry">
        <xsl:apply-templates select="objectclass" />
      </xsl:for-each>
    </methodsets>
  </xsl:template>

  <xsl:template match="objectclass">
    <xsl:choose>
      <xsl:when test="./string[2]='busmethodset'">
        <methodset>
          <xsl:variable name="dn" select="../@dn" />

          <xsl:attribute name="name">
            <xsl:value-of select="../cn/string" />
          </xsl:attribute>

          <xsl:attribute name="description">
            <xsl:value-of select="../description/string" />
          </xsl:attribute>

          <xsl:attribute name="implementationclass">
            <xsl:value-of select="../implementationclass/string" />
          </xsl:attribute>

          <namespaces>
	          <xsl:for-each select="../labeleduri/string">
	          	<namespace>
	          		<xsl:value-of select="." />
	          	</namespace>
	          </xsl:for-each>          
          </namespaces>

          <xsl:for-each select="parent::node()/parent::node()/entry">
            <xsl:variable name="methodDN" select="translate(./@dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
            <xsl:variable name="methodsetDN" select="translate($dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
            <xsl:if test="contains($methodDN,$methodsetDN)">
            <!-- object class of xsd can be just top|busmethodtype -->
			<xsl:if test="./objectclass/string[2]='busmethodtype'">
				<xsd>
					<xsl:attribute name="name">
						<xsl:value-of select="./cn/string" />
					</xsl:attribute>
					
					<methodxsd>
						<xsl:value-of select="./busmethodtypexsd/string" />
					</methodxsd>
				</xsd>
			</xsl:if>           
              <xsl:choose>
                <xsl:when test="./objectclass/string[2]='busmethod'">
				  <!-- object class of xsd can be top|busmethod|busmethodtype -->
                  <xsl:if test="./objectclass/string[3]='busmethodtype'">
                    <xsd>
                      <xsl:attribute name="name">
                        <xsl:value-of select="./cn/string" />
                      </xsl:attribute>

                      <methodxsd>
                        <xsl:value-of select="./busmethodtypexsd/string" />
                      </methodxsd>
                    </xsd>
                  </xsl:if>

                  <xsl:if test="./objectclass/string[2]='busmethod'and not(./objectclass/string[3])">
                    <method>
                      <xsl:attribute name="name">
                        <xsl:value-of select="./cn/string" />
                      </xsl:attribute>

                      <methodimplementation>
                        <xsl:value-of select="./busmethodimplementation/string" />
                      </methodimplementation>

                      <methodreturntype>
                        <xsl:value-of select="./busmethodreturntype/string" />
                      </methodreturntype>

                      <methodwsdl>
                        <xsl:value-of select="./busmethodwsdl/string" />
                      </methodwsdl>

                      <methodinterface>
                        <xsl:value-of select="./busmethodinterface/string" />
                      </methodinterface>
                      
                      <busmethodsignature>
                        <xsl:value-of select="./busmethodsignature/string" />
                      </busmethodsignature>                      
                    </method>
                  </xsl:if>
                </xsl:when>
              </xsl:choose>
            </xsl:if>
          </xsl:for-each>
        </methodset>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

