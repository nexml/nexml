<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:local="http://www.phylo.org/local">
    <xsl:output method="text" omit-xml-declaration="yes" doctype-public="text/plain"/>
    
    <!-- constructs a newick tree out of a ListTree xml description -->
    <xsl:function name="local:ListTree2Newick">
        <xsl:param name="nodeID"/>
        <xsl:param name="tree"/>
        <xsl:if test="$tree//parent[@node=$nodeID]">
        <xsl:text>(</xsl:text>
        <xsl:for-each select="$tree//parent[@node=$nodeID]"> 
            <xsl:variable name="childID" select="../@id"/>
            <xsl:value-of select="local:ListTree2Newick($childID,$tree)"/>
            <xsl:value-of select="../@label"/>
            <xsl:text>:</xsl:text>
            <xsl:value-of select="../dict//float"/>
            <xsl:if test="$tree//parent[@node=$nodeID][last()]/../@id != $childID">
                <xsl:text>,</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:function>
    
    <!-- constructs a newick tree out of a NestedTree xml description -->
    <xsl:function name="local:NestedTree2Newick">
        <xsl:param name="node"/>
        <xsl:param name="tree"/>
        <xsl:if test="$tree//$node/node">
            <xsl:text>(</xsl:text>
            <xsl:for-each select="$tree//$node/node">
                <xsl:variable name="child" select="."/>
                <xsl:value-of select="local:NestedTree2Newick($child,$tree)"/>
                <xsl:value-of select="@label"/>
                <xsl:text>:</xsl:text>
                <xsl:value-of select="./dict//float"/>
                <xsl:text>,</xsl:text>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:function>
    
    <!-- start processing -->  
    <xsl:template match="/">        
        #NEXUS  
        <xsl:value-of select="nexml/dict//string"/>
        
        <!-- process taxa blocks -->
        <xsl:for-each select="nexml/otus">
        BEGIN TAXA;
            <xsl:value-of select=".//dict//string"/>
            TITLE <xsl:value-of select="@id"/>;
            DIMENSIONS NTAX=<xsl:value-of select="./count(otu)"/>;
            TAXLABELS 
                <xsl:value-of select="otu/@label"/>;
        END;
        </xsl:for-each> 
        
        <!-- process characters blocks -->
        <xsl:for-each select="nexml/characters">
        BEGIN CHARACTERS;
            <xsl:value-of select=".//dict//string"/>
            <xsl:variable name="otusid" select="@otus"/>
            <xsl:variable name="datatype" select="@xsi:type"/>
            <xsl:variable name="charid" select="@id"/>
            LINK TAXA=<xsl:value-of select="@otus"/>;
            TITLE <xsl:value-of select="@id"/>;
            <xsl:if test="$datatype != 'DNA'">DIMENSIONS NCHAR=<xsl:value-of select="./matrix/definitions/count(def)"/>;</xsl:if>
            <xsl:if test="$datatype = 'DNA'">DIMENSIONS NCHAR=;</xsl:if>
            FORMAT DATATYPE=<xsl:value-of select="@xsi:type"/>;   
                MATRIX
                <xsl:for-each select="./matrix/row">                
                    <xsl:variable name="otuid" select="@otu"/>
                        <xsl:value-of select="/nexml/otus[@id=$otusid]/otu[@id=$otuid]/@label"/>
                        <xsl:text>&#x20;</xsl:text>
                        <xsl:for-each select="./observations/obs">                            
                            <xsl:if test="$datatype = 'STANDARD'">
                                <xsl:variable name="val" select="@val"/>
                                <xsl:variable name="def" select="@def"/>
                                <xsl:value-of select="/nexml/characters[@id=$charid]/matrix/definitions/def[@id=$def]/val[@id=$val]/@sym"/>
                                <xsl:text>&#x20;</xsl:text>
                            </xsl:if>
                            <xsl:if test="$datatype = 'CONTINUOUS'">
                                <xsl:text>&#x20;</xsl:text>
                            </xsl:if>
                            <xsl:if test="$datatype != 'STANDARD'">
                                <xsl:value-of select="@val"/>
                            </xsl:if>
                        </xsl:for-each> 
                        <xsl:value-of select="./seq"/>
                    <xsl:text>&#xa;</xsl:text>      
                </xsl:for-each>
                ; 
        END;
        </xsl:for-each>
        
        <!-- process trees blocks -->
        <xsl:for-each select="nexml/trees">
        BEGIN TREES;
            TITLE <xsl:value-of select="@id"/>;
            LINK TAXA=<xsl:value-of select="@otus"/>;
            <xsl:for-each select="./tree">
                <xsl:variable name="tree" select="."/>
                <xsl:variable name="treetype" select="@xsi:type"/>
                <xsl:variable name="rootID" select="./root/@id"/>
                <xsl:variable name="root" select="./root"/>
                <xsl:if test="$treetype = 'ListTree'">
                    TREE <xsl:value-of select="$tree/@id"/> = <xsl:value-of select="local:ListTree2Newick($rootID,$tree)"/>;                                            
                </xsl:if>
                <xsl:if test="$treetype = 'NestedTree'">
                    TREE <xsl:value-of select="$tree/@id"/> = <xsl:value-of select="local:NestedTree2Newick($root,$tree)"/>;
                </xsl:if>
            </xsl:for-each>        
        END;    
        </xsl:for-each> 
    </xsl:template>
</xsl:stylesheet>
