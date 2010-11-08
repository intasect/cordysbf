<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the response format to developer xml format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:template match="soapnodes">
    <soapnodes>
      <xsl:for-each select="entry">
        <xsl:apply-templates select="objectclass" />
      </xsl:for-each>
    </soapnodes>
  </xsl:template>

  <xsl:template match="objectclass">
    <xsl:choose>
      <xsl:when test="./string[2]='bussoapnode'">
        <soapnode>
          <xsl:variable name="dn" select="../@dn" />

          <xsl:attribute name="name">
            <xsl:value-of select="../cn/string" />
          </xsl:attribute>

          <xsl:attribute name="description">
            <xsl:value-of select="../description/string" />
          </xsl:attribute>

		  <xsl:for-each select="../labeleduri/string" >
			<namespace><xsl:value-of select="." /></namespace>
          </xsl:for-each>

		  <xsl:for-each select="../busmethodsets/string">
			<methodset><xsl:value-of select="." /></methodset>
          </xsl:for-each>

          <xsl:for-each select="parent::node()/parent::node()/entry">
            <xsl:variable name="soapprocessorDN" select="translate(./@dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
            <xsl:variable name="soapnodeDN" select="translate($dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
            <xsl:if test="contains($soapprocessorDN,$soapnodeDN)">
                <xsl:if test="./objectclass/string[2]='bussoapprocessor'">
                    <soapprocessor>
					  <xsl:variable name="dn" select="./@dn" />
                    	
                      <xsl:attribute name="name">
                        <xsl:value-of select="./cn/string" />
                      </xsl:attribute>

			          <xsl:attribute name="description">
			            <xsl:value-of select="./description/string" />
			          </xsl:attribute>
			          
			          <xsl:attribute name="computer">
			            <xsl:value-of select="./computer/string" />
			          </xsl:attribute>
			          
			          	<xsl:attribute name="osprocesshost">
			            <xsl:value-of select="./busosprocesshost/string" />
			          </xsl:attribute>

			          <xsl:attribute name="automaticstart">
			            <xsl:value-of select="./automaticstart/string" />
			          </xsl:attribute>
					  
					  <soapprocessorconfiguration>
						<xsl:value-of select="./bussoapprocessorconfiguration/string" />
			          </soapprocessorconfiguration>
			          	
			          <xsl:for-each select="parent::node()/entry">
			            <xsl:variable name="connectionpointDN" select="translate(./@dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
			            <xsl:variable name="soapprocessorDN" select="translate($dn,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
			            	<xsl:if test="contains($connectionpointDN,$soapprocessorDN)">
			                <xsl:if test="./objectclass/string[2]='busconnectionpoint'">
			                    <connectionpoint>
			                      <xsl:attribute name="name">
			                        <xsl:value-of select="./cn/string" />
			                      </xsl:attribute>
			
						          <xsl:attribute name="description">
						            <xsl:value-of select="./description/string" />
						          </xsl:attribute>
						          
						          <xsl:attribute name="private">
						            <xsl:value-of select="./busprivatepoint/string" />
						          </xsl:attribute>
						          
						          <xsl:attribute name="labeleduri">
						            <xsl:value-of select="./labeleduri/string" />
						          </xsl:attribute>
			
			                    </connectionpoint>
			                </xsl:if>
			            </xsl:if>
			          </xsl:for-each>
                    </soapprocessor>
                </xsl:if>
            </xsl:if>
          </xsl:for-each>
        </soapnode>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

