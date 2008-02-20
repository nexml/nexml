# $Id: XMLWritable.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Util::XMLWritable;
use strict;
use Bio::Phylo;
use Bio::Phylo::Util::Exceptions 'throw';
use vars '@ISA';
use UNIVERSAL 'isa';
@ISA=qw(Bio::Phylo);

my $logger = __PACKAGE__->get_logger;

my @fields = \( 
	my ( 
		%tag, 
		%id,
		%attributes,
    ) 
);

=head1 NAME

Bio::Phylo::Util::XMLWritable - Superclass for objects that stringify to xml

=head1 SYNOPSIS

 # no direct usage

=head1 DESCRIPTION

This class implements a single method, 'to_xml', that writes the invocant to
an xml string. Objects that subclass this class (all biological data objects
in Bio::Phylo) therefore can be written to xml. The 'to_xml' method sometimes
yields ugly (but valid) results, so subclasses may choose to provide their own
override.

=head1 METHODS

=head2 MUTATORS

=over

=item set_tag()

This method is usually only used internally, to define or alter the
name of the tag into which the object is serialized. For example,
for a Bio::Phylo::Forest::Node object, this method would be called 
with the 'node' argument, so that the object is serialized into an
xml element structure called <node/>

 Type    : Mutator
 Title   : set_tag
 Usage   : $obj->set_tag('node');
 Function: Sets the tag name
 Returns : $self
 Args    : A tag name (must be a valid xml element name)

=cut

	sub set_tag {
		my ( $self, $tag ) = @_;
		if ( $tag =~ qr/^[a-zA-Z]+$/ ) {
			$tag{ $self->get_id } = $tag;
			return $self;
		}
		else {
			throw 'BadString' => "'$tag' is not valid for xml";
		}
	}

=item set_attributes()

Assigns attributes for the element.

 Type    : Mutator
 Title   : set_attributes
 Usage   : $obj->set_attributes( 'foo' => 'bar' )
 Function: Sets the xml attributes for the object;
 Returns : $self
 Args    : key/value pairs or a hash ref

=cut

	sub set_attributes {
		my $self = shift;
		my %attrs;
		if ( scalar @_ == 1 and isa($_[0], 'HASH') ) {
			%attrs = %{ $_[0] };
		}
		elsif ( scalar @_ % 2 == 0 ) {
			%attrs = @_;	
		}
		else {
			throw 'OddHash' => 'Arguments are not even key/value pairs';	
		}		
		if ( my $hash = $attributes{ $self->get_id } ) {
			for my $key ( keys %attrs ) {
				$hash->{$key} = $attrs{$key};
			}
			$attributes{ $self->get_id } = $hash;
		}
		else {
			$attributes{ $self->get_id } = \%attrs;
		}
		return $self;
	}


=item set_xml_id()

This method is usually only used internally, to store the xml id
of an object as it is parsed out of a nexml file - this is for
the purpose of round-tripping nexml info sets.

 Type    : Mutator
 Title   : set_xml_id
 Usage   : $obj->set_xml_id('node345');
 Function: Sets the xml id
 Returns : $self
 Args    : An xml id (must be a valid xml NCName)

=cut

	sub set_xml_id {
		my ( $self, $id ) = @_;
		if ( $id =~ qr/^[a-zA-Z][a-zA-Z0-9]*$/ ) {
			$id{ $self->get_id } = $id;
			return $self;
		}
		else {
			throw 'BadString' => "'$id' is not a valid xml NCName";
		}
	}

=back

=head2 ACCESSORS

=over

=item get_tag()

Retrieves tag name for the element.

 Type    : Accessor
 Title   : get_tag
 Usage   : my $tag = $obj->get_tag;
 Function: Gets the xml tag name for the object;
 Returns : A tag name
 Args    : None.

=cut

	sub get_tag {
		my $self = shift;
		return $tag{ $self->get_id };
	}

=item get_xml_tag()

Retrieves tag string

 Type    : Accessor
 Title   : get_xml_tag
 Usage   : my $str = $obj->get_xml_tag;
 Function: Gets the xml tag for the object;
 Returns : A tag, i.e. pointy brackets
 Args    : Optional: a true value, to close an empty tag

=cut

	sub get_xml_tag {
		my $self = shift;
		my %attrs = %{ $self->get_attributes };
		my $tag = $self->get_tag;
		my $xml = '<' . $tag;
		for my $key ( keys %attrs ) {
			$xml .= ' ' . $key . '="' . $attrs{$key} . '"';
		}
		$xml .= shift(@_) ? '/>' : '>';
		return $xml;
	}

=item get_root_open_tag()

Retrieves tag string

 Type    : Accessor
 Title   : get_root_open_tag
 Usage   : my $str = $obj->get_root_open_tag;
 Function: Gets the nexml root tag
 Returns : A tag, i.e. pointy brackets
 Args    : 

=cut

	sub get_root_open_tag {
		my $class = __PACKAGE__;
		my $version = $Bio::Phylo::VERSION;
return qq'<nex:nexml 
	version="1.0" 
	generator="$class v.$version" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xml="http://www.w3.org/XML/1998/namespace"
	xsi:schemaLocation="http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd"
	xmlns:nex="http://www.nexml.org/1.0">';
	}

=item get_root_close_tag()

Retrieves tag string

 Type    : Accessor
 Title   : get_root_close_tag
 Usage   : my $str = $obj->get_root_close_tag;
 Function: Gets the nexml root tag
 Returns : A tag, i.e. pointy brackets
 Args    : 

=cut

	sub get_root_close_tag {
return '</nex:nexml>';
	}

=item get_attributes()

Retrieves attributes for the element.

 Type    : Accessor
 Title   : get_attributes
 Usage   : my %attrs = %{ $obj->get_attributes };
 Function: Gets the xml attributes for the object;
 Returns : A hash reference
 Args    : None.
 Comments: throws ObjectMismatch if no linked taxa object 
           can be found

=cut

	sub get_attributes {
		my $self = shift;
		my $attrs = $attributes{ $self->get_id } || {};
		if ( not exists $attrs->{'label'} and my $label = $self->get_name ) {
			$attrs->{'label'} = $label;
		}
		if ( not exists $attrs->{'id'} ) {
			$attrs->{'id'} = $self->get_xml_id;
		}
		if ( $self->can('get_taxa') ) {
			if ( my $taxa = $self->get_taxa ) {
				$attrs->{'otus'} = $taxa->get_xml_id;
			}
			else {
				throw 'ObjectMismatch' => "$self can link to a taxa element, but doesn't";
			}
		}
		if ( $self->can('get_taxon') ) {
			if ( my $taxon = $self->get_taxon ) {
				$attrs->{'otu'} = $taxon->get_xml_id;
			}
			else {
				$logger->info("No linked taxon found");
			}
		}
		return $attrs;
	}

=item get_xml_id()

Retrieves xml id for the element.

 Type    : Accessor
 Title   : get_xml_id
 Usage   : my $id = $obj->get_xml_id;
 Function: Gets the xml id for the object;
 Returns : An xml id
 Args    : None.

=cut

	sub get_xml_id {
		my $self = shift;
		if ( my $id = $id{ $self->get_id } ) {
			return $id;
		}		
		else {
			return $self->get_tag . $self->get_id;
		}
	}

=back

=head2 SERIALIZER

=over

=item to_xml()

Serializes invocant to XML.

 Type    : XML serializer
 Title   : to_xml
 Usage   : my $xml = $obj->to_xml;
 Function: Serializes $obj to xml
 Returns : An xml string
 Args    : None

=cut

	sub to_xml {
	    my $self = shift;
	    my $xml = $self->get_xml_tag;
		if ( $self->can('get_entities') ) {
			for my $ent ( @{ $self->get_entities } ) {
				$xml .= "\n" . $ent->to_xml;
			}
		}
		$xml .= sprintf( "</%s>", $self->get_tag );
		return $xml;
	}		

	sub _cleanup { 
    	my $self = shift;
		my $id = $self->get_id;
		for my $field (@fields) {
			delete $field->{$id};
		} 
	}

=back

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=head1 REVISION

 $Id: XMLWritable.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

1;