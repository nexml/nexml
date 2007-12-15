Bio::Phylo - An object-oriented Perl module 
for analyzing and manipulating phylogenetic trees. 

DESCRIPTION

Phylogenetics is the branch of evolutionary biology
that deals with reconstructing and analyzing the 
tree of life. This distribution provides objects and
methods to aid in handling and analyzing phylogenetic
data.

COMPATABILITY
 
Bio::Phylo installs without problems on most popular,
current platforms (Win32, OSX, RedHat linux, Solaris).
For a list of automated test results visit:

http://testers.cpan.org/show/Bio-Phylo.html

INSTALLATION

Bio::Phylo has the following required dependencies:
	Scalar::Util (core)
	Test::More (core)
	IO::String (non-core, but installs everywhere)

And the following optional dependencies:
	SVG (to draw trees as SVG vector drawings)
	Math::Random (to simulate trees)
	XML::Twig (to parse nexml)

These modules are all available from
http://www.cpan.org, and should install without
problems on most platforms.
 
To install the Bio::Phylo distribution, run the
following commands: 

perl Makefile.PL
make
make test
make install
 
(For platform specific information on what 'make'
command to use, check "perl -V:make". On Win32 this
usually returns "make='nmake';", which means you'll
need the 'nmake' utility:
http://support.microsoft.com/default.aspx?scid=kb;en-us;Q132084)

AUTHOR

Rutger Vos, rvos@interchange.ubc.ca

BUGS

Please report any bugs or feature requests to 
bug-phylo@rt.cpan.org, or through the web interface 
at http://rt.cpan.org/NoAuth/ReportBug.html?Queue=Phylo. 
I will be notified, and then you'll automatically 
be notified of progress on your bug as I make changes. 
 
ACKNOWLEDGEMENTS

The author would like to thank Jason Stajich for many 
ideas borrowed from BioPerl (http://www.bioperl.org), 
and Arne Mooers from the FAB* lab (http://www.sfu.ca/~fabstar) 
for comments and requests. 

SEE ALSO

Read the manual: perldoc Bio::Phylo::Manual

COPYRIGHT & LICENSE

Copyright 2005-2007 Rutger Vos, All Rights Reserved. 
This program is free software; you can redistribute 
it and/or modify it under the same terms as Perl itself.

