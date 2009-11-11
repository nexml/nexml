<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:nex="http://www.nexml.org/1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:cdao="http://www.evolutionaryontology.org/cdao.owl#"
    xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="xml"/>

    <!-- Boiler-Plate  -->

    <!-- Make the new root element -->
    <xsl:template match="/nex:nexml">
        <rdf:RDF xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
            xmlns:owl="http://www.w3.org/2002/07/owl#">
            <xsl:apply-templates select="descendant::*"/>
        </rdf:RDF>
    </xsl:template>

    <!-- Create the TU's  -->
    <xsl:template match="nex:otu">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of
                    select="@id"/></xsl:attribute>
            <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#TU"/>
        </rdf:Description>
    </xsl:template>

    <!-- Create the Trees -->
    <xsl:template match="nex:tree">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="../@id"/></xsl:attribute>
            <xsl:if test="@label">
                <xsl:call-template name="label"/>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="nex:node/@root = 'true'">
                    <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#RootedTree"/>
                    <cdao:has_Root>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of
                                select="nex:node[@root = 'true']/@id"/></xsl:attribute>
                    </cdao:has_Root>
                </xsl:when>
                <xsl:otherwise>
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#UnrootedTree"/>
                </xsl:otherwise>
            </xsl:choose>
        </rdf:Description>
    </xsl:template>
    <!-- Create a Network  -->
    <xsl:template match="nex:network">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="../@id"/></xsl:attribute>
            <xsl:if test="@label">
                <xsl:call-template name="label"/>
            </xsl:if>
            <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#Network"/>
        </rdf:Description>
    </xsl:template>
    <!-- Process an ancestor -->
    <xsl:template name="processancestor">
        <xsl:param name="cnodeid"/>
        <xsl:variable name="treeid" select="../@id"/>
        <xsl:choose>
            <xsl:when test="../nex:node[@id = $cnodeid and @root = 'true' ]"> </xsl:when>
            <xsl:when test="../nex:node[ @id = $cnodeid ]">
                <xsl:variable name="edge" select="../nex:edge[ @target = $cnodeid]"/>
                <cdao:has_Ancestor>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of
                            select="$edge/@source"/></xsl:attribute>
                </cdao:has_Ancestor>
                <xsl:call-template name="processancestor">
                    <xsl:with-param name="cnodeid" select="$edge/@source"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise> </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Process a Node -->
    <xsl:template match="nex:node">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="@id"
                /></xsl:attribute>
            <xsl:if test="@label">
                <xsl:call-template name="label"/>
            </xsl:if>
            <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#Node"/>
            <cdao:belongs_to_Tree>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="../@id"/></xsl:attribute>
            </cdao:belongs_to_Tree>
            <xsl:if test="@otu">
                <cdao:represents_TU>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of select="@otu"/></xsl:attribute>
                </cdao:represents_TU>
            </xsl:if>
            <xsl:if test="../nex:node[ @root = 'true']">
                <xsl:call-template name="processancestor">
                    <xsl:with-param name="cnodeid" select="@id"/>
                </xsl:call-template>
            </xsl:if>
        </rdf:Description>
    </xsl:template>
    <!-- Create a Label -->
    <xsl:template name="label">
        <rdfs:label>
            <xsl:value-of select="@label"/>
        </rdfs:label>
    </xsl:template>
    <!-- Create an Edge -->
    <xsl:template match="nex:edge">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="@id"
                /></xsl:attribute>
            <!--  Process directed edges and plain edges differently -->
            <xsl:choose>
                <!-- if there is a root node then the edges should be directed -->
                <xsl:when test="../nex:node/@root = 'true'">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#DirectedEdge"/>
                    <cdao:has_Parent_Node >
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@source"
                        /></xsl:attribute>
                    </cdao:has_Parent_Node>
                    <cdao:has_Child_Node>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@target"
                        /></xsl:attribute>
                    </cdao:has_Child_Node>
                </xsl:when>
                <xsl:otherwise>
                    <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#Edge"/>
                    <cdao:has_Node>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@source"
                        /></xsl:attribute>
                    </cdao:has_Node>
                    <cdao:has_Node>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@target"
                        /></xsl:attribute>
                    </cdao:has_Node>
                </xsl:otherwise>
            </xsl:choose>

            <cdao:belongs_to_Tree>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="../@id"/></xsl:attribute>
            </cdao:belongs_to_Tree>
            
            <!-- Process the length annotations. -->
            <xsl:if test="@length">
                <cdao:has_Annotation>
                    <rdf:Description>
                        <rdf:type
                            rdf:resource="http://www.evolutionaryontology.org/cdao.owl#EdgeLength"/>
                        <xsl:choose>
                            <xsl:when
                                test="concat(namespace::*[.='http://www.nexml.org/1.0' and name(.)], ':FloatTree')">
                                <cdao:has_Float_Value
                                    rdf:datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#float">
                                    <xsl:value-of select="@length"/>
                                </cdao:has_Float_Value>
                            </xsl:when>
                            <xsl:when
                                test="concat(namespace::*[.='http://www.nexml.org/1.0' and name(.)], ':IntTree')">
                                <cdao:has_Int_Value
                                    rdf:datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#integer">
                                    <xsl:value-of select="@length"/>
                                </cdao:has_Int_Value>
                            </xsl:when>
                            <xsl:otherwise>
                                <cdao:has_Value>
                                    <xsl:value-of select="@length"/>
                                </cdao:has_Value>
                            </xsl:otherwise>
                        </xsl:choose>
                    </rdf:Description>
                </cdao:has_Annotation>
            </xsl:if>
        </rdf:Description>
    </xsl:template>
    <!-- Process the characters -->
    <xsl:template match="nex:char">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:choose>
                <xsl:when
                    test="contains(../../@xsi:type, 'ContinuousCells') or contains(../../@xsi:type , 'ContinuousSeqs')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#ContinuousCharacter"
                    />
                </xsl:when>
                <xsl:when
                    test="contains(../../@xsi:type , 'StandardCells') or contains(../../@xsi:type , 'StandardSeqs')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#StandardCharacter"
                    />
                </xsl:when>
                <xsl:when
                    test="contains(../../@xsi:type , 'DnaSeqs') or contains(../../@xsi:type , 'DnaCells')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#NucleotideResidueCharacter"
                    />
                </xsl:when>
                <xsl:when
                    test="contains(../../@xsi:type , 'RnaSeqs') or contains(../../@xsi:type , 'RnaCells')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#RNAResidueCharacter"
                    />
                </xsl:when>
                <xsl:otherwise>
                    <rdf:type rdf:resource="http://www.evolutionaryontology.org/cdao.owl#Character"
                    />
                </xsl:otherwise>
            </xsl:choose>
        </rdf:Description>
    </xsl:template>
    <!-- Process an individual matrix cell. -->
    <xsl:template name="processcell">
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="contains(../../../@xsi:type, 'ContinuousCells')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#ContinuousStateDatum"
                    />
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'StandardCells')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#StandardStateDatum"
                    />
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'DnaSeqs')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#NucleotideStateDatum"
                    />
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'RnaSeqs')">
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#RNAResidueStateDatum"
                    />
                </xsl:when>
                <xsl:otherwise>
                    <rdf:type
                        rdf:resource="http://www.evolutionaryontology.org/cdao.owl#CharacterStateDatum"
                    />
                </xsl:otherwise>
            </xsl:choose>
            <cdao:belongs_to_TU>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="../@otu"/></xsl:attribute>
            </cdao:belongs_to_TU>
            <cdao:belongs_to_Character>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="@char"/></xsl:attribute>
            </cdao:belongs_to_Character>
            <xsl:choose>
                <xsl:when test="contains(../../../@xsi:type, 'ContinuousCells')">
                    <cdao:has_Continuous_State>
                        <cdao:Continuous>
                            <cdao:has_Value>
                                <xsl:value-of select="@state"/>
                            </cdao:has_Value>
                        </cdao:Continuous>
                    </cdao:has_Continuous_State>
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'StandardCells')">
                    <cdao:has_Standard_State>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                    </cdao:has_Standard_State>
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'DnaSeqs')">
                    <cdao:has_Nucleotide_State>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                    </cdao:has_Nucleotide_State>
                </xsl:when>
                <xsl:when test="contains(../../../@xsi:type, 'RnaSeqs')">
                    <cdao:has_RNA_State>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                    </cdao:has_RNA_State>
                </xsl:when>
                <xsl:otherwise>
                    <cdao:has_State>
                        <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                    </cdao:has_State>
                </xsl:otherwise>
            </xsl:choose>
        </rdf:Description>
    </xsl:template>
    <!-- Process a matrix row. -->
    <xsl:template name="processrow">
        <xsl:for-each select="nex:cell">
            <xsl:call-template name="processcell"/>
        </xsl:for-each>
    </xsl:template>
    <!-- Process a matrix. -->
    <xsl:template match="nex:matrix">
        <xsl:variable name="charactersid" select="../@id"/>
        <rdf:Description>
            <xsl:attribute name="rdf:ID"><xsl:value-of select="$charactersid"
                /></xsl:attribute>
            <rdf:type
                rdf:resource="http://www.evolutionaryontology.org/cdao.owl#CharacterStateDataMatrix"/>
            <xsl:for-each select="../nex:format/nex:char">
                <cdao:has_Character>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of select="@id"/></xsl:attribute>
                </cdao:has_Character>
            </xsl:for-each>
            <xsl:for-each select="//nex:otus/nex:otu">
                <cdao:has_TU>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of select="@id"/></xsl:attribute>
                </cdao:has_TU>
            </xsl:for-each>
        </rdf:Description>
        <xsl:for-each select="nex:row">
            <xsl:call-template name="processrow"/>
        </xsl:for-each>
    </xsl:template>
    <!-- Make up state classes-->
    <xsl:template name="makestate">
        <xsl:param name="classname"/>
        <xsl:param name="individualid"/>
        <xsl:param name="symbol"/>
        <rdf:Description>
            <xsl:attribute name="rdf:about">#<xsl:value-of
                    select="$individualid"/></xsl:attribute>
            <rdf:type>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="$classname"
                    /></xsl:attribute>
            </rdf:type>
            <rdfs:label>
                <xsl:value-of select="$symbol"/>
            </rdfs:label>
        </rdf:Description>

    </xsl:template>
    <!-- Create polymorphic states. -->
    <xsl:template name="makepolymorhicstateset">
        <xsl:param name="classname"/>
        <xsl:param name="individualid"/>
        <xsl:param name="symbol"/>

        <rdf:Description>
            <xsl:attribute name="rdf:about">#<xsl:value-of
                    select="$individualid"/></xsl:attribute>
            <rdf:type>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="$classname"
                    /></xsl:attribute>
            </rdf:type>
            <rdfs:label>
                <xsl:value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type
                rdf:resource="http://www.evolutionaryontology.org/cdao.owl#PolymorphicStateDomain"/>
            <xsl:for-each select="member">
                <cdao:has>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                </cdao:has>
            </xsl:for-each>
        </rdf:Description>


    </xsl:template>
    <!-- Create uncertain states.-->
    <xsl:template name="makeuncertainstateset">
        <xsl:param name="classname"/>
        <xsl:param name="individualid"/>
        <xsl:param name="symbol"/>

        <rdf:Description>
            <xsl:attribute name="rdf:about">#<xsl:value-of
                    select="$individualid"/></xsl:attribute>
            <rdf:type>
                <xsl:attribute name="rdf:resource">#<xsl:value-of select="$classname"
                    /></xsl:attribute>
            </rdf:type>
            <rdfs:label>
                <xsl:value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type
                rdf:resource="http://www.evolutionaryontology.org/cdao.owl#UncertainStateDomain"/>
            <xsl:for-each select="member">
                <cdao:has>
                    <xsl:attribute name="rdf:resource">#<xsl:value-of select="@state"/></xsl:attribute>
                </cdao:has>
            </xsl:for-each>
        </rdf:Description>
    </xsl:template>
    <!-- Process state definitions. -->
    <xsl:template match="nex:states">
        <xsl:variable name="otus" select="../../@otus"/>
        <xsl:variable name="statesid" select="@id"/>
        <xsl:variable name="classname" select="$statesid"/>
        <owl:Class>
            <xsl:attribute name="rdf:about">#<xsl:value-of select="$classname"/></xsl:attribute>
            <rdfs:subClassOf rdf:resource="http://www.evolutionaryontology.org/cdao.owl#Standard"/>
        </owl:Class>
        <xsl:for-each select="nex:state">
            <xsl:call-template name="makestate">
                <xsl:with-param name="classname" select="$classname"/>
                <xsl:with-param name="individualid" select="@id"/>
                <xsl:with-param name="symbol" select="@symbol"/>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="nex:polymorphic_state_set">
            <xsl:call-template name="makepolymorhicstateset">
                <xsl:with-param name="classname" select="$classname"/>
                <xsl:with-param name="individualid" select="@id"/>
                <xsl:with-param name="symbol" select="@symbol"/>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="nex:uncertain_state_set">
            <xsl:call-template name="makeuncertainstateset">
                <xsl:with-param name="classname" select="$classname"/>
                <xsl:with-param name="individualid" select="@id"/>
                <xsl:with-param name="symbol" select="@symbol"/>
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>
    <!-- Do nothing with the things we aren't interested in processing. -->
    <xsl:template match="*" priority="-1"/>

</xsl:stylesheet>
