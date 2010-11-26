<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the developer xml format to update request format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="orgDN" />

  <xsl:template match="/">
    <update>
      <xsl:for-each select="soapnodes/soapnode">
        <tuple>
          <new>
            <entry>
              <xsl:variable name="soapNodeDN" select="concat('cn=',@name,',cn=soap nodes,',$orgDN)" />

              <xsl:attribute name="dn">
                <xsl:value-of select="$soapNodeDN" />
              </xsl:attribute>

              <cn>
                <string>
                  <xsl:value-of select="@name" />
                </string>
              </cn>
              
              <description>
                <string>
                  <xsl:value-of select="@description" />
                </string>
              </description>

              <objectclass>
                <string>top</string>

                <string>bussoapnode</string>
              </objectclass>

              <labeleduri>
				  <xsl:for-each select="namespace" >
					<string><xsl:value-of select="." /></string>
				  </xsl:for-each>
              </labeleduri>

              <busmethodsets>
				  <xsl:for-each select="methodset">
					<xsl:variable name="methodsetCN" select="substring-before(.,',')" />
					<xsl:variable name="baseDN" select="translate(substring-after(.,','),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
				  	
				  	<xsl:choose>
				  		<xsl:when test="starts-with($baseDN,'cn=method sets')">
							<string><xsl:value-of select="concat( $methodsetCN , ',cn=method sets,' , $orgDN )" /></string>
						</xsl:when>
						<xsl:otherwise>
							<string><xsl:value-of select="." /></string>
						</xsl:otherwise>
				  	</xsl:choose>
				  </xsl:for-each>
              </busmethodsets>
			  
            </entry>
          </new>
        </tuple>

        <xsl:apply-templates select="soapprocessor" />

      </xsl:for-each>
    </update>
  </xsl:template>

  <xsl:template match="soapprocessor">
    <xsl:variable name="soapnodeCN" select="../@name" />

    <tuple>
      <new>
        <entry>
          <xsl:attribute name="dn">
            <xsl:value-of select="concat('cn=' , @name , ',cn=' , $soapnodeCN , ',cn=soap nodes,' , $orgDN )" />
          </xsl:attribute>

          <cn>
            <string>
              <xsl:value-of select="@name" />
            </string>
          </cn>
              
		  <description>
			<string>
			  <xsl:value-of select="@description" />
			</string>
		  </description>

          <objectclass>
            <string>top</string>

            <string>bussoapprocessor</string>
          </objectclass>

		  <automaticstart>
		    <string>
			  <xsl:value-of select="@automaticstart" />
		    </string>
		  </automaticstart>

		  <computer>
		    <string>
			  <xsl:value-of select="@computer" />
		    </string>
		  </computer>

          <xsl:if test="string-length(string(@osprocesshost))!='0'">
            <busosprocesshost>
              <string>
                <xsl:value-of select="@osprocesshost" />
              </string>
            </busosprocesshost>
          </xsl:if>

		  <bussoapprocessorconfiguration>
		    <string>
			  <xsl:value-of select="soapprocessorconfiguration" />
		    </string>
		  </bussoapprocessorconfiguration>

        </entry>
      </new>
    </tuple>

	<xsl:apply-templates select="connectionpoint" />
    
  </xsl:template>

  <xsl:template match="connectionpoint">
    <xsl:variable name="soapnodeCN" select="../../@name" />
    <xsl:variable name="soapprocessorCN" select="../@name" />

    <tuple>
      <new>
        <entry>
          <xsl:variable name="connectionpointDN" select="concat('cn=', @name, ',cn=', $soapprocessorCN, ',cn=', $soapnodeCN ,',cn=soap nodes,',$orgDN)" />

          <xsl:attribute name="dn">
            <xsl:value-of select="$connectionpointDN" />
          </xsl:attribute>

          <cn>
            <string>
              <xsl:value-of select="@name" />
            </string>
          </cn>
              
		  <description>
			<string>
			  <xsl:value-of select="@description" />
			</string>
		  </description>

          <objectclass>
            <string>top</string>

            <string>busconnectionpoint</string>
          </objectclass>

          <labeleduri>
            <string>
              <xsl:value-of select="@labeleduri" />
            </string>
          </labeleduri>

          <xsl:if test="string-length(string(@private))!='0'">
            <busprivatepoint>
              <string>
                <xsl:value-of select="@private" />
              </string>
            </busprivatepoint>
          </xsl:if>

        </entry>
      </new>
    </tuple>
  </xsl:template>
</xsl:stylesheet>

