<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- To convert the developer xml structure to isv format-->
	<!--output format set to xml with indentation -->
	<xsl:output method="xml" indent="yes" />

	<xsl:param name="isvDN" />

	<!--change the <methodsets> to <busmethodsets> -->
	<xsl:template match="/">
		<busmethodsets>
			<xsl:attribute name="loader">com.eibus.contentmanagement.ISVMethodSetManager</xsl:attribute>

			<xsl:attribute name="description">Method Sets</xsl:attribute>

			<!--change the <methodset> to <busmethodset> -->
			<xsl:for-each select="methodsets/methodset">
				<busmethodset>
					<entry>
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
								<xsl:value-of
									select="@implementationclass" />
							</string>
						</implementationclass>
					</entry>

					<xsl:apply-templates select="method" />

					<xsl:apply-templates select="xsd" />
				</busmethodset>
			</xsl:for-each>
		</busmethodsets>
	</xsl:template>

	<!--change the <method> to <entry> format-->
	<xsl:template match="method">
		<xsl:variable name="methodsetCN" select="../@name" />

		<entry>
			<cn>
				<string>
					<xsl:value-of select="@name" />
				</string>
			</cn>

			<objectclass>
				<string>top</string>

				<string>busmethod</string>
			</objectclass>

			<xsl:if
				test="string-length(string(methodimplementation))!='0'">
				<busmethodimplementation>
					<string>
						<xsl:value-of select="methodimplementation" />
					</string>
				</busmethodimplementation>
			</xsl:if>

			<xsl:if
				test="string-length(string(methodreturntype))!='0'">
				<busmethodreturntype>
					<xsl:variable name="return"
						select="substring-before(methodreturntype,',')" />

					<string>
						<xsl:value-of
							select="concat( $return , ',cn=' , $methodsetCN , ',' , $isvDN )" />
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

			<xsl:if
				test="string-length(string(methodinterface))!='0'">
				<busmethodinterface>
					<string>
						<xsl:value-of select="methodinterface" />
					</string>
				</busmethodinterface>
			</xsl:if>

			<xsl:if
				test="string-length(string(busmethodsignature))!='0'">
				<busmethodsignature>
					<string>
						<xsl:value-of select="busmethodsignature" />
					</string>
				</busmethodsignature>
			</xsl:if>
		</entry>
	</xsl:template>

	<!--change the <xsd> to <entry> format-->
	<xsl:template match="xsd">
		<entry>
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
	</xsl:template>
</xsl:stylesheet>

