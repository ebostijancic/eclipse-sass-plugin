<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template match="action-config">
        <action-config>
            <layout>
                <xsl:copy-of select="/action-config/layout/*[name()!='columns']"/>
                <columns>
                    <xsl:apply-templates select="/action-config/layout/columns/column"/>
                </columns>
            </layout>
            <xsl:copy-of select="/action-config/*[name()!='layout']"/>
        </action-config>
    </xsl:template>
    
    <!--+ 
        | looks for column tags of type account and no pattern defined
        | and generates a default pattern an decimal-separator
        +-->
    <xsl:template match="column">
        <xsl:choose>
            <xsl:when test="@type='account' and not(@pattern)">
                <!--column attribute="{@attribute}" convert="{@convert}" dosummary="{@dosummary}" header="{@header}" type="{@type}" tdattr_style="{@tdattr_style}" pattern="0.00" decimal-separator="," /-->
                <column pattern="0.00" decimal-separator=",">
                    <xsl:for-each select="@*[name(.) != 'pattern' and name(.)!='decimal-separator']">
                        <xsl:attribute name="{name(.)}">
                            <xsl:value-of select="."/>
                        </xsl:attribute>
                    </xsl:for-each>
                </column>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
</xsl:stylesheet>
