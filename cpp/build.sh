#!/bin/sh
if test -f build_env.sh
then
	source build_env.sh
else
	if test -z "$XSD_HOME" -o -z "$XERCES_HOME"
	then
		echo "XSD_HOME and XERCES_HOME must be defined in your env or the file build_env.sh"
		echo "For example, you could set build_env.sh to contain lines like this:"
		echo
		echo "export XSD_HOME=\"\$HOME/xsd-3.1.0.b1-i686-macosx\""
		echo "export XERCES_HOME=\"\$HOME/xerces-c_2_8_0-x86-macosx-gcc_4_0\""
		exit 1
	fi
fi

XSDBIN=$XSD_HOME/bin/xsd
XSDINC=$XSD_HOME/libxsd/
XERCESINC=$XERCES_HOME/include
XERCESLIB=$XERCES_HOME/lib
CXX=g++

set -x 

if test -d out
then
	rm -rf out
fi
if test -f cpp-bindings.tar.gz
then 
	rm cpp-bindings.tar.gz
fi

files=$(find ../xsd -name "*.xsd")
for file in $files; do
	outdir=$(echo $file |  perl -e 'while(<>){s|\.{2}/xsd|out|g;s|[a-z]*\.xsd$||;print}')
	if [ ! -d $outdir ]
		then mkdir $outdir
	fi
	$XSDBIN cxx-tree --namespace-map http://www.nexml.org/1.0=NeXML --generate-polymorphic --generate-ostream --output-dir $outdir $file || exit
done
cp driver.cxx out

tar -cvzf cpp-bindings.tar out

cd out

sources=$(find . -name "*.cxx")
for source in $sources; do
	object=$(echo $source | sed -e 's/.cxx$/.o/')
	$CXX -I$XERCESINC -I$XSDINC -o $object -c $source || exit
done

objects=$(find . -name "*.o")
$CXX -L$XERCESLIB -lxerces-c -o driver $objects
