#!/usr/bin/perl
BEGIN {
    unshift @INC, 'perl/lib';
    unshift @INC, '../phylo/lib'; # i.e. the latest Bio::Phylo on CIPRES svn, in framework/perl
}
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
use HTML::Entities;

my $q = CGI->new;
my $file = $q->upload('file');
print <<HEADER;
Content-type:text/html

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head><title>nexml validation results</title>
<style type="text/css">
.debug { background-color: #7DACFF; display: none }
.info  { background-color: #82FF7B; display: none }
.warn  { background-color: #D0FF7D }
.error { background-color: #FFB77B }
.fatal { background-color: #FF7B7B }
table, tr, td, pre { padding: 0px; margin: 0px }
</style>
<script type="text/javascript">
function toggle(class) {
	var pre_elts = document.getElementsByTagName('pre');
	for ( var i = 0; i < pre_elts.length; i++ ) {
		var this_class = pre_elts[i].className;
		if ( this_class == class ) {
			if ( pre_elts[i].style.display == 'none' ) {
				pre_elts[i].style.display = 'block';
			}
			else if ( pre_elts[i].style.display == 'block' ) {
				pre_elts[i].style.display = 'none';
			}
		}
	}
}
</script>
</head>
<body>
Show: 
<a href="javascript:toggle('debug')" class="debug">debug</a> |
<a href="javascript:toggle('info')" class="info">info</a> | 
<a href="javascript:toggle('warn')" class="warn">warnings</a> | 
<a href="javascript:toggle('error')" class="error">errors</a> | 
<a href="javascript:toggle('fatal')" class="fatal">fatal</a>
HEADER
my @lines = <$file>;
my $logger = Bio::Phylo::Util::Logger->new;
$logger->VERBOSE( '-level' => 4, '-class' => 'Bio::Phylo::Parsers::Nexml' );
$logger->set_listeners(
	sub {
		my ( $log_string, $method, $subroutine, $filename, $line, $msg ) = @_;
		my ( $xline, $xcol, $xbyte );
		if ( $msg =~ m/^(\d+):(\d+):(\d+)\b/ ) {
			( $xline, $xcol, $xbyte ) = ( $1, $2, $3 );
			$msg =~ s/^(\d+:\d+:\d)\s*//;
		}
		print '<pre class="', lc($method), '">', encode_entities($msg), ' at line <a href="#line', $xline, '">',$xline,'</a></pre>';
	}
);
my $blocks;
eval {
	$blocks = parse( '-format' => 'nexml', '-string' => join '', @lines );
};
if ( $@ ) {
	my $line = $@->line;
	print '<pre class="fatal">', encode_entities($@->error), ' at line <a href="#line', $line, '">',$line,'</a></pre>';
}
my @class = qw(info warn error fatal);
print '<table>';
my $i = 0;
for my $line (@lines){
	chomp($line);
	print '<tr class="info"><td><pre><a href="error">', ++$i, '</a><a name="line',$i,'"></a></pre></td><td><pre>', encode_entities($line), '</pre></td></tr>', "\n";
}
print '</table></body></html>';
