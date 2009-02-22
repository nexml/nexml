# this is NOT part of the distribution, or in any way of interest
# other than on the nexml webserver. It builds a CPAN compatible
# distribution out of the source tree, places that in the downloads
# folder, then cleans up after itself. That's all.
perl Makefile.PL > /dev/null
make manifest &> /dev/null
make dist > /dev/null
make clean > /dev/null
mv Bio-Phylo*.tar.gz ../downloads
rm Makefile.old MANIFEST.bak MANIFEST META.yml
