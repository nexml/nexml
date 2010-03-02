# $Id$
package Bio::Phylo::Unparsers::Newick;
use strict;
use Bio::Phylo::Forest::Tree ();
use Bio::Phylo::IO ();
use Bio::Phylo::Util::CONSTANT qw(:objecttypes);
use vars qw(@ISA);

@ISA=qw(Bio::Phylo::IO);

=head1 NAME

Bio::Phylo::Unparsers::Newick - Serializer used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module turns a tree object into a newick formatted (parenthetical) tree
description. It is called by the L<Bio::Phylo::IO> facade, don't call it
directly. You can pass the following additional arguments to the unparse
call:
	
	# by default, names for tips are derived from $node->get_name, if 
	# 'internal' is specified, uses $node->get_internal_name, if 'taxon'
	# uses $node->get_taxon->get_name, if 'taxon_internal' uses 
	# $node->get_taxon->get_internal_name, if $key, uses $node->get_generic($key)
	-tipnames => one of (internal|taxon|taxon_internal|$key)
	
	# for things like a translate table in nexus, or to specify truncated
	# 10-character names, you can pass a translate mapping as a hashref.
	# to generate the translated names, the strings obtained following the
	# -tipnames rules are used.
	-translate => { Homo_sapiens => 1, Pan_paniscus => 2 }	
	
	# array ref used to specify keys, which are embedded as key/value pairs (where
	# the value is obtained from $node->get_generic($key)) in comments, 
	# formatted depending on '-nhxstyle', which could be 'nhx' (default), i.e.
	# [&&NHX:$key1=$value1:$key2=$value2] or 'mesquite', i.e. 
	# [% $key1 = $value1, $key2 = $value2 ]
	-nhxkeys => [ $key1, $key2 ]	
	
	# if set, appends labels to internal nodes (names obtained from the same
	# source as specified by '-tipnames')
	-nodelabels => 1
	
	# specifies a formatting style / dialect
	-nhxstyle => one of (mesquite|nhx)
	
	# specifies a branch length sprintf number formatting template, default is %f
	-blformat => '%e'

=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $newick = Bio::Phylo::Unparsers::Newick->_new;
 Function: Initializes a Bio::Phylo::Unparsers::Newick object.
 Returns : A Bio::Phylo::Unparsers::Newick object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;
    my $self  = {};
    if (@_) {
        my %opts = @_;
        foreach my $key ( keys %opts ) {
            my $localkey = uc $key;
            $localkey =~ s/-//;
            unless ( ref $opts{$key} ) {
                $self->{$localkey} = uc $opts{$key};
            }
            else {
                $self->{$localkey} = $opts{$key};
            }
        }
    }
    bless $self, $class;
    return $self;
}

=begin comment

 Type    : Wrapper
 Title   : _to_string($tree)
 Usage   : $newick->_to_string($tree);
 Function: Prepares for the recursion to unparse the tree object into a
           newick string.
 Alias   :
 Returns : SCALAR
 Args    : Bio::Phylo::Forest::Tree

=end comment

=cut

sub _to_string {
    my $self = shift;
    my $tree = $self->{'PHYLO'};
    my $type = $tree->_type;
    if ( $type == _TREE_ ) {
        my $root = $tree->get_root;
        my %args;
        for my $key ( qw(TRANSLATE TIPNAMES NHXKEYS NODELABELS BLFORMAT NHXSTYLE) ) {
            if ( my $val = $self->{$key} ) {
                my $arg = '-' . lc($key);
                $args{$arg} = $val;
            }
        } 
        return $root->to_newick( %args );
    }
    elsif ( $type == _FOREST_ ) {
        my $forest = $tree;
        my $newick = "";
        for my $tree ( @{ $forest->get_entities } ) {
            my $root = $tree->get_root;
            my %args;
            for my $key ( qw(TRANSLATE TIPNAMES NHXKEYS NODELABELS BLFORMAT NHXSTYLE) ) {
                if ( my $val = $self->{$key} ) {
                    my $arg = '-' . lc($key);
                    $args{$arg} = $val;
                }
            } 
            $newick .= $root->to_newick( %args ) . "\n";        
        }
        return $newick;
    }
    elsif ( $type == _PROJECT_ ) {
        my $project = $tree;
        my $newick = "";
        
        for my $forest ( @{ $project->get_forests } ) {
            for my $tree ( @{ $forest->get_entities } ) {
                my $root = $tree->get_root;
                my %args;
                for my $key ( qw(TRANSLATE TIPNAMES NHXKEYS NODELABELS BLFORMAT NHXSTYLE) ) {
                    if ( my $val = $self->{$key} ) {
                        my $arg = '-' . lc($key);
                        $args{$arg} = $val;
                    }
                } 
                $newick .= $root->to_newick( %args ) . "\n";        
            }
        }
        
        return $newick;
    }    
}

=begin comment

 Type    : Unparser
 Title   : __to_string
 Usage   : $newick->__to_string($tree, $node);
 Function: Unparses the tree object into a newick string.
 Alias   :
 Returns : SCALAR
 Args    : A Bio::Phylo::Forest::Tree object. Optional: A Bio::Phylo::Forest::Node
           object, the starting point for recursion.

=end comment

=cut

{
    my $string = q{};
    #no warnings 'uninitialized';

    sub __to_string {
        my ( $self, $tree, $n ) = @_;
        if ( !$n->get_parent ) {
            if ( defined $n->get_branch_length ) {
                $string = $n->get_name . ':' . $n->get_branch_length . ';';
            }
            else {
                $string = $n->get_name ? $n->get_name . ';' : ';';
            }
        }
        elsif ( !$n->get_previous_sister ) {
            if ( defined $n->get_branch_length ) {
                $string = $n->get_name . ':' . $n->get_branch_length . $string;
            }
            else { $string = $n->get_name . $string; }
        }
        else {
            if ( defined $n->get_branch_length ) {
                $string =
                  $n->get_name . ':' . $n->get_branch_length . ',' . $string;
            }
            else { $string = $n->get_name . ',' . $string; }
        }
        if ( $n->get_first_daughter ) {
            $n      = $n->get_first_daughter;
            $string = ')' . $string;
            $self->__to_string( $tree, $n );
            while ( $n->get_next_sister ) {
                $n = $n->get_next_sister;
                $self->__to_string( $tree, $n );
            }
            $string = '(' . $string;
        }
    }
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The newick unparser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to unparse newick strings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
