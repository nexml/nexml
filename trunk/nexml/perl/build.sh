# this is NOT part of the distribution, or in any way of interest
# other than on the nexml webserver. It builds a CPAN compatible
# distribution out of the source tree, places that in the downloads
# folder, then cleans up after itself. That's all.
DOWNLOADS=../downloads
./podinherit -dir lib -append
perl Makefile.PL > /dev/null
make manifest &> /dev/null
make dist > /dev/null
make clean > /dev/null
if [ -d "$DOWNLOADS" ]; then
    mv Bio-Phylo*.tar.gz ../downloads
fi
./podinherit -dir lib -strip
rm Makefile.old MANIFEST.bak MANIFEST META.yml
