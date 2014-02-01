<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!--+
        |  matches config nodes and strips all
        |  outdated tags like 'sitemap' and 'layout'
        +-->
    <xsl:template match="config">
        <config>
            <xsl:copy-of select="*[name()!='columnRepeater' and name()!='layout' and name()!='sitemap']"/>
            <columns>
                <xsl:apply-templates select="columnRepeater"/>
            </columns>
        </config>
    </xsl:template>

    <!--+
        |  converts from old repeater structure (columnRepeater)
        +-->
    <xsl:template match="columnRepeater">
        <column>
            <xsl:copy-of select="*"/>
        </column>
    </xsl:template>

</xsl:stylesheet>