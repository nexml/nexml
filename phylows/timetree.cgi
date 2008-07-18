#!/usr/bin/perl
use strict;
use warnings;
use HTML::Tree;
use LWP::UserAgent;
use URI::Escape;
use CGI;
use Scalar::Util qw(looks_like_number);
use HTML::Entities;
use CGI::Carp qw(fatalsToBrowser);
use Bio::Phylo::Factory;
use Bio::Phylo::IO 'unparse';
use constant URL => 'http://timetree.org/time_query.php?';

my $cgi = CGI->new;
my $lua = LWP::UserAgent->new;
my $tre = HTML::TreeBuilder->new;
my $fac = Bio::Phylo::Factory->new;
my $taxon_a = $cgi->param('taxon_a') || shift @ARGV;
my $taxon_b = $cgi->param('taxon_b') || shift @ARGV;
my $pubmed  = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=';
my $dates   = [ date->new ];

my $response = $lua->get( 
    URL 
    . 'taxon_a=' 
    . uri_escape($taxon_a) 
    . '&' 
    . 'taxon_b=' 
    . uri_escape($taxon_b) 
);

if ( $response->is_success ) {
    my $content = $response->content;
    $tre->parse( $content );
    $tre->eof;
    recurse( $tre, $dates );
    pop @{ $dates };
    my $forest = $fac->create_forest;
    my $taxa   = $fac->create_taxa;
    my $taxona = $fac->create_taxon( '-xml_id' => 'taxona' );
    my $taxonb = $fac->create_taxon( '-xml_id' => 'taxonb' );
    $taxa->insert( $taxona, $taxonb );
    $forest->set_taxa( $taxa );
    for my $date ( @{ $dates } ) {
        my $tree = $fac->create_tree;        
        my %dict = (
            'genes'  => [ 'string' => $date->genes  ],
            'data'   => [ 'string' => $date->data   ],     
            'source' => [ 'string' => $date->source ],
            'pub'    => [ 'url'    => encode_entities($pubmed . $date->pub . '&dopt=Abstract') ]                               
        );
        $tree->set_generic( 'dict' => \%dict );
        my $root = $fac->create_node;
        $tree->insert($root);
        my $node_a = $fac->create_node( 
            '-branch_length' => $date->time, 
            '-parent'        => $root,
            '-taxon'         => $taxona,
            '-attributes'    => { 'label' => $date->taxona }
        );
        my $node_b = $fac->create_node( 
            '-branch_length' => $date->time, 
            '-parent'        => $root,
            '-taxon'         => $taxonb,
            '-attributes'    => { 'label' => $date->taxonb }
        );
        $tree->insert( $node_a, $node_b );
        $forest->insert( $tree );
    }
    print unparse( '-format' => 'nexml', '-phylo' => $forest );
}
else {
    die $response->status_line;
}

sub recurse {
    my ( $node, $dates ) = @_;
    if ( ref $node and $node->isa('HTML::Element')  ) {
        if ( $node->tag eq 'a' and $node->attr('href') =~ /\Q$pubmed\E(\d+)/ ) {
            my $pub = $1;
            $dates->[-1]->pub($pub);
        }
        if ( $node->tag eq 'div' and $node->attr('class') and $node->attr('class') =~ /^mock/ ) {
            for my $content ( $node->content_list ) {
                if ( ref $content ) {                    
                    for my $child ( $content->content_list ) {
                        if ( not ref $child ) {
                            if ( looks_like_number $child ) {
                                $dates->[-1]->time($child);
                            }
                            elsif ( $child eq 'n/c' ) {
                                $dates->[-1]->genes($child);
                            }
                        }
                        else {
                            for my $taxon ( $child->content_list ) {
                                if ( $dates->[-1]->taxona ) {
                                    $dates->[-1]->taxonb($taxon);
                                }
                                else {
                                    $dates->[-1]->taxona($taxon);
                                }
                            }
                        }
                    }
                }
                else {
                    if ( $content =~ /(?:Nucleotide|Amino)/ ) {
                        $dates->[-1]->data($content);
                    }
                    elsif ( looks_like_number $content or $content =~ m/^\d+kb$/ or $content eq '-' ) {
                        $dates->[-1]->genes($content);
                    }
                    else {
                        if ( $content !~ /(?:DataType|Time|Source|Taxon [AB]|# Genes)/ ) {
                            $dates->[-1]->source($content);
                            if ( $dates->[-1]->is_complete ) {
                                push @{ $dates }, date->new;
                                #print "\n\n=====\n";
                            }
                            else {
                                $dates->[-1]->pub( $dates->[-2]->pub ); 
                                #print "\n\n=====\n";
                                die "huh?" if not $dates->[-1]->is_complete;
                                push @{ $dates }, date->new;                                
                            }
                        }
                    }
                }
            }
        }
        recurse($_,$dates) for $node->content_list;
    } 
}

package date;

our $AUTOLOAD;

sub new {
    my $class = shift;
    my $self = {
        'time'   => undef,
        'genes'  => undef,
        'taxona' => undef,
        'taxonb' => undef,
        'data'   => undef,
        'source' => undef,
        'pub'    => undef,
    };
    return bless $self, $class;
}

sub is_complete {
    my $self = shift;    
    for my $key ( keys %{ $self } ) {
        return 0 if not $self->{$key};
    }
    return 1;
}

sub AUTOLOAD {
    my ( $self, $arg ) = @_;
    my $method = $AUTOLOAD;
    $method =~ s/^.*://;
    if ( exists $self->{$method} ) {
        if ( $arg ) {
            $self->{$method} = $arg;
            #print "$method => $arg\n";        
        }
        return $self->{$method};
    }
    else {
        die "Can't do $method";
    }
}

sub DESTROY {}
    
1;