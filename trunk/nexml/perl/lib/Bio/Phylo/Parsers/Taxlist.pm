# $Id$
package Bio::Phylo::Parsers::Taxlist;
use strict;
use Bio::Phylo::Factory;
use Bio::Phylo::IO;
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::IO);

my $fac = Bio::Phylo::Factory->new;

=head1 NAME

Bio::Phylo::Parsers::Taxlist - Parser used by Bio::Phylo::IO, no serviceable parts inside
inside.

=head1 DESCRIPTION

This module is used for importing sets of taxa from plain text files, one taxon
on each line. It is called by the L<Bio::Phylo::IO|Bio::Phylo::IO> object, so
look there for usage examples. If you want to parse from a string, you
may need to indicate the field separator (default is '\n') to the
Bio::Phylo::IO->parse call:

 -fieldsep => '\n',

=begin comment

 Type    : Constructor
 Title   : new
 Usage   : my $taxlist = Bio::Phylo::Parsers::Taxlist->new;
 Function: Initializes a Bio::Phylo::Parsers::Taxlist object.
 Returns : A Bio::Phylo::Parsers::Taxlist object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = $_[0];
    my $self  = {};
    bless $self, $class;
    return $self;
}

=begin comment

 Type    : parser
 Title   : from_handle(%options)
 Usage   : $taxlist->from_handle(%options);
 Function: Reads taxon names from file, populates taxa object
 Returns : A Bio::Phylo::Taxa object.
 Args    : -handle => (\*FH), -file => (filename)
 Comments:

=end comment

=cut

*_from_handle = \&_from_both;
*_from_string = \&_from_both;

sub _from_both {
    my $self = shift;
    my %opts = @_;
    if ( !$opts{'-fieldsep'} ) {
        $opts{'-fieldsep'} = "\n";
    }
    my $taxa = $fac->create_taxa;
    if ( $opts{'-handle'} ) {
        while ( readline $opts{'-handle'} ) {
            chomp;
            if ($_) {
                $taxa->insert( $fac->create_taxon( -name => $_ ) );
            }
        }
    }
    elsif ( $opts{'-string'} ) {
        foreach ( split /$opts{'-fieldsep'}/, $opts{'-string'} ) {
            chomp;
            if ($_) {
                $taxa->insert( $fac->create_taxon( -name => $_ ) );
            }
        }
    }
    if ( $opts{'-project'} ) {
    	$opts{'-project'}->insert($taxa);
    	return $opts{'-project'};    	
    }
    elsif ( $opts{'-as_project'} ) {
    	my $proj = $fac->create_project;
    	$proj->insert($taxa);
    	return $proj;
    }
    else {
    	return $taxa;
    }
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The taxon list parser is called by the L<Bio::Phylo::IO> object.
Look there for examples.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
