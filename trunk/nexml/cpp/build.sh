#!/bin/sh
# http://codesynthesis.com/products/xsd/download.xhtml
XSD_HOME=~/xsd-3.1.0.b1-i686-macosx
# http://xerces.apache.org/xerces-c/download.cgi
XERCES_HOME=~/xerces-c_2_8_0-x86-macosx-gcc_4_0
XSDBIN=$XSD_HOME/bin/xsd
XSDINC=$XSD_HOME/libxsd/
XERCESINC=$XERCES_HOME/include
XERCESLIB=$XERCES_HOME/lib
CXX=g++
rm -rf out cpp-bindings.tar.gz
files=$(find ../xsd -name "*.xsd")
for file in $files; do
	outdir=$(echo $file |  perl -e 'while(<>){s|\.{2}/xsd|out|g;s|[a-z]*\.xsd$||;print}')
	if [ ! -d $outdir ]
		then mkdir $outdir
	fi
	$XSDBIN cxx-tree --namespace-map http://www.nexml.org/1.0=NeXML \
	--generate-polymorphic --generate-ostream --output-dir $outdir $file
done
cp driver.cxx out
tar -cf cpp-bindings.tar out
gzip -9 cpp-bindings.tar
cd out
sources=$(find . -name "*.cxx")
for source in $sources; do
	object=$(echo $source | sed -e 's/.cxx$/.o/')
	$CXX -I$XERCESINC -I$XSDINC -o $object -c $source
done
objects=$(find . -name "*.o")
$CXX -L$XERCESLIB -lxerces-c -o driver $objects