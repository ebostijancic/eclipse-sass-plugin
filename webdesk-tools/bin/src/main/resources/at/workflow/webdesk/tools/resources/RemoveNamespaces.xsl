<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="no"/>
	
	<xsl:param name="prefixToRemove">*</xsl:param>
	
	<xsl:template match="/|comment()|processing-instruction()">
		<xsl:copy>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*">
	
		<xsl:choose>
			<xsl:when test="($prefixToRemove = '*') or ($prefixToRemove = local-name(.))">
				<xsl:element name="{local-name()}">
					<xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="{name()}">
					<xsl:apply-templates select="@*|node()"/>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*">
		<xsl:choose>
			<xsl:when test="($prefixToRemove = '*') or ($prefixToRemove = local-name(.))">
				<xsl:attribute name="{local-name()}">
					<xsl:apply-templates select="."/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="{name()}">
					<xsl:apply-templates select="."/>
				</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>