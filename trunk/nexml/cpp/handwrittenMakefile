CXX=g++
XSD=/home/boris/work/xsd/xsd/xsd/xsd
LIBXSD=/home/boris/work/xsd/xsd/libxsd

xsd := characters/abstract.xsd \
characters/characters.xsd \
characters/continuous.xsd \
characters/dna.xsd \
characters/protein.xsd \
characters/restriction.xsd \
characters/rna.xsd \
characters/standard.xsd \
external/xlink.xsd \
external/xml.xsd \
meta/annotations.xsd \
meta/meta.xsd \
taxa/taxa.xsd \
trees/abstract.xsd \
trees/network.xsd \
trees/tree.xsd \
trees/trees.xsd \
nexml.xsd \
abstract.xsd

dir = out out/characters out/external out/meta out/taxa out/trees

# Create output directories
#
$(shell mkdir -p $(dir))

src := driver.cxx $(patsubst %.xsd,out/%.cxx,$(xsd))
hxx := $(patsubst %.xsd,out/%.hxx,$(xsd))
obj := $(src:.cxx=.o)

driver: $(obj) -lxerces-c
	$(CXX) -o $@ $^

%.o: %.cxx $(hxx)
	$(CXX) -I$(LIBXSD) -o $@ -c $<

.PRECIOUS: out/%.hxx out/%.cxx
out/%.hxx out/%.cxx: %.xsd
	$(XSD) cxx-tree --namespace-map http://www.nexml.org/1.0=NeXML \
--generate-polymorphic --generate-ostream --output-dir $(@D) $<

.PHONY: clean
clean:
	rm -rf out
	rm -f driver driver.o
