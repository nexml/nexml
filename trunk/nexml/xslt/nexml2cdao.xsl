<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE stylesheet [
<!ENTITY cdao_ns "http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#">
]>
<stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:nex="http://www.nexml.org/2009" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:cdao="&cdao_ns;"    
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.w3.org/1999/XSL/Transform">
    
    <!-- PARTS OF THIS STYLESHEET WERE ADAPTED FROM RDFa2RDFXML.xsl, whose copyright statement is reproduced below -->
    <!-- Version 0.21 by Fabien.Gandon@sophia.inria.fr -->
    <!-- This software is distributed under either the CeCILL-C license or the GNU Lesser General Public License version 3 license. -->
    <!-- This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License -->
    <!-- as published by the Free Software Foundation version 3 of the License or under the terms of the CeCILL-C license. -->
    <!-- This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied -->
    <!-- warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. -->
    <!-- See the GNU Lesser General Public License version 3 at http://www.gnu.org/licenses/  -->
    <!-- and the CeCILL-C license at http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html for more details -->    
    
    <output indent="yes" method="xml" media-type="application/rdf+xml" encoding="UTF-8" omit-xml-declaration="yes"/>
    
    <!-- base of the current HTML doc -->
    <variable name="html_base"/>
    
    <!-- default HTML vocabulary namespace -->
    <variable name="default_voc" select="'http://www.w3.org/1999/xhtml/vocab#'"/>
    
    <!-- url of the current XHTML page if provided by the XSLT engine -->
    <param name="url" select="''"/>
    
    <!-- this contains the URL of the source document whether it was provided by the base or as a parameter e.g. http://example.org/bla/file.html-->
    <variable name="this">
        <choose>
            <when test="string-length($html_base)&gt;0"><value-of select="$html_base"/></when>
            <otherwise><value-of select="$url"/></otherwise>
        </choose>
    </variable>
    
    <!-- this_location contains the location the source document e.g. http://example.org/bla/ -->
    <variable name="this_location">
        <call-template name="get-location"><with-param name="url" select="$this"/></call-template>
    </variable>
    
    <!-- this_root contains the root location of the source document e.g. http://example.org/ -->
    <variable name="this_root">
        <call-template name="get-root"><with-param name="url" select="$this"/></call-template>
    </variable>    

    <!-- 
        NeXML elements reference other elements. For example, some node elements reference the id of
        otu elements. Given the value of that id attribute, this function creates an rdf:resource attribute.
        The value of the attribute becomes the concatenation of the base URI of the otu element (or its
        nearest ancestor) with the id attribute.
    -->
    <template name="compute-uri-resource-by-idref">
        <param name="idref"/>
        <for-each select="//*">
            <if test="@id = $idref">
                <call-template name="compute-uri-resource"/>
            </if>
        </for-each>
    </template>
    
    <!-- 
        NeXML elements have certain default attributes: a required id attribute and an optional label.
        This function turns the label attribute in an rdfs:label predicate and value, and turns the
        id attribute into an rdf:about attribute containing the concatenation of the base URI of the otu 
        element (or its nearest ancestor) with the id attribute.
    -->
    <template name="defaults">        
        <call-template name="compute-uri-about"/>        
        <if test="@label">
            <rdfs:label>
                <value-of select="@label"/>
            </rdfs:label>
        </if> 
    </template>
    
    <!-- 
        This creates an rdf:about attribute composed of @xml:base (found recursively) and the focal @id.     
    -->
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
                <for-each select="..">
                    <call-template name="compute-uri-about">
                        <with-param name="id" select="$id"/>
                    </call-template>
                </for-each>                
            </otherwise>
        </choose>        
    </template>
    
    <!-- 
        This creates an rdf:resource attribute composed of @xml:base (found recursively) and the focal @id 
    -->
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
                <for-each select="..">
                    <call-template name="compute-uri-resource">
                        <with-param name="id" select="$id"/>
                    </call-template>
                </for-each>
            </otherwise>
        </choose>      
    </template>     


    <!-- Make the new root element -->
    <template match="/nex:nexml">
        <rdf:RDF xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
            xmlns:owl="http://www.w3.org/2002/07/owl#">
            <apply-templates select="descendant::*"/>      
            <apply-templates mode="rdf2rdfxml"/>            
        </rdf:RDF>
    </template>
    
    <!-- 
        The rdf2rdfxml templates pass through text content. This causes problems in cases
        where core nexml (not rdfa annotations with literal text content) are encountered.
        Luckily, the only instance of this is in <seq> elements. Hence, we need to capture
        them here and do nothing so that they don't end up in the produced rdf
    -->        
    <template match="//nex:seq" mode="rdf2rdfxml"/>
    
    <!-- process on taxon/character/node/tree/etc sets -->
    <template match="nex:set">
        <variable name="idsraw">
            <value-of select="concat(@otu,' ',@otus,' ',@trees,' ',@tree,' ',@network,' ',@node,' ',@edge,' ',@rootedge,' ',@characters,' ',@states,' ',@state,' ',@polymorphic_state_set,' ',@uncertain_state_set,' ',@member,' ',@char,' ',@row,' ',@cell)"/>
        </variable>
        <variable name="ids" select="concat(normalize-space($idsraw),' ')"/>
        <rdf:Description>
            <call-template name="defaults"/>
            <cdao:SetOfThings>
                <rdf:Bag>
                    <call-template name="processsetmembers">
                        <with-param name="ids" select="$ids"/>
                    </call-template>
                </rdf:Bag>
            </cdao:SetOfThings>
        </rdf:Description>        
    </template>
    
    <!-- recurse through list of id reference to populate rdf:Bag of set members -->
    <template name="processsetmembers">
        <param name="ids"/>
        <variable name="id" select="substring-before($ids,' ')"/>
        <variable name="remainder" select="substring-after($ids,' ')"/>
        <rdf:li>
            <call-template name="compute-uri-resource-by-idref">
                <with-param name="idref" select="$id"/>
            </call-template>
        </rdf:li>        
        <if test="string-length($remainder) &gt; 0">
            <call-template name="processsetmembers">
                <with-param name="ids" select="$remainder"/>
            </call-template>
        </if>
    </template>
    
    <!-- match RDFa element -->
    <template match="*[attribute::property or attribute::rel or attribute::rev or attribute::typeof]" mode="rdf2rdfxml">
        
        <!-- identify suject -->
        <variable name="subject"><call-template name="subject"/></variable>
        
        
        <!-- do we have object properties? -->
        <if test="string-length(@rel)&gt;0 or string-length(@rev)&gt;0">
            <variable name="object"> <!-- identify the object(s) -->
                <choose>
                    <when test="@resource"> 
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="@resource"/></call-template>
                    </when>
                    <when test="@href"> 
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="@href"/></call-template>
                    </when>
                    <when test="descendant::*[attribute::about or attribute::src or attribute::typeof or         attribute::href or attribute::resource or         attribute::rel or attribute::rev or attribute::property]"> 
                        <call-template name="recurse-objects"/>
                    </when>
                    <otherwise>
                        <call-template name="self-curie-or-uri"><with-param name="node" select="."/></call-template>
                    </otherwise>
                </choose>
            </variable>
            
            <call-template name="relrev">
                <with-param name="subject" select="$subject"/>
                <with-param name="object" select="$object"/>
            </call-template>  
            
        </if>
        
        
        <!-- do we have data properties ? -->
        <if test="string-length(@property)&gt;0">
            
            <!-- identify language -->
            <variable name="language" select="string(ancestor-or-self::*/attribute::xml:lang[position()=1])"/>
            
            <variable name="expended-pro"><call-template name="expand-ns"><with-param name="qname" select="@property"/></call-template></variable>
            
            <choose>
                <when test="@content"> <!-- there is a specific content -->
                    <call-template name="property">
                        <with-param name="subject" select="$subject"/>
                        <with-param name="object" select="@content"/>
                        <with-param name="datatype">
                            <choose>
                                <when test="@datatype='' or not(@datatype)"/> <!-- enforcing plain literal -->
                                <otherwise><call-template name="expand-ns"><with-param name="qname" select="@datatype"/></call-template></otherwise>
                            </choose>
                        </with-param>
                        <with-param name="predicate" select="@property"/>
                        <with-param name="attrib" select="'true'"/>
                        <with-param name="language" select="$language"/>
                    </call-template>   
                </when>
                <when test="not(*)"> <!-- there no specific content but there are no children elements in the content -->
                    <call-template name="property">
                        <with-param name="subject" select="$subject"/>
                        <with-param name="object" select="."/>
                        <with-param name="datatype">
                            <choose>
                                <when test="@datatype='' or not(@datatype)"/> <!-- enforcing plain literal -->
                                <otherwise><call-template name="expand-ns"><with-param name="qname" select="@datatype"/></call-template></otherwise>
                            </choose>
                        </with-param>
                        <with-param name="predicate" select="@property"/>
                        <with-param name="attrib" select="'true'"/>
                        <with-param name="language" select="$language"/>
                    </call-template>   
                </when>
                <otherwise> <!-- there is no specific content; we use the value of element -->
                    <call-template name="property">
                        <with-param name="subject" select="$subject"/>
                        <with-param name="object" select="."/>
                        <with-param name="datatype">
                            <choose>
                                <when test="@datatype='' or not(@datatype)">http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral</when> <!-- enforcing XML literal -->
                                <otherwise><call-template name="expand-ns"><with-param name="qname" select="@datatype"/></call-template></otherwise>
                            </choose>
                        </with-param>
                        <with-param name="predicate" select="@property"/>
                        <with-param name="attrib" select="'false'"/>
                        <with-param name="language" select="$language"/>
                    </call-template> 
                </otherwise>
            </choose>
        </if>
        
        <!-- do we have classes ? -->
        <if test="@typeof">
            <call-template name="class">
                <with-param name="resource"><call-template name="self-curie-or-uri"><with-param name="node" select="."/></call-template></with-param>
                <with-param name="class" select="@typeof"/>
            </call-template>
        </if>
        
        <apply-templates mode="rdf2rdfxml"/> 
        
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
    
    <!-- 
        Process an individual matrix "cell", this works for either real cell elements or ones we've parsed out of seq
        elements, provided we have looked up the correct char element by its index and pass in the @id, and looked up
        the correct state/uncertain_state_set/polymorphic_state_set by its @symbol and pass in its @id (the latter does
        not apply to continuous values, for which we can pass in the raw float).        
    -->
    <template name="processcell">
        <param name="char" select="@char"/>
        <param name="state" select="@state"/>
        <param name="type" select="../../../@xsi:type"/>        
        <rdf:Description>
            <attribute name="rdf:ID"><value-of select="../@otu"/>-<value-of select="$char"/></attribute>
            <choose>
                <when test="contains($type, 'Continuous')"><rdf:type rdf:resource="&cdao_ns;ContinuousStateDatum"/></when>
                <when test="contains($type, 'Standard')"><rdf:type rdf:resource="&cdao_ns;StandardStateDatum"/></when>
                <when test="contains($type, 'Dna')"><rdf:type rdf:resource="&cdao_ns;NucleotideStateDatum"/></when>
                <when test="contains($type, 'Rna')"><rdf:type rdf:resource="&cdao_ns;RNAResidueStateDatum"/></when>
                <otherwise><rdf:type rdf:resource="&cdao_ns;CharacterStateDatum"/></otherwise>
            </choose>
            <cdao:belongs_to_TU>
                <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="../@otu"/></call-template>
            </cdao:belongs_to_TU>
            <cdao:belongs_to_Character>
                <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="$char"/></call-template>
            </cdao:belongs_to_Character>
            <choose>
                <when test="contains($type, 'Continuous')">
                    <cdao:has_Continuous_State>
                        <cdao:Continuous><cdao:has_Value><value-of select="$state"/></cdao:has_Value></cdao:Continuous>
                    </cdao:has_Continuous_State>
                </when>
                <when test="contains($type, 'Standard')">
                    <cdao:has_Standard_State>
                        <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="$state"/></call-template>
                    </cdao:has_Standard_State>
                </when>
                <when test="contains($type, 'Dna')">
                    <cdao:has_Nucleotide_State>
                        <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="$state"/></call-template>
                    </cdao:has_Nucleotide_State>
                </when>
                <when test="contains($type, 'Rna')">
                    <cdao:has_RNA_State>
                        <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="$state"/></call-template>
                    </cdao:has_RNA_State>
                </when>
                <otherwise>
                    <cdao:has_State>
                        <call-template name="compute-uri-resource-by-idref"><with-param name="idref" select="$state"/></call-template>
                    </cdao:has_State>
                </otherwise>
            </choose>
        </rdf:Description>
    </template>
    
    <!-- Process a matrix row. -->
    <template name="processrow">
        <param name="type" select="../../@xsi:type"/>
        <for-each select="nex:cell">
            <call-template name="processcell"/>
        </for-each>
        <for-each select="nex:seq">
            <variable name="rawseqdata" select="text()"/>
            <variable name="seqdata">
                <choose>
                    <when test="contains($type,'Dna') or contains($type,'Rna') or contains($type,'Protein') or contains($type,'Restriction')">
                        <value-of select="translate($rawseqdata,' ','')"/>
                    </when>
                    <when test="contains($type,'Continuous') or contains($type,'Standard')">
                        <value-of select="concat(normalize-space($rawseqdata),' ')"/>
                    </when>
                </choose>                
            </variable>
            <call-template name="processseq">
                <with-param name="seqdata" select="$seqdata"/>
                <with-param name="type" select="$type"/>
                <with-param name="index" select="1"/>
            </call-template>
        </for-each>
    </template>
    
    <!-- process a row seq -->
    <template name="processseq">
        <param name="seqdata"/>
        <param name="type"/>
        <param name="index"/>
        
        <!-- get the raw "cell" value, i.e. the substring that's either 1 character or the first white-space separated token -->
        <variable name="cellvalue">
            <choose>
                <when test="contains($type,'Dna') or contains($type,'Rna') or contains($type,'Protein') or contains($type,'Restriction')">
                    <value-of select="substring($seqdata,1,1)"/>
                </when>
                <when test="contains($type,'Continuous') or contains($type,'Standard')">
                    <value-of select="substring-before($seqdata,' ')"/>
                </when>
            </choose>                
        </variable>
        
        <!-- get the remaining string, i.e. the substring after the first character or after the first white space -->
        <variable name="remainder">
            <choose>
                <when test="contains($type,'Dna') or contains($type,'Rna') or contains($type,'Protein') or contains($type,'Restriction')">
                    <value-of select="substring($seqdata,2)"/>
                </when>
                <when test="contains($type,'Continuous') or contains($type,'Standard')">
                    <value-of select="substring-after($seqdata,' ')"/>
                </when>
            </choose>                
        </variable> 
        
        <!-- look up the state @id by its symbol, or use the raw number for continuous values -->
        <variable name="state">
            <choose>
                <when test="contains($type,'Dna') or contains($type,'Rna') or contains($type,'Protein') or contains($type,'Restriction') or contains($type,'Standard')">
                    <for-each select="../../../nex:format/nex:states/*">
                        <if test="@symbol = $cellvalue">
                            <value-of select="@id"/>
                        </if>
                    </for-each>
                </when>
                <when test="contains($type,'Continuous')">
                    <value-of select="$cellvalue"/>
                </when>
            </choose>            
        </variable>     
        
        <!-- process the virtual "cell", given the correct char @id and state @id -->
        <call-template name="processcell">
            <with-param name="type" select="$type"/>
            <with-param name="char" select="../../../nex:format/nex:char[$index]/@id"/>
            <with-param name="state" select="$state"/>
        </call-template> 

        <!-- now continue with the remainder if there's any left -->
        <if test="string-length($remainder) &gt; 0">
            <call-template name="processseq">
                <with-param name="type" select="$type"/>
                <with-param name="seqdata" select="$remainder"/>
                <with-param name="index" select="$index+1"/>
            </call-template>  
        </if>            
    </template>   
    
    <!-- Process a matrix. -->
    <template match="nex:matrix">
        <variable name="charactersid" select="../@id"/>
        <variable name="otusid" select="../@otus"/>
        <rdf:Description>
            <call-template name="compute-uri-about"><with-param name="id" select="$charactersid"></with-param></call-template>
            <if test="../@label">
                <rdfs:label>
                    <value-of select="../@label"/>
                </rdfs:label>
            </if>
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
            <call-template name="defaults"/>
            <rdf:type>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="$classname"></with-param>
                </call-template>
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
            <call-template name="defaults"/>
            <rdf:type>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="$classname"></with-param>
                </call-template>
            </rdf:type>
            <rdfs:label>
                <value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type rdf:resource="&cdao_ns;PolymorphicStateDomain"/>
            <for-each select="nex:member">
                <cdao:has>
                    <call-template name="compute-uri-resource-by-idref">
                        <with-param name="idref" select="@state"></with-param>
                    </call-template>
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
            <call-template name="defaults"/>
            <rdf:type>
                <call-template name="compute-uri-resource-by-idref">
                    <with-param name="idref" select="$classname"></with-param>
                </call-template>
            </rdf:type>
            <rdfs:label>
                <value-of select="$symbol"/>
            </rdfs:label>
            <rdf:type rdf:resource="&cdao_ns;UncertainStateDomain"/>
            <for-each select="nex:member">
                <cdao:has>
                    <call-template name="compute-uri-resource-by-idref">
                        <with-param name="idref" select="@state"></with-param>
                    </call-template>
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
            <call-template name="defaults"/>
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
    
    <!-- tokenize a string using space as a delimiter -->
    <template name="tokenize">
        <param name="string"/>
        <if test="string-length($string)&gt;0">
            <choose>
                <when test="contains($string,' ')">
                    <value-of select="normalize-space(substring-before($string,' '))"/>
                    <call-template name="tokenize"><with-param name="string" select="normalize-space(substring-after($string,' '))"/></call-template>  	  				
                </when>
                <otherwise><value-of select="$string"/></otherwise>
            </choose>
        </if>
    </template>

    <!-- get file location from URL -->
    <template name="get-location">
        <param name="url"/>
        <if test="string-length($url)&gt;0 and contains($url,'/')">
            <value-of select="concat(substring-before($url,'/'),'/')"/>
            <call-template name="get-location"><with-param name="url" select="substring-after($url,'/')"/></call-template>
        </if>
    </template>
    
    <!-- get root location from URL -->
    <template name="get-root">
        <param name="url"/>
        <choose>
            <when test="contains($url,'//')">
                <value-of select="concat(substring-before($url,'//'),'//',substring-before(substring-after($url,'//'),'/'),'/')"/>
            </when>
            <otherwise>UNKNOWN ROOT</otherwise>
        </choose>    
    </template>

    <!-- return namespace of a qname -->
    <template name="return-ns">
        <param name="qname"/>
        <variable name="ns_prefix" select="substring-before($qname,':')"/>
        <if test="string-length($ns_prefix)&gt;0"> <!-- prefix must be explicit -->
            <variable name="name" select="substring-after($qname,':')"/>
            <value-of select="ancestor-or-self::*/namespace::*[name()=$ns_prefix][position()=1]"/>
        </if>
        <if test="string-length($ns_prefix)=0 and ancestor-or-self::*/namespace::*[name()=''][position()=1]"> <!-- no prefix -->
            <variable name="name" select="substring-after($qname,':')"/>
            <value-of select="ancestor-or-self::*/namespace::*[name()=''][position()=1]"/>
        </if>
    </template>
    
    <!-- expand namespace of a qname -->
    <template name="expand-ns">
        <param name="qname"/>
        <variable name="ns_prefix" select="substring-before($qname,':')"/>
        <if test="string-length($ns_prefix)&gt;0"> <!-- prefix must be explicit -->
            <variable name="name" select="substring-after($qname,':')"/>
            <variable name="ns_uri" select="ancestor-or-self::*/namespace::*[name()=$ns_prefix][position()=1]"/>
            <value-of select="concat($ns_uri,$name)"/>
        </if>
        <if test="string-length($ns_prefix)=0 and ancestor-or-self::*/namespace::*[name()=''][position()=1]"> <!-- no prefix -->
            <variable name="name" select="substring-after($qname,':')"/>
            <variable name="ns_uri" select="ancestor-or-self::*/namespace::*[name()=''][position()=1]"/>
            <value-of select="concat($ns_uri,$name)"/>
        </if>
    </template>

    <!-- determines the CURIE / URI of a node -->
    <template name="self-curie-or-uri">
        <param name="node"/>
        <choose>
            <when test="$node/attribute::about"> <!-- we have an about attribute to extend -->
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$node/attribute::about"/></call-template>
            </when>
            <when test="$node/attribute::src"> <!-- we have an src attribute to extend -->
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$node/attribute::src"/></call-template>
            </when>
            <when test="$node/attribute::resource and not($node/attribute::rel or $node/attribute::rev)"> <!-- enforcing the resource as subject if no rel or rev -->
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$node/attribute::resource"/></call-template>
            </when>
            <when test="$node/attribute::href and not($node/attribute::rel or $node/attribute::rev)"> <!-- enforcing the href as subject if no rel or rev -->
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$node/attribute::href"/></call-template>
            </when>
            <!--when test="$node/self::h:head or $node/self::h:body or $node/self::h:html"><value-of select="$this"/></when> <!-//- enforcing the doc as subject -->     
            <when test="$node/attribute::id"> <!-- we have an id attribute to extend -->
                <value-of select="concat($this,'#',$node/attribute::id)"/>
            </when>
            <otherwise>blank:node:<value-of select="generate-id($node)"/></otherwise>
        </choose>
    </template> 
    
    <!-- expand CURIE / URI -->
    <template name="expand-curie-or-uri">
        <param name="curie_or_uri"/>
        <choose>
            <when test="starts-with($curie_or_uri,'[_:')"> <!-- we have a CURIE blank node -->
                <value-of select="concat('blank:node:',substring-after(substring-before($curie_or_uri,']'),'[_:'))"/>
            </when>
            <when test="starts-with($curie_or_uri,'[')"> <!-- we have a CURIE between square brackets -->
                <call-template name="expand-ns"><with-param name="qname" select="substring-after(substring-before($curie_or_uri,']'),'[')"/></call-template>
            </when>
            <when test="starts-with($curie_or_uri,'#')"> <!-- we have an anchor -->
                <value-of select="concat($this,$curie_or_uri)"/>
            </when>
            <when test="string-length($curie_or_uri)=0"> <!-- empty anchor means the document itself -->
                <value-of select="$this"/>
            </when>
            <when test="not(starts-with($curie_or_uri,'[')) and contains($curie_or_uri,':')"> <!-- it is a URI -->
                <value-of select="$curie_or_uri"/>
            </when>     
            <when test="not(contains($curie_or_uri,'://')) and not(starts-with($curie_or_uri,'/'))"> <!-- relative URL -->
                <value-of select="concat($this_location,$curie_or_uri)"/>
            </when>
            <when test="not(contains($curie_or_uri,'://')) and (starts-with($curie_or_uri,'/'))"> <!-- URL from root domain -->
                <value-of select="concat($this_root,substring-after($curie_or_uri,'/'))"/>
            </when>
            <otherwise>UNKNOWN CURIE URI</otherwise>
        </choose>
    </template>      
    
    <!-- returns the first token in a list separated by spaces -->
    <template name="get-first-token">
        <param name="tokens"/>
        <if test="string-length($tokens)&gt;0">
            <choose>
                <when test="contains($tokens,' ')">
                    <value-of select="normalize-space(substring-before($tokens,' '))"/>			
                </when>
                <otherwise><value-of select="$tokens"/></otherwise>
            </choose>
        </if>
    </template>

    <!-- returns the namespace for an object property -->
    <template name="get-relrev-ns">
        <param name="qname"/>
        <variable name="ns_prefix" select="substring-before(translate($qname,'[]',''),':')"/>
        <choose>
            <when test="string-length($ns_prefix)&gt;0">
                <call-template name="return-ns"><with-param name="qname" select="$qname"/></call-template>
            </when>
            <!-- returns default_voc if the predicate is a reserved value -->
            <otherwise>
                <variable name="is-reserved"><call-template name="check-reserved"><with-param name="nonprefixed"><call-template name="no-leading-colon"><with-param name="name" select="$qname"/></call-template></with-param></call-template></variable>
                <if test="$is-reserved='true'"><value-of select="$default_voc"/></if>
            </otherwise>
        </choose>
    </template>
    
    <!-- returns the namespace for a data property -->
    <template name="get-property-ns">
        <param name="qname"/>
        <variable name="ns_prefix" select="substring-before(translate($qname,'[]',''),':')"/>
        <choose>
            <when test="string-length($ns_prefix)&gt;0">
                <call-template name="return-ns"><with-param name="qname" select="$qname"/></call-template>
            </when>
            <!-- returns default_voc otherwise -->
            <otherwise><value-of select="$default_voc"/></otherwise>
        </choose>
    </template>

    <!-- returns the qname for a predicate -->
    <template name="get-predicate-name">
        <param name="qname"/>
        <variable name="clean_name" select="translate($qname,'[]','')"/>
        <call-template name="no-leading-colon"><with-param name="name" select="$clean_name"/></call-template>
    </template>
    
    <!-- no leading colon -->
    <template name="no-leading-colon">
        <param name="name"/>
        <choose>
            <when test="starts-with($name,':')"> <!-- remove leading colons -->
                <value-of select="substring-after($name,':')"/>
            </when>
            <otherwise><value-of select="$name"/></otherwise>
        </choose>
    </template>

    <!-- check if a predicate is reserved -->
    <template name="check-reserved">
        <param name="nonprefixed"/>
        <choose>
            <when test="$nonprefixed='alternate' or $nonprefixed='appendix' or $nonprefixed='bookmark' or $nonprefixed='cite'">true</when>
            <when test="$nonprefixed='chapter' or $nonprefixed='contents' or $nonprefixed='copyright' or $nonprefixed='first'">true</when>
            <when test="$nonprefixed='glossary' or $nonprefixed='help' or $nonprefixed='icon' or $nonprefixed='index'">true</when>
            <when test="$nonprefixed='last' or $nonprefixed='license' or $nonprefixed='meta' or $nonprefixed='next'">true</when>
            <when test="$nonprefixed='p3pv1' or $nonprefixed='prev' or $nonprefixed='role' or $nonprefixed='section'">true</when>
            <when test="$nonprefixed='stylesheet' or $nonprefixed='subsection' or $nonprefixed='start' or $nonprefixed='top'">true</when>
            <when test="$nonprefixed='up'">true</when>
            <when test="$nonprefixed='made' or $nonprefixed='previous' or $nonprefixed='search'">true</when>  <!-- added because they are frequent -->
            <otherwise>false</otherwise>
        </choose>
    </template>
    
    <template name="recursive-copy"> <!-- full copy -->
        <copy><for-each select="node()|attribute::* "><call-template name="recursive-copy"/></for-each></copy>
    </template>

    <template name="subject"> <!-- determines current subject -->
        <choose>
            
            <!-- current node is a meta or a link in the head and with no about attribute -->
            <!--when test="(self::h:link or self::h:meta) and ( ancestor::h:head ) and not(attribute::about)">
                <value-of select="$this"/>
            </when-->
            
            <!-- an attribute about was specified on the node -->
            <when test="self::*/attribute::about">
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="@about"/></call-template>
            </when>
            
            <!-- an attribute src was specified on the node -->
            <when test="self::*/attribute::src">
                <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="@src"/></call-template>
            </when>
            
            
            <!-- an attribute typeof was specified on the node -->
            <when test="self::*/attribute::typeof">
                <call-template name="self-curie-or-uri"><with-param name="node" select="."/></call-template>
            </when>
            
            <!-- current node is a meta or a link in the body and with no about attribute -->
            <!--when test="(self::h:link or self::h:meta) and not( ancestor::h:head ) and not(attribute::about)">
                <call-template name="self-curie-or-uri"><with-param name="node" select="parent::*"/></call-template>
            </when-->
            
            <!-- an about was specified on its parent or the parent had a rel or a rev attribute but no href or an typeof. -->
            <when test="ancestor::*[attribute::about or attribute::src or attribute::typeof or attribute::resource or attribute::href or attribute::rel or attribute::rev][position()=1]">
                <variable name="selected_ancestor" select="ancestor::*[attribute::about or attribute::src or attribute::typeof or attribute::resource or attribute::href or attribute::rel or attribute::rev][position()=1]"/> 
                <choose>
                    <when test="$selected_ancestor[(attribute::rel or attribute::rev) and not (attribute::resource or attribute::href)]">
                        <value-of select="concat('blank:node:INSIDE_',generate-id($selected_ancestor))"/>
                    </when>
                    <when test="$selected_ancestor/attribute::about">
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$selected_ancestor/attribute::about"/></call-template>
                    </when>
                    <when test="$selected_ancestor/attribute::src">
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$selected_ancestor/attribute::src"/></call-template>
                    </when>
                    <when test="$selected_ancestor/attribute::resource">
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$selected_ancestor/attribute::resource"/></call-template>
                    </when>
                    <when test="$selected_ancestor/attribute::href">
                        <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="$selected_ancestor/attribute::href"/></call-template>
                    </when>
                    <otherwise>
                        <call-template name="self-curie-or-uri"><with-param name="node" select="$selected_ancestor"/></call-template>
                    </otherwise>
                </choose>
            </when>
            
            <otherwise> <!-- it must be about the current document -->
                <value-of select="$this"/>
            </otherwise>
            
        </choose>
    </template>
    
    <!-- recursive call for object(s) of object properties -->
    <template name="recurse-objects">
        <xsl:for-each select="child::*">
            <choose>
                <when test="attribute::about or attribute::src"> <!-- there is a known resource -->
                    <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="attribute::about | attribute::src"/></call-template><text> </text>
                </when>
                <when test="(attribute::resource or attribute::href) and ( not (attribute::rel or attribute::rev or attribute::property))"> <!-- there is an incomplet triple -->
                    <call-template name="expand-curie-or-uri"><with-param name="curie_or_uri" select="attribute::resource | attribute::href"/></call-template><text> </text>
                </when>
                <when test="attribute::typeof and not (attribute::about)"> <!-- there is an implicit resource -->
                    <call-template name="self-curie-or-uri"><with-param name="node" select="."/></call-template><text> </text>
                </when>
                <when test="attribute::rel or attribute::rev or attribute::property"> <!-- there is an implicit resource -->
                    <if test="not (preceding-sibling::*[attribute::rel or attribute::rev or attribute::property])"> <!-- generate the triple only once -->
                        <call-template name="subject"/><text> </text>
                    </if>
                </when>     
                <otherwise> <!-- nothing at that level thus consider children -->
                    <call-template name="recurse-objects"/>
                </otherwise>
            </choose>
        </xsl:for-each>
    </template>

    <!-- generate recursive call for multiple objects in rel or rev -->
    <template name="relrev">
        <param name="subject"/>
        <param name="object"/>
        
        <!-- test for multiple predicates -->
        <variable name="single-object"><call-template name="get-first-token"><with-param name="tokens" select="$object"/></call-template></variable> 
        
        <if test="string-length(@rel)&gt;0">
            <call-template name="relation">
                <with-param name="subject" select="$subject"/>
                <with-param name="object" select="$single-object"/>
                <with-param name="predicate" select="@rel"/>
            </call-template>       
        </if>
        
        <if test="string-length(@rev)&gt;0">
            <call-template name="relation">
                <with-param name="subject" select="$single-object"/>
                <with-param name="object" select="$subject"/>
                <with-param name="predicate" select="@rev"/>
            </call-template>      
        </if>
        
        <!-- recursive call for multiple predicates -->
        <variable name="other-objects" select="normalize-space(substring-after($object,' '))"/>
        <if test="string-length($other-objects)&gt;0">
            <call-template name="relrev">
                <with-param name="subject" select="$subject"/>
                <with-param name="object" select="$other-objects"/>
            </call-template>
        </if>
        
    </template>
    
    <!-- generate an RDF statement for a relation -->
    <template name="relation">
        <param name="subject"/>
        <param name="predicate"/>
        <param name="object"/>
        
        <!-- test for multiple predicates -->
        <variable name="single-predicate"><call-template name="get-first-token"><with-param name="tokens" select="$predicate"/></call-template></variable>
        
        <!-- get namespace of the predicate -->
        <variable name="predicate-ns"><call-template name="get-relrev-ns"><with-param name="qname" select="$single-predicate"/></call-template></variable>
        
        <!-- get name of the predicate -->
        <variable name="predicate-name"><call-template name="get-predicate-name"><with-param name="qname" select="$single-predicate"/></call-template></variable>
        
        <choose>
            <when test="string-length($predicate-ns)&gt;0"> <!-- there is a known namespace for the predicate -->
                <element name="rdf:Description" namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <choose>
                        <when test="starts-with($subject,'blank:node:')"><attribute name="rdf:nodeID"><value-of select="substring-after($subject,'blank:node:')"/></attribute></when>
                        <otherwise>
                            <!-- XXX changed this to compute the absolute URI instead -->
                            <!--attribute name="rdf:about"><value-of select="$subject"/></attribute-->
                            <call-template name="compute-uri-about"><with-param name="id" select="substring($subject,2)"></with-param></call-template>
                        </otherwise>
                    </choose>
                    <element name="{$predicate-name}" namespace="{$predicate-ns}">
                        <choose>
                            <when test="starts-with($object,'blank:node:')"><attribute name="rdf:nodeID"><value-of select="substring-after($object,'blank:node:')"/></attribute></when>
                            <otherwise><attribute name="rdf:resource"><value-of select="$object"/></attribute></otherwise>
                        </choose>
                    </element>     
                </element>
            </when>
            <otherwise> <!-- no namespace generate a comment for debug -->
                <xsl:comment>No namespace for the rel or rev value ; could not produce the triple for: <value-of select="$subject"/> - <value-of select="$single-predicate"/> - <value-of select="$object"/></xsl:comment>
            </otherwise>
        </choose>
        
        <!-- recursive call for multiple predicates -->
        <variable name="other-predicates" select="normalize-space(substring-after($predicate,' '))"/>
        <if test="string-length($other-predicates)&gt;0">
            <call-template name="relation">
                <with-param name="subject" select="$subject"/>
                <with-param name="predicate" select="$other-predicates"/>
                <with-param name="object" select="$object"/>
            </call-template>    	
        </if>
        
    </template>

    <!-- generate an RDF statement for a property -->
    <template name="property">
        <param name="subject"/>
        <param name="predicate"/>
        <param name="object"/>
        <param name="datatype"/>
        <param name="attrib"/> <!-- is the content from an attribute ? true /false -->
        <param name="language"/>
        
        <!-- test for multiple predicates -->
        <variable name="single-predicate"><call-template name="get-first-token"><with-param name="tokens" select="$predicate"/></call-template></variable>
        
        <!-- get namespace of the predicate -->
        <variable name="predicate-ns"><call-template name="get-property-ns"><with-param name="qname" select="$single-predicate"/></call-template></variable>
        
        
        <!-- get name of the predicate -->
        <variable name="predicate-name"><call-template name="get-predicate-name"><with-param name="qname" select="$single-predicate"/></call-template></variable>
        
        <choose>
            <when test="string-length($predicate-ns)&gt;0"> <!-- there is a known namespace for the predicate -->
                <element name="rdf:Description" namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <choose>
                        <when test="starts-with($subject,'blank:node:')"><attribute name="rdf:nodeID"><value-of select="substring-after($subject,'blank:node:')"/></attribute></when>
                        <otherwise>
                            <!--attribute name="rdf:about"><value-of select="$subject"/></attribute-->
                            <!-- XXX changed this to compute the absolute URI instead -->
                            <call-template name="compute-uri-about"><with-param name="id" select="substring($subject,2)"></with-param></call-template>
                        </otherwise>
                    </choose>
                    <element name="{$predicate-name}" namespace="{$predicate-ns}">
                        <if test="string-length($language)&gt;0"><attribute name="xml:lang"><value-of select="$language"/></attribute></if>
                        <choose>
                            <when test="$datatype='http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral'">
                                <choose>
                                    <when test="$attrib='true'"> <!-- content is in an attribute -->
                                        <attribute name="rdf:datatype"><value-of select="$datatype"/></attribute>
                                        <value-of select="normalize-space(string($object))"/>
                                    </when>
                                    <otherwise> <!-- content is in the element and may include some tags -->
                                        <!-- On a property element, only one of the attributes rdf:parseType or rdf:datatype is permitted.
                                            <attribute name="rdf:datatype"><value-of select="$datatype" /></attribute> -->
                                        <attribute name="rdf:parseType"><value-of select="'Literal'"/></attribute>
                                        <for-each select="$object/node()"> 
                                            <call-template name="recursive-copy"/>
                                        </for-each>
                                    </otherwise>
                                </choose>
                            </when>
                            <when test="string-length($datatype)&gt;0">
                                <!-- there is a datatype other than XMLLiteral -->
                                <attribute name="rdf:datatype"><value-of select="$datatype"/></attribute>
                                <choose>
                                    <when test="$attrib='true'"> <!-- content is in an attribute -->
                                        <value-of select="normalize-space(string($object))"/>
                                    </when>
                                    <otherwise> <!-- content is in the text nodes of the element -->
                                        <value-of select="normalize-space($object)"/>
                                    </otherwise>
                                </choose>
                            </when>
                            <otherwise> <!-- there is no datatype -->
                                <choose>
                                    <when test="$attrib='true'"> <!-- content is in an attribute -->
                                        <value-of select="normalize-space(string($object))"/>
                                    </when>
                                    <otherwise> <!-- content is in the text nodes of the element -->
                                        <attribute name="rdf:parseType"><value-of select="'Literal'"/></attribute>
                                        <for-each select="$object/node()"> 
                                            <call-template name="recursive-copy"/>
                                        </for-each>
                                    </otherwise>
                                </choose> 
                            </otherwise>
                        </choose>
                    </element>        
                </element>
            </when>
            <otherwise> <!-- generate a comment for debug -->
                <xsl:comment>Could not produce the triple for: <value-of select="$subject"/> - <value-of select="$single-predicate"/> - <value-of select="$object"/></xsl:comment>
            </otherwise>
        </choose>
        
        <!-- recursive call for multiple predicates -->
        <variable name="other-predicates" select="normalize-space(substring-after($predicate,' '))"/>
        <if test="string-length($other-predicates)&gt;0">
            <call-template name="property">
                <with-param name="subject" select="$subject"/>
                <with-param name="predicate" select="$other-predicates"/>
                <with-param name="object" select="$object"/>
                <with-param name="datatype" select="$datatype"/>
                <with-param name="attrib" select="$attrib"/>
                <with-param name="language" select="$language"/>
            </call-template>    	
        </if>
        
    </template>
    
    <!-- generate an RDF statement for a class -->
    <template name="class">
        <param name="resource"/>
        <param name="class"/>
        
        <!-- case multiple classes -->
        <variable name="single-class"><call-template name="get-first-token"><with-param name="tokens" select="$class"/></call-template></variable>
        
        <!-- get namespace of the class -->    
        <variable name="class-ns"><call-template name="return-ns"><with-param name="qname" select="$single-class"/></call-template></variable>
        
        <if test="string-length($class-ns)&gt;0"> <!-- we have a qname for the class -->
            <variable name="expended-class"><call-template name="expand-ns"><with-param name="qname" select="$single-class"/></call-template></variable>        
            <element name="rdf:Description" namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <choose>
                    <when test="starts-with($resource,'blank:node:')"><attribute name="rdf:nodeID"><value-of select="substring-after($resource,'blank:node:')"/></attribute></when>
                    <otherwise><attribute name="rdf:about"><value-of select="$resource"/></attribute></otherwise>
                </choose>
                <element name="rdf:type" namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <attribute name="rdf:resource"><value-of select="$expended-class"/></attribute>
                </element>     
            </element>
        </if>     
        
        <!-- recursive call for multiple classes -->
        <variable name="other-classes" select="normalize-space(substring-after($class,' '))"/>
        <if test="string-length($other-classes)&gt;0">
            <call-template name="class">
                <with-param name="resource" select="$resource"/>
                <with-param name="class" select="$other-classes"/>
            </call-template>    	
        </if>
        
    </template>    
    
    <!-- Do nothing with the things we aren't interested in processing. -->
    <template match="text()|@*|*" priority="-1"/>   

</stylesheet>
