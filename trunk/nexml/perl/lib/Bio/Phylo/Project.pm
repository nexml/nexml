package Bio::Phylo::Project;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT qw(:objecttypes);
use Bio::Phylo::Util::Exceptions 'throw';
use vars '@ISA';
use strict;
@ISA=qw(Bio::Phylo::Listable);

=head1 NAME

Bio::Phylo::Project - Container object to collect related data

=head1 SYNOPSIS

 use Bio::Phylo::Project;
 use Bio::Phylo::Factory;
 my $fac = Bio::Phylo::Factory->new;
 my $proj = Bio::Phylo::Project->new;
 $proj->insert($fac->create_taxa);
 $proj->insert($fac->create_matrix);
 $proj->insert($fac->create_forest)
 print $proj->to_xml;

=head1 DESCRIPTION

The project module is used to collect taxa blocks, tree blocks and
matrices.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Project constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $project = Bio::Phylo::Project->new;
 Function: Instantiates a Bio::Phylo::Project
           object.
 Returns : A Bio::Phylo::Project object.
 Args    : none.

=cut

sub new {
	my $class = shift;
	my $version = $class->VERSION;
	my %args = (
		'-tag'        => 'nex:nexml',
		'-attributes' => {
			'version'   => '1.0',
			'generator' => "$class v.$version",
			'xmlns:xsi' => 'http://www.w3.org/2001/XMLSchema-instance',
			'xmlns:xml' => 'http://www.w3.org/XML/1998/namespace',
			'xmlns:nex' => 'http://www.nexml.org/1.0',			
			'xsi:schemaLocation' => 'http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd',
		}
	);
	return $class->SUPER::new(%args,@_);
}

=back

=head2 ACCESSORS

=over

=cut

{
	my $TYPE = _PROJECT_;
	my $TAXA = _TAXA_;
	my $FOREST = _FOREST_;
	my $MATRIX = _MATRIX_;

	my $get_object = sub {
		my ( $self, $CONSTANT ) = @_;
		my @result;
		for my $ent ( @{ $self->get_entities } ) {
			if ( $ent->_type == $CONSTANT ) {
				push @result, $ent;
			}			
		}	
		return \@result;
	};
	
=item get_taxa()

Getter for taxa objects

 Type    : Constructor
 Title   : get_taxa
 Usage   : my $taxa = $proj->get_taxa;
 Function: Getter for taxa objects
 Returns : An array reference of taxa objects
 Args    : NONE.

=cut	
	
	sub get_taxa {
		my $self = shift;
		return $get_object->($self,$TAXA);
	}
	
=item get_forests()

Getter for forest objects

 Type    : Constructor
 Title   : get_forests
 Usage   : my $forest = $proj->get_forests;
 Function: Getter for forest objects
 Returns : An array reference of forest objects
 Args    : NONE.

=cut		
	
	sub get_forests {
		my $self = shift;
		return $get_object->($self,$FOREST);
	}
	
=item get_matrices()

Getter for matrix objects

 Type    : Constructor
 Title   : get_matrices
 Usage   : my $matrix = $proj->get_matrices;
 Function: Getter for matrix objects
 Returns : An array reference of matrix objects
 Args    : NONE.

=cut	
	
	sub get_matrices {
		my $self = shift;
		return $get_object->($self,$MATRIX);		
	}

=back

=head2 SERIALIZERS

=over

=item to_xml()

Serializes invocant to XML.

 Type    : XML serializer
 Title   : to_xml
 Usage   : my $xml = $obj->to_xml;
 Function: Serializes $obj to xml
 Returns : An xml string
 Args    : Same arguments as can be passed to individual contained objects

=cut
	
	sub to_xml {
		my $self = shift;
		my $xml = $self->get_root_open_tag;
		my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
		my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
		for ( values %taxa, @linked ) {
			$xml .= $_->to_xml(@_);
		}
		$xml .= $self->get_root_close_tag;
		eval { require XML::Twig };
		if ( not $@ ) {
			my $twig = XML::Twig->new( 'pretty_print' => 'indented' );
			eval { $twig->parse($xml) };
			if ( $@ ) {
				throw 'API' => "Couldn't build xml: " . $@;
			}
			else {
				return $twig->sprint;
			}
		}
		else {
			undef $@;
			return $xml;
		}
	}

=item to_nexus()

Serializes invocant to NEXUS.

 Type    : NEXUS serializer
 Title   : to_nexus
 Usage   : my $nexus = $obj->to_nexus;
 Function: Serializes $obj to nexus
 Returns : An nexus string
 Args    : Same arguments as can be passed to individual contained objects

=cut
	
	sub to_nexus {
		my $self = shift;
		my $nexus = "#NEXUS\n";
		my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
		my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
		for ( values %taxa, @linked ) {
			$nexus .= $_->to_nexus(@_);
		}	
		return $nexus;	
	}
	
	sub _type { $TYPE }

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Listable>

The L<Bio::Phylo::Project> object inherits from the L<Bio::Phylo::Listable>
object. Look there for more methods applicable to the project object.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Taxa.pm 604 2008-09-05 17:32:28Z rvos $

=cut

}

