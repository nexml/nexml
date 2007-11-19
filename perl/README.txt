This folder contains the perl code written by Jason Caravas and Rutger Vos 
during the google summer of code '07 project on phylogenetic xml. The t
subfolder contains test scripts for Bio::Phylo using the Test::More test
harness. The lib subfolder contains parsers and unparsers for the
Bio::Phylo::IO architecture.

================================================================================
Installation
================================================================================
This is what Mark Holder had to do to run t/Nexml.pm
create a directory to add to my PERL5LIB the contents are shown below. They were
obtained by copying the files from a CIPRES installation  -- they were in
CIPRES_ROOT/lib/perl/lib.

Then I added the ext directory mentioned below to my DYLD_LIBRARY_PATH (on Mac, 
on *nix it would be added to the LD_LIBRARY_PATH)

Contents of the new directory on my PERL5LIB:
	auto
	auto/XML
	auto/XML/Parser
	auto/XML/Parser/.packlist
	auto/XML/Parser/Expat
	auto/XML/Parser/Expat/Expat.bs
	auto/XML/Parser/Expat/Expat.bundle
	auto/XML/Twig
	auto/XML/Twig/.packlist
	Bio
	Bio/Phylo
	Bio/Phylo/Adaptor
	Bio/Phylo/Adaptor/Bioperl
	Bio/Phylo/Adaptor/Bioperl/Datum.pm
	Bio/Phylo/Adaptor/Bioperl/Matrix.pm
	Bio/Phylo/Adaptor/Bioperl/Node.pm
	Bio/Phylo/Adaptor/Bioperl/Tree.pm
	Bio/Phylo/Adaptor.pm
	Bio/Phylo/Forest
	Bio/Phylo/Forest/Node.pm
	Bio/Phylo/Forest/Tree.pm
	Bio/Phylo/Forest.pm
	Bio/Phylo/Generator.pm
	Bio/Phylo/IO.pm
	Bio/Phylo/Listable.pm
	Bio/Phylo/Manual.pod
	Bio/Phylo/Matrices
	Bio/Phylo/Matrices/Datatype
	Bio/Phylo/Matrices/Datatype/Continuous.pm
	Bio/Phylo/Matrices/Datatype/Custom.pm
	Bio/Phylo/Matrices/Datatype/Dna.pm
	Bio/Phylo/Matrices/Datatype/Mixed.pm
	Bio/Phylo/Matrices/Datatype/Protein.pm
	Bio/Phylo/Matrices/Datatype/Restriction.pm
	Bio/Phylo/Matrices/Datatype/Rna.pm
	Bio/Phylo/Matrices/Datatype/Standard.pm
	Bio/Phylo/Matrices/Datatype.pm
	Bio/Phylo/Matrices/Datum.pm
	Bio/Phylo/Matrices/Matrix.pm
	Bio/Phylo/Matrices/TypeSafeData.pm
	Bio/Phylo/Matrices.pm
	Bio/Phylo/Mediators
	Bio/Phylo/Mediators/NodeMediator.pm
	Bio/Phylo/Mediators/TaxaMediator.pm
	Bio/Phylo/Parsers
	Bio/Phylo/Parsers/Newick.pm
	Bio/Phylo/Parsers/Nexus.pm
	Bio/Phylo/Parsers/Table.pm
	Bio/Phylo/Parsers/Taxlist.pm
	Bio/Phylo/Taxa
	Bio/Phylo/Taxa/TaxaLinker.pm
	Bio/Phylo/Taxa/Taxon.pm
	Bio/Phylo/Taxa/TaxonLinker.pm
	Bio/Phylo/Taxa.pm
	Bio/Phylo/Treedrawer
	Bio/Phylo/Treedrawer/Svg.pm
	Bio/Phylo/Treedrawer.pm
	Bio/Phylo/Unparsers
	Bio/Phylo/Unparsers/Mrp.pm
	Bio/Phylo/Unparsers/Newick.pm
	Bio/Phylo/Unparsers/Nexus.pm
	Bio/Phylo/Unparsers/Pagel.pm
	Bio/Phylo/Util
	Bio/Phylo/Util/CONSTANT.pm
	Bio/Phylo/Util/Exceptions.pm
	Bio/Phylo/Util/IDPool.pm
	Bio/Phylo/Util/Logger.pm
	Bio/Phylo/Util/XMLWritable.pm
	Bio/Phylo.pm
	ext
	ext/lib
	ext/lib/libexpat.1.5.2.dylib
	ext/lib/libexpat.1.dylib
	ext/lib/libexpat.dylib
	ext/lib/libexpat.la
	XML
	XML/Parser
	XML/Parser/Encodings
	XML/Parser/Encodings/big5.enc
	XML/Parser/Encodings/euc-kr.enc
	XML/Parser/Encodings/iso-8859-2.enc
	XML/Parser/Encodings/iso-8859-3.enc
	XML/Parser/Encodings/iso-8859-4.enc
	XML/Parser/Encodings/iso-8859-5.enc
	XML/Parser/Encodings/iso-8859-7.enc
	XML/Parser/Encodings/iso-8859-8.enc
	XML/Parser/Encodings/iso-8859-9.enc
	XML/Parser/Encodings/Japanese_Encodings.msg
	XML/Parser/Encodings/README
	XML/Parser/Encodings/windows-1250.enc
	XML/Parser/Encodings/windows-1252.enc
	XML/Parser/Encodings/x-euc-jp-jisx0221.enc
	XML/Parser/Encodings/x-euc-jp-unicode.enc
	XML/Parser/Encodings/x-sjis-cp932.enc
	XML/Parser/Encodings/x-sjis-jdk117.enc
	XML/Parser/Encodings/x-sjis-jisx0221.enc
	XML/Parser/Encodings/x-sjis-unicode.enc
	XML/Parser/Expat.pm
	XML/Parser/LWPExternEnt.pl
	XML/Parser/Style
	XML/Parser/Style/Debug.pm
	XML/Parser/Style/Objects.pm
	XML/Parser/Style/Stream.pm
	XML/Parser/Style/Subs.pm
	XML/Parser/Style/Tree.pm
	XML/Parser.pm
	XML/Twig
	XML/Twig/XPath.pm
	XML/Twig/XPath.pm_bak
	XML/Twig.pm
