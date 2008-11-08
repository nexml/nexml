#!/usr/bin/perl
use strict;
use warnings;
use constant URL => 'http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nucleotide&sendto=t&list_uids=';
require Bio::SeqIO;

my $GI  = '189068371';
my $url = URL . $GI;