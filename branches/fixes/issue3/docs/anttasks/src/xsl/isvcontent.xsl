<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:template match="*"/>
    <xsl:template match="xml">
        <html xmlns="">
            <head>
                <title>
                	ISV Content Example :
                	<xsl:value-of select="@title"/>
                	- Provided by BCP Build Framework
                </title>
                <meta http-equiv="Content-Language" content="en-us" />
            </head>
            <body>
                <div>
                	&lt;<xsl:value-of select="@collectionname"/>&gt;<xsl:apply-templates select="item"/>&lt;/<xsl:value-of select="@collectionname" />&gt;
                </div>
            </body>
        </html>
    </xsl:template>

<xsl:template match="item">
<pre>
<xsl:text> </xsl:text><xsl:text> </xsl:text>&lt;<xsl:value-of select="@name" />
		<xsl:for-each select="attribute">
			<xsl:element name="br" />
<xsl:text> </xsl:text><xsl:text> </xsl:text><xsl:text> </xsl:text><xsl:text> </xsl:text><xsl:text> </xsl:text>
<xsl:value-of select="@name" />=&quot;<xsl:if test="@href"><xsl:element name="a">
<xsl:attribute name="href">
<xsl:value-of select="@href" />
</xsl:attribute>
<xsl:value-of select="@value" />
</xsl:element></xsl:if><xsl:if test="@nohref"><xsl:value-of select="@value" /></xsl:if>&quot;
</xsl:for-each>
<xsl:text> </xsl:text><xsl:text> </xsl:text>/&gt;
</pre>
</xsl:template>
</xsl:stylesheet>