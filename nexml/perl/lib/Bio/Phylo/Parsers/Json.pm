# $Id: Table.pm 1593 2011-02-27 15:26:04Z rvos $
package Bio::Phylo::Parsers::Json;
use strict;
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Parsers::Abstract;
use Bio::Phylo::Util::Exceptions 'throw';
use vars qw(@ISA);

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::Parsers::Abstract);

eval { require XML::XML2JSON };
if ( $@ ) {
    throw 'ExtensionError' => "Error loading the XML::XML2JSON extension: $@";
}

=head1 NAME

Bio::Phylo::Parsers::Json - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module is used to import NeXML data that was re-formatted as JSON, using
the mapping implemented by L<XML::XML2JSON>.

=cut

sub _parse {
    my $self = shift;
    my $fh   = $self->_handle;
    my $json = do { local $/; <$fh> };
    my $conf = XML::XML2JSON->new;
    my $xml  = $conf->json2xml($json);
    return @{ parse( '-format' => 'nexml', '-string' => $xml ) };
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The json parser is called by the L<Bio::Phylo::IO|Bio::Phylo::IO> object.
Look there to learn how to parse data using Bio::Phylo.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=back

=head1 CITATION

If you use Bio::Phylo in published research, please cite it:

B<Rutger A Vos>, B<Jason Caravas>, B<Klaas Hartmann>, B<Mark A Jensen>
and B<Chase Miller>, 2011. Bio::Phylo - phyloinformatic analysis using Perl.
I<BMC Bioinformatics> B<12>:63.
L<http://dx.doi.org/10.1186/1471-2105-12-63>

=head1 REVISION

 $Id: Table.pm 1593 2011-02-27 15:26:04Z rvos $

=cut

1;
