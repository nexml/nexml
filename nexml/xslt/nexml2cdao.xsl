<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE stylesheet [
<!ENTITY cdao_ns "http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#">
]>
<stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:nex="http://www.nexml.org/2009" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:cdao="&cdao_ns;"
    xmlns="http://www.w3.org/1999/XSL/Transform"
    xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <output indent="yes" method="xml" media-type="application/rdf+xml" encoding="UTF-8" omit-xml-declaration="yes"/>

    <!-- writes out the rdf:resource attribute of an idreferenced element -->
    <template name="compute-uri-resource-by-idref">
        <param name="idref"/>
        <for-each select="//*">
            <if test="contains(@id,$idref)">
                <call-template name="compute-uri-resource"/>
            </if>
        </for-each>
    </template>
    
    <!-- Process default @id and @label attributes -->
    <!-- TODO: add metadata annotation processing here -->
    <template name="defaults">
        <call-template name="compute-uri-about"/>        
        <if test="@label">
            <rdfs:label>
                <value-of select="@label"/>
            </rdfs:label>
        </if>        
    </template>
    
    <!-- this creates an rdf:about attribute composed of @xml:base (found recursively) and the focal @id -->
    <template name="compute-uri-about">
        <param name="id" select="@id"/>       
        <choose>
            <when test="@xml:base">
                <attribute name="rdf:about">
                    <value-of select="@xml:base"/>
                    <value-of select="$id"/>
                </attribute>
            </when>
            <otherwise>
                <for-each select="../.">
                    <call-template name="compute-uri-about">
                        <with-param name="id" select="$id"/>
                    </call-template>
                </for-each>
            </otherwise>
        </choose>        
    </template>
    
    <!-- this creates an rdf:resource attribute composed of @xml:base (found recursively) and the focal @id -->
    <template name="compute-uri-resource">
        <param name="id" select="@id"/>       
        <choose>
            <when test="@xml:base">
                <attribute name="rdf:resource">
                    <value-of select="@xml:base"/>
                    <value-of select="$id"/>
                </attribute>
            </when>
            <otherwise>
                <for-each select="../.">
                    <call-template name="compute-uri-resource">
                        <with-param name="id" select="$id"/>
                    </call-template>
                </for-each>
            </otherwise>
        </choose>      
    </template>     

    <!-- Boiler-Plate  -->

    <!-- Make the new root element -->
    <template match="/nex:nexml">
        <rdf:RDF xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
            xmlns:owl="http://www.w3.org/2002/07/owl#">
            <apply-templates select="descendant::*"/>
        </rdf:RDF>
    </template>

    <!-- Create the TU's  -->
    <template match="nex:otu">
        <rdf:Description>
            <call-template name="defaults"/>            
            <rdf:type rdf:resource="&cdao_ns;TU"/>
        </rdf:Description>
    </template>

    <!-- Create the Trees -->
    <template match="nex:tree">
        <rdf:Description>
            <call-template name="defaults"/>
            <choose>
                <when test="nex:node/@root = 'true'">
                    <rdf:type rdf:resource="&cdao_ns;RootedTree"/>
                    <cdao:has_Root>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="nex:node[@root = 'true']/@id"/>
                        </call-template>
                    </cdao:has_Root>
                </when>
                <otherwise>
                    <rdf:type rdf:resource="&cdao_ns;UnrootedTree"/>
                </otherwise>
            </choose>
        </rdf:Description>
    </template>
    
    <!-- Create a Network  -->
    <template match="nex:network">
        <rdf:Description>
            <call-template name="defaults"/>
            <rdf:type rdf:resource="&cdao_ns;Network"/>
        </rdf:Description>
    </template>
    
    <!-- Process an ancestor -->
    <template name="processancestor">
        <param name="cnodeid"/>
        <variable name="treeid" select="../@id"/>
        <choose>
            <when test="../nex:node[@id = $cnodeid and @root = 'true' ]"> </when>
            <when test="../nex:node[ @id = $cnodeid ]">
                <variable name="edge" select="../nex:edge[ @target = $cnodeid]"/>
                <cdao:has_Ancestor>
                    <call-template name="compute-uri-resource-by-idref">
                        <with-param name="idref" select="$edge/@source"/>
                    </call-template>                    
                </cdao:has_Ancestor>
                <call-template name="processancestor">
                    <with-param name="cnodeid" select="$edge/@source"/>
                </call-template>
            </when>
            <otherwise> </otherwise>
        </choose>
    </template>
    
    <!-- Process a Node -->
    <template match="nex:node">
        <rdf:Description>
            <call-template name="defaults"/>
            <rdf:type rdf:resource="&cdao_ns;Node"/>
            <cdao:belongs_to_Tree>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="../@id"/>
                </call-template> 
            </cdao:belongs_to_Tree>
            <if test="@otu">
                <cdao:represents_TU>
                    <call-template name="compute-uri-resource-by-idref">
                        <with-param name="idref" select="@otu"/>
                    </call-template>
                </cdao:represents_TU>
            </if>
            <if test="../nex:node[ @root = 'true']">
                <call-template name="processancestor">
                    <with-param name="cnodeid" select="@id"/>
                </call-template>
            </if>
        </rdf:Description>
    </template>         
    
    <!-- Create an Edge -->
    <template match="nex:edge">
        <rdf:Description>
            <call-template name="defaults"/>
            <!--  Process directed edges and plain edges differently -->
            <choose>
                <!-- if there is a root node then the edges should be directed -->
                <when test="../nex:node/@root = 'true'">
                    <rdf:type rdf:resource="&cdao_ns;DirectedEdge"/>
                    <cdao:has_Parent_Node >
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@source"/>
                        </call-template>
                    </cdao:has_Parent_Node>
                    <cdao:has_Child_Node>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@target"/>
                        </call-template>
                    </cdao:has_Child_Node>
                </when>
                <otherwise>
                    <rdf:type rdf:resource="&cdao_ns;Edge"/>
                    <cdao:has_Node>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@source"/>
                        </call-template>
                    </cdao:has_Node>
                    <cdao:has_Node>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@target"/>
                        </call-template>
                    </cdao:has_Node>
                </otherwise>
            </choose>

            <cdao:belongs_to_Tree>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="../@id"/>
                </call-template>
            </cdao:belongs_to_Tree>
            
            <!-- Process the length annotations. -->
            <if test="@length">
                <cdao:has_Annotation>
                    <rdf:Description>
                        <rdf:type rdf:resource="&cdao_ns;EdgeLength"/>
                        <choose>
                            <when test="concat(namespace::*[.='http://www.nexml.org/2009' and name(.)], ':FloatTree')">
                                <cdao:has_Float_Value rdf:datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#float">
                                    <value-of select="@length"/>
                                </cdao:has_Float_Value>
                            </when>
                            <when test="concat(namespace::*[.='http://www.nexml.org/2009' and name(.)], ':IntTree')">
                                <cdao:has_Int_Value rdf:datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#integer">
                                    <value-of select="@length"/>
                                </cdao:has_Int_Value>
                            </when>
                            <otherwise>
                                <cdao:has_Value>
                                    <value-of select="@length"/>
                                </cdao:has_Value>
                            </otherwise>
                        </choose>
                    </rdf:Description>
                </cdao:has_Annotation>
            </if>
        </rdf:Description>
    </template>
    
    <!-- Process the characters -->
    <template match="nex:char">
        <rdf:Description>
            <call-template name="defaults"/>
            <choose>
                <when test="contains(../../@xsi:type, 'ContinuousCells') or contains(../../@xsi:type , 'ContinuousSeqs')">
                    <rdf:type rdf:resource="&cdao_ns;ContinuousCharacter"/>
                </when>
                <when test="contains(../../@xsi:type , 'StandardCells') or contains(../../@xsi:type , 'StandardSeqs')">
                    <rdf:type rdf:resource="&cdao_ns;StandardCharacter"/>
                </when>
                <when test="contains(../../@xsi:type , 'DnaSeqs') or contains(../../@xsi:type , 'DnaCells')">
                    <rdf:type rdf:resource="&cdao_ns;NucleotideResidueCharacter"/>
                </when>
                <when test="contains(../../@xsi:type , 'RnaSeqs') or contains(../../@xsi:type , 'RnaCells')">
                    <rdf:type rdf:resource="&cdao_ns;RNAResidueCharacter"/>
                </when>
                <otherwise>
                    <rdf:type rdf:resource="&cdao_ns;Character"/>
                </otherwise>
            </choose>
        </rdf:Description>
    </template>
    
    <!-- Process an individual matrix cell. -->
    <template name="processcell">
        <rdf:Description>
            <attribute name="rdf:ID"><value-of select="../@otu"/>-<value-of select="@char"/></attribute>
            <choose>
                <when test="contains(../../../@xsi:type, 'ContinuousCells')">
                    <rdf:type rdf:resource="&cdao_ns;ContinuousStateDatum"/>
                </when>
                <when test="contains(../../../@xsi:type, 'StandardCells')">
                    <rdf:type rdf:resource="&cdao_ns;StandardStateDatum"/>
                </when>
                <when test="contains(../../../@xsi:type, 'DnaSeqs')">
                    <rdf:type rdf:resource="&cdao_ns;NucleotideStateDatum"/>
                </when>
                <when test="contains(../../../@xsi:type, 'RnaSeqs')">
                    <rdf:type rdf:resource="&cdao_ns;RNAResidueStateDatum"/>
                </when>
                <otherwise>
                    <rdf:type rdf:resource="&cdao_ns;CharacterStateDatum"/>
                </otherwise>
            </choose>
            <cdao:belongs_to_TU>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="../@otu"/>
                </call-template>
            </cdao:belongs_to_TU>
            <cdao:belongs_to_Character>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="@char"/>
                </call-template>
            </cdao:belongs_to_Character>
            <choose>
                <when test="contains(../../../@xsi:type, 'ContinuousCells')">
                    <cdao:has_Continuous_State>
                        <cdao:Continuous>
                            <cdao:has_Value>
                                <value-of select="@state"/>
                            </cdao:has_Value>
                        </cdao:Continuous>
                    </cdao:has_Continuous_State>
                </when>
                <when test="contains(../../../@xsi:type, 'StandardCells')">
                    <cdao:has_Standard_State>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@state"/>
                        </call-template>
                    </cdao:has_Standard_State>
                </when>
                <when test="contains(../../../@xsi:type, 'DnaSeqs')">
                    <cdao:has_Nucleotide_State>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@state"/>
                        </call-template>
                    </cdao:has_Nucleotide_State>
                </when>
                <when test="contains(../../../@xsi:type, 'RnaSeqs')">
                    <cdao:has_RNA_State>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@state"/>
                        </call-template>
                    </cdao:has_RNA_State>
                </when>
                <otherwise>
                    <cdao:has_State>
                        <call-template name="compute-uri-resource-by-idref">
                            <with-param name="idref" select="@state"/>
                        </call-template>
                    </cdao:has_State>
                </otherwise>
            </choose>
        </rdf:Description>
    </template>
    
    <!-- Process a matrix row. -->
    <template name="processrow">
        <for-each select="nex:cell">
            <call-template name="processcell"/>
        </for-each>
    </template>
    
    <!-- Process a matrix. -->
    <template match="nex:matrix">
        <variable name="charactersid" select="../@id"/>
        <variable name="otusid" select="../@otus"/>
        <rdf:Description>
            <call-template name="compute-uri-resource-by-idref">
                <with-param name="idref" select="$charactersid"/>
            </call-template>
            <rdf:type rdf:resource="&cdao_ns;CharacterStateDataMatrix"/>
            <for-each select="../nex:format/nex:char">
                <cdao:has_Character>
                    <call-template name="compute-uri-resource-by-idref">
                        <with-param name="idref" select="@id"/>
                    </call-template>
                </cdao:has_Character>
            </for-each>
            
            <!-- first find the matching otus element, then iterate over its otu elements -->
            <for-each select="//nex:otus">
                <if test="contains(@id,$otusid)">
                    <for-each select="nex:otu">                    
                    <cdao:has_TU>
                        <call-template name="compute-uri-resource"/>
                    </cdao:has_TU>
                    </for-each>
                </if>
            </for-each>
            
        </rdf:Description>
        <for-each select="nex:row">
            <call-template name="processrow"/>
        </for-each>
    </template>
    
    <!-- Make up state classes-->
    <template name="makestate">
        <param name="classname"/>
        <param name="individualid"/>
        <param name="symbol"/>
        <rdf:Description>
            <attribute name="rdf:about">#<value-of
                    select="$individualid"/></attribute>
            <rdf:type>
                <attribute name="rdf:resource">#<value-of select="$classname"
                    /></attribute>
            </rdf:type>
            <rdfs:label>
                <value-of select="$symbol"/>
            </rdfs:label>
        </rdf:Description>

    </template>
    
    <!-- Create polymorphic states. -->
    <template name="makepolymorhicstateset">
        <param name="classname"/>
        <param name="individualid"/>
        <param name="symbol"/>

        <rdf:Description>
            <attribute name="rdf:about">#<value-of select="$individualid"/></attribute>
            <rdf:type>
                <attribute name="rdf:resource">#<value-of select="$classname"/></attribute>
            </rdf:type>
            <rdfs:label>
                <value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type rdf:resource="&cdao_ns;PolymorphicStateDomain"/>
            <for-each select="member">
                <cdao:has>
                    <attribute name="rdf:resource">#<value-of select="@state"/></attribute>
                </cdao:has>
            </for-each>
        </rdf:Description>

    </template>
    
    <!-- Create uncertain states.-->
    <template name="makeuncertainstateset">
        <param name="classname"/>
        <param name="individualid"/>
        <param name="symbol"/>

        <rdf:Description>
            <attribute name="rdf:about">#<value-of select="$individualid"/></attribute>
            <rdf:type>
                <attribute name="rdf:resource">#<value-of select="$classname"/></attribute>
            </rdf:type>
            <rdfs:label>
                <value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type rdf:resource="&cdao_ns;UncertainStateDomain"/>
            <for-each select="member">
                <cdao:has>
                    <attribute name="rdf:resource">#<value-of select="@state"/></attribute>
                </cdao:has>
            </for-each>
        </rdf:Description>
    </template>
    
    <!-- Process state definitions. -->
    <template match="nex:states">
        <variable name="otus" select="../../@otus"/>
        <variable name="statesid" select="@id"/>
        <variable name="classname" select="$statesid"/>
        <owl:Class>
            <attribute name="rdf:about">#<value-of select="$classname"/></attribute>
            <rdfs:subClassOf rdf:resource="&cdao_ns;Standard"/>
        </owl:Class>
        <for-each select="nex:state">
            <call-template name="makestate">
                <with-param name="classname" select="$classname"/>
                <with-param name="individualid" select="@id"/>
                <with-param name="symbol" select="@symbol"/>
            </call-template>
        </for-each>
        <for-each select="nex:polymorphic_state_set">
            <call-template name="makepolymorhicstateset">
                <with-param name="classname" select="$classname"/>
                <with-param name="individualid" select="@id"/>
                <with-param name="symbol" select="@symbol"/>
            </call-template>
        </for-each>
        <for-each select="nex:uncertain_state_set">
            <call-template name="makeuncertainstateset">
                <with-param name="classname" select="$classname"/>
                <with-param name="individualid" select="@id"/>
                <with-param name="symbol" select="@symbol"/>
            </call-template>
        </for-each>

    </template>
    
    <!-- Do nothing with the things we aren't interested in processing. -->
    <template match="*" priority="-1"/>

</stylesheet>
