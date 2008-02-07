# $Id: Pagel.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Unparsers::Pagel;
use strict;
use Bio::Phylo::Forest::Tree;
use Bio::Phylo::IO;
use vars qw(@ISA);

@ISA=qw(Bio::Phylo::IO);

=head1 NAME

Bio::Phylo::Unparsers::Pagel - Unparses pagel data files. No serviceable parts
inside.

=head1 DESCRIPTION

This module unparses a Bio::Phylo data structure into an input file for
Discrete/Continuous/Multistate. The pagel file format (as it is interpreted
here) consists of:

=over

=item first line

the number of tips, the number of characters

=item subsequent lines

offspring name, parent name, branch length, character state(s).

=back

During unparsing, the tree is randomly resolved, and branch lengths are
formatted to %f floats (i.e. integers, decimal point, integers).

The pagel module is called by the L<Bio::Phylo::IO> object, so
look there to learn how to create Pagel formatted files.

=begin comment

 Type    : Constructor
 Title   : new
 Usage   : my $pagel = new Bio::Phylo::Unparsers::Pagel;
 Function: Initializes a Bio::Phylo::Unparsers::Pagel object.
 Alias   :
 Returns : A Bio::Phylo::Unparsers::Pagel object.
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

 Type    : Unparser
 Title   : to_string($tree)
 Usage   : $pagel->to_string($tree);
 Function: Unparses a Bio::Phylo::Tree object into a pagel formatted string.
 Returns : SCALAR
 Args    : Bio::Phylo::Tree

=end comment

=cut

sub _to_string {
    my $self = shift;
    my $tree = $self->{'PHYLO'};
    $tree->resolve;
    my ( $charcounter, $string ) = 0;
    foreach my $node ( @{ $tree->get_entities } ) {
        if ( $node->get_parent ) {
            $string .=
              $node->get_internal_name . ',' . $node->get_parent->get_internal_name . ',';
            if ( $node->get_branch_length ) {
                $string .= sprintf( "%f", $node->get_branch_length );
            }
            else {
                $string .= sprintf( "%f", 0 );
            }
            if ( $node->get_taxon ) {
                my $taxon = $node->get_taxon;
                foreach ( @{ $taxon->get_data } ) {
                    $string .= ',' . $_->get_char;
                    $charcounter++;
                }
            }
            $string .= "\n";
        }
        else {
            next;
        }
    }
    my $header = $tree->calc_number_of_terminals . " ";
    $header .= $charcounter / $tree->calc_number_of_terminals;
    $string = $header . "\n" . $string;
    return $string;
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The pagel unparser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to create pagel formatted files.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Pagel.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

1;
