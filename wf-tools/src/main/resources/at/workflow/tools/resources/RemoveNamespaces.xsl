<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="no" />

	<xsl:param name="nsToRemove">*</xsl:param>

	<xsl:template match="/|comment()|processing-instruction()">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="($nsToRemove = '*') or ($nsToRemove = namespace-uri(.))">
				<xsl:element name="{local-name()}">
					<xsl:apply-templates select="@*|node()" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="{name()}" namespace="{namespace-uri(.)}">
					<xsl:apply-templates select="@*|node()" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:choose>
			<xsl:when test="($nsToRemove = '*') or ($nsToRemove = namespace-uri(.))">
				<xsl:attribute name="{local-name()}">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="{name()}">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>