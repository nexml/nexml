package Bio::Phylo::Project;
use Bio::Phylo::Listable ();
use Bio::Phylo::Util::CONSTANT qw(:objecttypes looks_like_object looks_like_instance);
use Bio::Phylo::Util::Exceptions 'throw';
#use UNIVERSAL 'isa';
use vars '@ISA';
use strict;
@ISA=qw(Bio::Phylo::Listable);

=head1 NAME

Bio::Phylo::Project - Container for related data

=head1 SYNOPSIS

 use Bio::Phylo::Factory;
 my $fac  = Bio::Phylo::Factory->new;
 my $proj = $fac->create_project;
 my $taxa = $fac->create_taxa;
 $proj->insert($taxa);
 $proj->insert($fac->create_matrix->set_taxa($taxa));
 $proj->insert($fac->create_forest->set_taxa($taxa));
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
			'version'   => '0.8',
			'generator' => "$class v.$version",			
			'xmlns'     => 'http://www.nexml.org/1.0',			
			'xsi:schemaLocation' => 'http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd',
		},
		'-identifiable' => 0,
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

=item get_document()

 Type    : Serializer
 Title   : doc
 Usage   : $proj->get_document()
 Function: Creates a DOM Document object, containing the 
           present state of the project by default
 Returns : a Document object
 Args    : a DOM factory object
           Optional: pass 1 to obtain a document node without 
           content

=cut

    sub get_document {
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( looks_like_instance($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::Util::DOM::DOM;
		    unless ($dom) {
				throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
	###	# make sure argument handling works here...
		my $empty = shift @args;
		my $doc = $dom->create_document();
		my $root;
	
		unless ($empty) {
		    $root = $self->to_dom($dom);
		    $doc->set_root($root);
		}
		return $doc;
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
		my $xml = $self->get_xml_tag;
		my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
		my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
		for ( values %taxa, @linked ) {
			$xml .= $_->to_xml(@_);
		}
		$xml .= '</' . $self->get_tag . '>';
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

=item to_dom()

 Type    : Serializer
 Title   : to_dom
 Usage   : $node->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : a DOM factory object

=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $elt = $self->get_dom_elt($dom);
	
		my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
		my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
		for ( values %taxa, @linked ) {
		    $elt->set_child( $_->to_dom($dom, @_) );
		}
		return $elt;
    }
	
	sub _type { $TYPE }

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Listable>

The L<Bio::Phylo::Project> object inherits from the L<Bio::Phylo::Listable>
object. Look there for more methods applicable to the project object.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

}

