#!/usr/bin/perl
BEGIN {
    use CGI::Carp qw(fatalsToBrowser);
}
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/perllib';
    use lib $ENV{'DOCUMENT_ROOT'} . '/perllib/arch';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib';
    unshift @INC, '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
}
use strict;
use warnings;
use HTML::Tree;
use LWP::UserAgent;
use URI::Escape;
use CGI;
use Data::Dumper;
use Scalar::Util qw(looks_like_number);
use HTML::Entities;
use Bio::Phylo::Factory;
use Bio::Phylo::IO 'unparse';
use constant URL => 'http://69.36.181.148/time_query.php?';

my $cgi = CGI->new;
my $lua = LWP::UserAgent->new;
my $tre = HTML::TreeBuilder->new;
my $fac = Bio::Phylo::Factory->new;
my $taxon_a = $cgi->param('taxon_a') || shift @ARGV;
my $taxon_b = $cgi->param('taxon_b') || shift @ARGV;
my $pubmed  = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=';
my $dates   = [];

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
    find_tbody( $tre, $dates );
    my $project = $fac->create_project;
    my $forest  = $fac->create_forest;
    my $taxa    = $fac->create_taxa;
    my $taxona  = $fac->create_taxon( '-xml_id' => 'taxona' );
    my $taxonb  = $fac->create_taxon( '-xml_id' => 'taxonb' );
    $project->insert( $taxa, $forest );
    $taxa->insert( $taxona, $taxonb );
    $forest->set_taxa( $taxa );
    for my $date ( @{ $dates } ) {
        my $tree = $fac->create_tree;   
        my $dict = $fac->create_dictionary;
        for my $field ( qw(genes data source) ) {
            $dict->insert( 
                $fac->create_annotation( 
                    '-xml_id' => $field, '-tag' => 'string', '-value' => $date->$field
                ) 
            );
        }
        $dict->insert(
            $fac->create_annotation(
                '-xml_id' => 'pub', 
                '-tag'    => 'uri', 
                '-value'  => encode_entities($pubmed . $date->pub . '&dopt=Abstract'),
            )
        );
        $tree->add_dictionary( $dict );
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
    print "Content-type: text/xml\n\n";
    print $project->to_xml;
}
else {
    die $response->status_line;
}

sub find_tbody {
    my ( $node, $dates ) = @_;
    for my $table ( $node->descendents ) {
        if ( UNIVERSAL::isa( $table, 'HTML::Element' ) ) {
            my $class = $table->attr('class');
            if ( $table->tag eq 'table' and $class and $class eq 'collapsible' ) {           
                for my $tbody ( $table->content_list ) {
                    if ( UNIVERSAL::isa( $tbody, 'HTML::Element' ) and $tbody->tag =~ /tbody/i ) {
                        process_tbody( $tbody, $dates );
                    }
                }            
            }
        }
    }
}

sub process_tbody {
    my ( $tbody, $dates ) = @_;    
    my ( $pub, $taxona, $taxonb, $source, $data, $genes, $time );
    for my $tr ( $tbody->content_list ) {
        if ( UNIVERSAL::isa( $tr, 'HTML::Element' ) and $tr->tag eq 'tr' ) {
            
            # a header row
            if ( $tr->attr('bgcolor') and $tr->attr('bgcolor') eq '#FFFFFF' ) {
                for my $a ( $tr->descendents ) {
                    if ( UNIVERSAL::isa( $a, 'HTML::Element' ) and $a->tag eq 'a' ) {
                        my $href = $a->attr('href');
                        if ( $href =~ /\Q$pubmed\E(\d+)/ ) {
                            $pub = $1;
                        }
                    }                
                }            
            }
            
            # a content row
            elsif ( $tr->attr('class') eq 'collapsible' ) {
                for my $div ( $tr->descendents ) {
                    if ( UNIVERSAL::isa( $div, 'HTML::Element' ) and $div->tag eq 'div' ) {
                        if ( $div->attr('class') eq 'mockTD' ) {
                            if ( not defined $time ) {
                                $time = $div->as_text;
                            }
                            elsif ( not defined $genes ) {
                                $genes = $div->as_text;
                            }
                            elsif ( not defined $data ) {
                                $data = $div->as_text;
                            }
                        }
                        elsif ( $div->attr('class') eq 'mockTDTaxa' ) {
                            if ( not defined $taxona ) {
                                $taxona = $div->as_text;
                            }
                            elsif ( not defined $taxonb ) {
                                $taxonb = $div->as_text;
                            }
                        }
                        elsif ( $div->attr('class') eq 'mockTDSource' ) {
                            $source = $div->as_text;
                            push @{ $dates }, date->new(
                                'pub'    => $pub,
                                'taxona' => $taxona,
                                'taxonb' => $taxonb,
                                'source' => $source,
                                'data'   => $data,
                                'genes'  => $genes,
                                'time'   => $time
                            );
                            ( $taxona, $taxonb, $source, $data, $genes, $time ) =
                            ( undef,   undef,   undef,   undef, undef,  undef );
                        }
                    }
                }            
            }            
        }
    }
}

package date;

our $AUTOLOAD;

sub new {
    my $class = shift;
    my %args  = @_;
    my $self = {
        'time'   => $args{'time'},
        'genes'  => $args{'genes'},
        'taxona' => $args{'taxona'},
        'taxonb' => $args{'taxonb'},
        'data'   => $args{'data'},
        'source' => $args{'source'},
        'pub'    => $args{'pub'},
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
