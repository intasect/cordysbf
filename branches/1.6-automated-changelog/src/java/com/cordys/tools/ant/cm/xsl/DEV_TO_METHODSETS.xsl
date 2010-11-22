<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- To convert the developer xml format to update request format-->
<!--output format set to xml with indentation -->
  <xsl:output method="xml" indent="yes" />

  <xsl:param name="orgDN" />

  <xsl:variable name="methodSetDN" />

  <xsl:template match="/">
    <update>
      <xsl:for-each select="methodsets/methodset">
        <tuple>
          <new>
            <entry>
              <xsl:variable name="methodSetDN" select="concat('cn=',@name,',cn=method sets,',$orgDN)" />

              <xsl:attribute name="dn">
                <xsl:value-of select="$methodSetDN" />
              </xsl:attribute>

              <cn>
                <string>
                  <xsl:value-of select="@name" />
                </string>
              </cn>

              <objectclass>
                <string>top</string>
                <string>busmethodset</string>
              </objectclass>

			  <labeleduri>
		          <xsl:if test="string-length(string(@namespace)) != 0">
		          	<!--  Old version -->
		          	<string>
		          		<xsl:value-of select="@namespace" />
		          	</string>
		          </xsl:if>		
		          <xsl:if test="string-length(string(@namespace)) = 0">
		          	<!--  New version -->
		          	<xsl:for-each select="namespaces/namespace">
    	            	<string>
        	          		<xsl:value-of select="." />
                		</string>
             		</xsl:for-each>  
		          </xsl:if>			          	  
          	 </labeleduri>
	
              <implementationclass>
                <string>
                  <xsl:value-of select="@implementationclass" />
                </string>
              </implementationclass>
            </entry>
          </new>
        </tuple>

        <xsl:apply-templates select="method" />

        <xsl:apply-templates select="xsd" />
      </xsl:for-each>
    </update>
  </xsl:template>

  <xsl:template match="method">
    <xsl:variable name="methodsetCN" select="../@name" />

    <tuple>
      <new>
        <entry>
          <xsl:attribute name="dn">
            <xsl:value-of select="concat('cn=' , @name , ',cn=' , $methodsetCN , ',cn=method sets,' , $orgDN )" />
          </xsl:attribute>

          <cn>
            <string>
              <xsl:value-of select="@name" />
            </string>
          </cn>

          <objectclass>
            <string>top</string>

            <string>busmethod</string>
          </objectclass>

          <xsl:if test="string-length(string(methodimplementation))!='0'">
            <busmethodimplementation>
              <string>
                <xsl:value-of select="methodimplementation" />
              </string>
            </busmethodimplementation>
          </xsl:if>

          <xsl:if test="string-length(string(methodreturntype))!='0'">
            <busmethodreturntype>
              <xsl:variable name="return" select="substring-before(methodreturntype,',')" />

              <string>
                <xsl:value-of select="concat( $return , ',cn=' , $methodsetCN , ',cn=method sets,' , $orgDN )" />
              </string>
            </busmethodreturntype>
          </xsl:if>

          <xsl:if test="string-length(string(methodwsdl))!='0'">
            <busmethodwsdl>
              <string>
                <xsl:value-of select="methodwsdl" />
              </string>
            </busmethodwsdl>
          </xsl:if>

          <xsl:if test="string-length(string(methodinterface))!='0'">
            <busmethodinterface>
              <string>
                <xsl:value-of select="methodinterface" />
              </string>
            </busmethodinterface>
          </xsl:if>
          
          <xsl:if test="string-length(string(busmethodsignature))!='0'">
            <busmethodsignature>
              <string>
                <xsl:value-of select="busmethodsignature" />
              </string>
            </busmethodsignature>
          </xsl:if>          
        </entry>
      </new>
    </tuple>
  </xsl:template>

  <xsl:template match="xsd">
    <xsl:variable name="methodsetCN" select="../@name" />

    <tuple>
      <new>
        <entry>
          <xsl:variable name="methodSetDN" select="concat('cn=', @name, ',cn=', $methodsetCN ,',cn=method sets,',$orgDN)" />

          <xsl:attribute name="dn">
            <xsl:value-of select="$methodSetDN" />
          </xsl:attribute>

          <cn>
            <string>
              <xsl:value-of select="@name" />
            </string>
          </cn>

          <objectclass>
            <string>top</string>

            <string>busmethod</string>

            <string>busmethodtype</string>
          </objectclass>

          <busmethodtypexsd>
            <string>
              <xsl:value-of select="methodxsd" />
            </string>
          </busmethodtypexsd>
        </entry>
      </new>
    </tuple>
  </xsl:template>
</xsl:stylesheet>

