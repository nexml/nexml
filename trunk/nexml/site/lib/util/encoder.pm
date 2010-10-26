package util::encoder;
use URI::Escape ();
use HTML::Entities ();

=back

=head1 NAME

util::encoder - website related string transformations

=head1 METHODS

=over

=item new()

=cut

sub new { return bless {}, shift }

=item uri_escape()

=cut

sub util::encoder::uri_escape { URI::Escape::uri_escape(pop) }

=item uri_unescape()

=cut

sub util::encoder::uri_unescape { URI::Escape::uri_unescape(pop) }

=item encode_entities()

=cut

sub util::encoder::encode_entities { HTML::Entities::encode_entities(pop) }

=item decode_entities()

=cut

sub util::encoder::decode_entities { HTML::Entities::decode_entities(pop) }

=item chomp()

=cut

sub util::encode::chomp { CORE::chomp($_[1]) }

=back

=head1 SEE ALSO

Also see the website: L<http://www.nexml.org>

=head1 REVISION

 $Id: util.pm 775 2009-02-22 21:30:40Z rvos $

=cut

1;