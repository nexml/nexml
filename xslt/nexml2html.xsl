<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--
		Transforms XML into JavaScript objects, using the JSON format.
		Version 2009-08-09, written by Jens Duttke
	-->
	<!--xsl:output method="text" encoding="UTF-8" omit-xml-declaration="yes" indent="no" media-type="application/json" /-->
	<xsl:strip-space elements="*" />
	<xsl:template match="/">
		<html>
		<head>
		<link rel="stylesheet" href="https://dl.dropbox.com/u/4180059/jsonlib/nexml2html.css"></link>
		</head>
		<body id="body">
			<!--script src="http://yui.yahooapis.com/3.5.1/build/yui/yui-min.js"></script-->
			<script type="text/javascript" src="https://dl.dropbox.com/u/4180059/jsonlib/prototype.js"></script>
			<script type="text/javascript" src="https://dl.dropbox.com/u/4180059/jsonlib/nexml.js"></script>
			<script type="text/javascript" src="https://dl.dropbox.com/u/4180059/jsonlib/nexml2html.js"></script>			
			<script type="text/javascript">
			<xsl:text>nexml2html({</xsl:text>
			<xsl:for-each select="*">
				<xsl:call-template name="processNode" />
			</xsl:for-each>
			<xsl:text>});</xsl:text>
			</script>		
		</body>
		</html>
	</xsl:template>



	<xsl:template name="processNode">
		<!-- Output the node name + open array (only if we aren't already in an array) -->
		<xsl:if test="count(preceding-sibling::*[name() = name(current())]) = 0">
			<xsl:text>"</xsl:text><xsl:value-of select="name()" /><xsl:text>":</xsl:text>
			<xsl:if test="count(following-sibling::*[name() = name(current())]) &gt; 0">
				<xsl:text>[</xsl:text>
			</xsl:if>
		</xsl:if>
		<xsl:text>{</xsl:text>

		<!-- Output node content -->
		<xsl:text>"#":"</xsl:text><xsl:call-template name="escape"><xsl:with-param name="text" select="text()" /></xsl:call-template><xsl:text>"</xsl:text>

		<!-- Output node attributes -->
		<xsl:for-each select="@*">
			<xsl:text>,"@</xsl:text><xsl:value-of select="local-name()" /><xsl:text>":"</xsl:text><xsl:call-template name="escape"><xsl:with-param name="text" select="current()" /></xsl:call-template><xsl:text>"</xsl:text>
		</xsl:for-each>

		<!-- Process sub nodes -->
		<xsl:for-each select="*">
			<xsl:if test="count(preceding-sibling::*[name() = name(current())]) = 0">
				<xsl:text>,</xsl:text>
				<xsl:call-template name="processNode" />
			</xsl:if>
		</xsl:for-each>
		<xsl:text>}</xsl:text>

		<!-- Process following sub nodes with the same node name as array -->
		<xsl:if test="count(preceding-sibling::*[name() = name(current())]) = 0">
			<xsl:for-each select="following-sibling::*[name() = name(current())]">
				<xsl:text>,</xsl:text>
				<xsl:call-template name="processNode" />
			</xsl:for-each>
		</xsl:if>

		<!-- Close array -->
		<xsl:if test="count(preceding-sibling::*[name() = name(current())]) &gt; 0">
			<xsl:if test="count(following-sibling::*[name() = name(current())]) = 0">
				<xsl:text>]</xsl:text>
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<!-- Replace characters which could cause an invalid JS object, by their escape-codes. -->
	<xsl:template name="escape">
		<xsl:param name="text" />
		<xsl:param name="char" select="'\'" />
		<xsl:param name="nextChar" select="substring(substring-after('\/&quot;&#xD;&#xA;&#x9;',$char),1,1)" />

		<xsl:choose>
			<xsl:when test="$char = ''">
				<xsl:value-of select="$text" />
			</xsl:when>
			<xsl:when test="contains($text,$char)">
				<xsl:call-template name="escape">
					<xsl:with-param name="text" select="substring-before($text,$char)" />
					<xsl:with-param name="char" select="$nextChar" />
				</xsl:call-template>
				<xsl:value-of select="concat('\',translate($char,'&#xD;&#xA;&#x9;','nrt'))" />
				<xsl:call-template name="escape">
					<xsl:with-param name="text" select="substring-after($text,$char)" />
					<xsl:with-param name="char" select="$char" />
				</xsl:call-template>
			</xsl:when>

			<xsl:otherwise>
				<xsl:call-template name="escape">
					<xsl:with-param name="text" select="$text" />
					<xsl:with-param name="char" select="$nextChar" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>