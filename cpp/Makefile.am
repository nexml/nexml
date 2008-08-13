pkglib_LTLIBRARIES = libnexmlparse.la
libnexmlparse_ladir = $(includedir)/nexmlparse
libnexmlparse_la_LDFLAGS = -release @PACKAGE_VERSION@

NEXML_XSD_PREFIXES = \
	abstract \
	characters/abstractcharacters \
	characters/characters \
	characters/continuous \
	characters/dna \
	characters/protein \
	characters/restriction \
	characters/rna \
	characters/standard \
	external/xlink \
	external/xml \
	meta/annotations \
	meta/meta \
	nexml \
	taxa/taxa \
	trees/abstracttrees \
	trees/network \
	trees/tree \
	trees/trees 

NEXML_XSD_FULL_PATHS = \
	$(abs_top_srcdir)/xsd/abstract.xsd \
	$(abs_top_srcdir)/xsd/characters/abstractcharacters.xsd \
	$(abs_top_srcdir)/xsd/characters/characters.xsd \
	$(abs_top_srcdir)/xsd/characters/continuous.xsd \
	$(abs_top_srcdir)/xsd/characters/dna.xsd \
	$(abs_top_srcdir)/xsd/characters/protein.xsd \
	$(abs_top_srcdir)/xsd/characters/restriction.xsd \
	$(abs_top_srcdir)/xsd/characters/rna.xsd \
	$(abs_top_srcdir)/xsd/characters/standard.xsd \
	$(abs_top_srcdir)/xsd/external/xlink.xsd \
	$(abs_top_srcdir)/xsd/external/xml.xsd \
	$(abs_top_srcdir)/xsd/meta/annotations.xsd \
	$(abs_top_srcdir)/xsd/meta/meta.xsd \
	$(abs_top_srcdir)/xsd/nexml.xsd \
	$(abs_top_srcdir)/xsd/taxa/taxa.xsd \
	$(abs_top_srcdir)/xsd/trees/abstracttrees.xsd \
	$(abs_top_srcdir)/xsd/trees/network.xsd \
	$(abs_top_srcdir)/xsd/trees/tree.xsd \
	$(abs_top_srcdir)/xsd/trees/trees.xsd 

FILES_FROM_NEXML_XSD = 			\
	abstract.hxx \
	characters/abstractcharacters.hxx \
	characters/characters.hxx \
	characters/continuous.hxx \
	characters/dna.hxx \
	characters/protein.hxx \
	characters/restriction.hxx \
	characters/rna.hxx \
	characters/standard.hxx \
	external/xlink.hxx \
	external/xml.hxx \
	meta/annotations.hxx \
	meta/meta.hxx \
	nexml.hxx \
	taxa/taxa.hxx \
	trees/abstracttrees.hxx \
	trees/network.hxx \
	trees/tree.hxx \
	trees/trees.hxx \
	abstract.cxx \
	characters/abstractcharacters.cxx \
	characters/characters.cxx \
	characters/continuous.cxx \
	characters/dna.cxx \
	characters/protein.cxx \
	characters/restriction.cxx \
	characters/rna.cxx \
	characters/standard.cxx \
	external/xlink.cxx \
	external/xml.cxx \
	meta/annotations.cxx \
	meta/meta.cxx \
	nexml.cxx \
	taxa/taxa.cxx \
	trees/abstracttrees.cxx \
	trees/network.cxx \
	trees/tree.cxx \
	trees/trees.cxx

GENERATED_HEADER_FILES = $(foreach file, $(NEXML_XSD_PREFIXES), $(foreach ext, .hxx, $(file)$(ext))) 
nodist_libnexmlparse_la_HEADERS = $(GENERATED_HEADER_FILES)

nodist_libnexmlparse_la_SOURCES = 			\
	$(FILES_FROM_NEXML_XSD) 		

CLEANFILES = $(FILES_FROM_NEXML_XSD)

XSDGEN_EXT = .hxx .cxx
# below are our "hand written" rules that specify how to generate cxx and hxx files from xsd and how the objects depend on them.


# .hxx and .cxx generated from our .xsd
$(foreach file, $(NEXML_XSD_PREFIXES), $(foreach ext, $(XSDGEN_EXT), $(file)$(ext))): $(addsuffix .xsd, $(foreach xsdfile, $(NEXML_XSD_PREFIXES), $(abs_top_srcdir)/xsd/$(xsdfile))) $(foreach file, $(FROM_ORB_PREFIXES), $(foreach ext, $(XSDGEN_EXT), $(file)$(ext)))
	@xsdtag=`echo "$@" | $(EXSED) -e 's:(.*/)?([a-zA-Z]*)\....:\2:'`; xsdsubdir=`echo "$@" | $(EXSED) -e 's:(.*/)?([a-zA-Z]*)\....:\1:'`; echo $@ compiling $@ as $$xsdtag in $$xsdsubdir; if ! test -d ./$$xsdsubdir ; then mkdir ./$$xsdsubdir ; else echo ./$$xsdsubdir exists  ; fi ; @XSDBIN@ cxx-tree --namespace-map http://www.nexml.org/1.0=NeXML --generate-polymorphic --generate-ostream --output-dir ./$$xsdsubdir $(abs_top_srcdir)/xsd/$$xsdsubdir$$xsdtag.xsd 

# each .lo for generated files: depend on all generated .cxxs 
# should be built using default .cxx:.lo rule
$(foreach file, $(NEXML_XSD_PREFIXES), $(file).lo): $(addsuffix .cxx, $(foreach xsdfile, $(NEXML_XSD_PREFIXES), $(xsdfile))) 

uninstall-hook:
	rmdir $(libnexmlparse_ladir) 2> /dev/null || true
dist-hook:
	
