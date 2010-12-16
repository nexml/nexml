# $Id$
package Bio::Phylo::NeXML::Writable;
use strict;
use Bio::Phylo ();
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::NeXML::DOM;
use Bio::Phylo::Util::CONSTANT qw(
	_DICTIONARY_ 
	_META_ 
	_DOMCREATOR_ 
	looks_like_object 
	looks_like_hash
	looks_like_instance
	looks_like_implementor
	:namespaces
);
use vars '@ISA';
@ISA=qw(Bio::Phylo);

{

    my $logger = __PACKAGE__->get_logger;
    my $DICTIONARY_CONSTANT = _DICTIONARY_;
    my $META_CONSTANT = _META_;
    my %namespaces = (
    	'nex' => _NS_NEXML_,
    	'xml' => _NS_XML_,
    	'xsi' => _NS_XSI_,
    	'rdf' => _NS_RDF_,
    	'xsd' => _NS_XSD_,
    );
    my @fields = \( my ( %tag, %id, %attributes, %identifiable, %suppress_ns, %meta ) );

=head1 NAME

Bio::Phylo::NeXML::Writable - Superclass for objects that serialize to NeXML

=head1 SYNOPSIS

 # no direct usage

=head1 DESCRIPTION

This is the superclass for all objects that can be serialized to NeXML 
(L<http://www.nexml.org>).

=head1 METHODS

=head2 MUTATORS

=over

=item set_namespaces()

 Type    : Mutator
 Title   : set_namespaces
 Usage   : $obj->set_namespaces( 'dwc' => 'http://www.namespaceTBD.org/darwin2' );
 Function: Adds one or more prefix/namespace pairs
 Returns : $self
 Args    : One or more prefix/namespace pairs, as even-sized list, 
           or as a hash reference, i.e.:
           $obj->set_namespaces( 'dwc' => 'http://www.namespaceTBD.org/darwin2' );
           or
           $obj->set_namespaces( { 'dwc' => 'http://www.namespaceTBD.org/darwin2' } );
 Notes   : This is a global for the XMLWritable class, so that in a recursive
 		   to_xml call the outermost element contains the namespace definitions.
 		   This method can also be called as a static class method, i.e.
 		   Bio::Phylo::NeXML::Writable->set_namespaces(
 		   'dwc' => 'http://www.namespaceTBD.org/darwin2');

=cut

	sub set_namespaces {
		my $self = shift;
		if ( scalar(@_) == 1 and ref($_[0]) eq 'HASH' ) {
			my $hash = shift;
			for my $key ( keys %{ $hash } ) {
				$namespaces{$key} = $hash->{$key};
			}
		}
		elsif ( my %hash = looks_like_hash @_ ) {
			for my $key ( keys %hash ) {
				$namespaces{$key} = $hash{$key};
			}			
		}
	}

=item set_suppress_ns()

 Type    : Mutator
 Title   : set_suppress_ns
 Usage   : $obj->set_suppress_ns();
 Function: Tell this object not to write namespace attributes
 Returns : 
 Args    : none

=cut

	sub set_suppress_ns {
	    my $self = shift;
	    my $id = $self->get_id;
	    $suppress_ns{$id} = 1;
	}

=item clear_suppress_ns()

 Type    : Mutator
 Title   : clear_suppress_ns
 Usage   : $obj->clear_suppress_ns();
 Function: Tell this object to write namespace attributes
 Returns : 
 Args    : none

=cut

	sub clear_suppress_ns {
	    my $self = shift;
	    my $id = $self->get_id;
	    $suppress_ns{$id} = 0;
	}

=item add_meta()

 Type    : Mutator
 Title   : add_meta
 Usage   : $obj->add_meta($meta);
 Function: Adds a metadata attachment to the object
 Returns : $self
 Args    : A Bio::Phylo::NeXML::Meta object

=cut
    
    sub add_meta {
        my ( $self, $meta_obj ) = @_;
        if ( looks_like_object $meta_obj, $META_CONSTANT ) {
            my $id = $self->get_id;
            if ( not $meta{$id} ) {            	
                $meta{$id} = [];
            }
            push @{ $meta{$id} }, $meta_obj;
            $self->set_attributes( 'about' => '#' . $self->get_xml_id );
        }
        return $self;
    }

=item remove_meta()

 Type    : Mutator
 Title   : remove_meta
 Usage   : $obj->remove_meta($meta);
 Function: Removes a metadata attachment from the object
 Returns : $self
 Args    : Bio::Phylo::NeXML::Meta

=cut

    sub remove_meta {
        my ( $self, $meta ) = @_;
        my $id = $self->get_id;
        my $meta_id = $meta->get_id;
        if ( $meta{$id} ) {
            DICT: for my $i ( 0 .. $#{ $meta{$id} } ) {
                if ( $meta{$id}->[$i]->get_id == $meta_id ) {
                    splice @{ $meta{$id} }, $i, 1;
                    last DICT;
                }
            }
        }
        if ( not $meta{$id} or not @{ $meta{$id} } ) {
        	$self->unset_attribute( 'about' );
        }
        return $self;
    }

=item set_identifiable()

By default, all XMLWritable objects are identifiable when serialized,
i.e. they have a unique id attribute. However, in some cases a serialized
object may not have an id attribute (governed by the nexml schema). For
such objects, id generation can be explicitly disabled using this method.
Typically, this is done internally - you will probably never use this method.

 Type    : Mutator
 Title   : set_identifiable
 Usage   : $obj->set_identifiable(0);
 Function: Enables/disables id generation
 Returns : $self
 Args    : BOOLEAN

=cut

    sub set_identifiable {
        my $self = shift;
        $identifiable{ $self->get_id } = !!shift;
        return $self;
    }

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
		# _ is ok; see http://www.w3.org/TR/2004/REC-xml-20040204/#NT-NameChar
		if ( $tag =~ qr/^[a-zA-Z]+\:?[a-zA-Z_]*$/ ) {
			$tag{ $self->get_id } = $tag;
			return $self;
		}
		else {
			throw 'BadString' => "'$tag' is not valid for xml";
		}
	}

=item set_name()

Sets invocant name.

 Type    : Mutator
 Title   : set_name
 Usage   : $obj->set_name($name);
 Function: Assigns an object's name.
 Returns : Modified object.
 Args    : Argument must be a string. Ensure that this string is safe to use for
           whatever output format you want to use (this differs between xml and
           nexus, for example).

=cut

	sub set_name {
		my ( $self, $name ) = @_;
		if ( defined $name ) {
			return $self->set_attributes( 'label' => $name );
		}
		else {
			return $self;
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
		my $id = $self->get_id;
		my %attrs;
		if ( scalar @_ == 1 and ref $_[0] eq 'HASH' ) {
			%attrs = %{ $_[0] };
		}
		elsif ( scalar @_ % 2 == 0 ) {
			%attrs = @_;	
		}
		else {
			throw 'OddHash' => 'Arguments are not even key/value pairs';	
		}		
		my $hash = $attributes{$id} || {};
		my $fully_qualified_attribute_regex = qr/^(.+?):(.+)/;
		for my $key ( keys %attrs ) {
			if ( $key =~ $fully_qualified_attribute_regex ) {
				my ( $prefix, $attribute ) = ( $1, $2 );
				if ( $prefix ne 'xmlns' and not exists $namespaces{$prefix} ) {
					$logger->warn("Attribute '${prefix}:${attribute}' is not bound to a namespace");
				}
			}
			$hash->{$key} = $attrs{$key};
		}
		$attributes{$id} = $hash;
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
		if ( $id =~ qr/^[a-zA-Z][a-zA-Z0-9\-_\.]*$/ ) {
			$id{ $self->get_id } = $id;
			return $self;
		}
		else {
			throw 'BadString' => "'$id' is not a valid xml NCName for $self";
		}
	}

=item unset_attribute()

Removes specified attribute

 Type    : Mutator
 Title   : unset_attribute
 Usage   : $obj->unset_attribute( 'foo' )
 Function: Removes the specified xml attribute for the object
 Returns : $self
 Args    : an attribute name

=cut

	sub unset_attribute {
		my $self = shift;
		my $attrs = $attributes{ $self->get_id };
		if ( $attrs and looks_like_instance($attrs,'HASH') ) {
			delete $attrs->{$_} for @_;
		}
		return $self;
	}

=back

=head2 ACCESSORS

=over

=item get_namespaces()

 Type    : Accessor
 Title   : get_namespaces
 Usage   : my %ns = %{ $obj->get_namespaces };
 Function: Retrieves the known namespaces
 Returns : A hash of prefix/namespace key/value pairs, or
           a single namespace if a single, optional
           prefix was provided as argument
 Args    : Optional - a namespace prefix

=cut

	sub get_namespaces { 
		my ($self,$prefix) = @_;
		if ( $prefix ) {
			return $namespaces{$prefix};
		}
		else {
			my %tmp_namespaces = %namespaces;
			return \%tmp_namespaces;
		} 
	}

=item get_meta()

Retrieves the metadata for the element.

 Type    : Accessor
 Title   : get_meta
 Usage   : my @meta = @{ $obj->get_meta };
 Function: Retrieves the metadata for the element.
 Returns : An array ref of Bio::Phylo::NeXML::Meta objects
 Args    : None.

=cut

    sub get_meta {
        my $self = shift;
#        $logger->debug("getting meta for $self");
        my $id = $self->get_id;
        return $meta{$id} || [];
    }

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
		if ( my $tagstring = $tag{ $self->get_id } ) {
			return $tagstring;
		}
		elsif ( looks_like_implementor $self, '_tag' ) {
			return $self->_tag;
		}
		else {
			return '';
		}
	}

=item get_name()

Gets invocant's name.

 Type    : Accessor
 Title   : get_name
 Usage   : my $name = $obj->get_name;
 Function: Returns the object's name.
 Returns : A string
 Args    : None

=cut
	
	sub get_name {
		my $self = shift;
		my $id = $self->get_id;
		if ( ! $attributes{$id} ) {			
			$attributes{$id} = {};
		}
		if ( defined $attributes{$id}->{'label'} ) {
			return $attributes{$id}->{'label'};		
		}
		else {
			return '';
		}
		
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
		my ($self, $closeme) = @_;
		my %attrs = %{ $self->get_attributes };
		my $tag = $self->get_tag;
		my $xml = '<' . $tag;
		for my $key ( keys %attrs ) {
			$xml .= ' ' . $key . '="' . $attrs{$key} . '"';
		}
		my $has_contents = 0;
		my $meta = $self->get_meta;
		if ( @{ $meta } ) {
			$xml .= '>';# if not @{ $dictionaries };
			$xml .= $_->to_xml for @{ $meta };
			$has_contents++			
		}
		if ( looks_like_implementor $self,'get_sets' ) {
			my $sets = $self->get_sets;
			if ( @{ $sets } ) {
				$xml .= '>' if not @{ $meta };
				$xml .= $_->to_xml for @{ $sets };
				$has_contents++;
			}
		}
		if ( $has_contents ) {
			$xml .= "</$tag>" if $closeme;
		}
		else {
			$xml .= $closeme ? '/>' : '>';
		}
		return $xml;
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

	my $SAFE_CHARACTERS_REGEX = qr/(?:[a-zA-Z0-9]|-|_|\.)/;
	my $XMLEntityEncode = sub {
		my $buf = '';
		for my $c ( split //, shift ) {
			if ( $c =~ $SAFE_CHARACTERS_REGEX ) {
				$buf .= $c;
			}
			else {
				$buf .= '&#' . ord($c) . ';';
			}			
		}
		return $buf;
	};
	
	my $add_namespaces_to_attributes = sub {
		my ( $self, $attrs ) = @_;
		my $i = 0;
		my $inside_to_xml_recursion = 0;
		CHECK_RECURSE: while ( my @frame = caller($i) ) {
			if ( $frame[3] =~ m/::to_xml$/ ) {
				$inside_to_xml_recursion++;
				last CHECK_RECURSE if $inside_to_xml_recursion > 1;
			}
			$i++;
		}
		if ( $inside_to_xml_recursion <= 1 ) {
			my $tmp_namespaces = get_namespaces();
			for my $ns ( keys %{ $tmp_namespaces } ) {
				$attrs->{'xmlns:' . $ns} = $tmp_namespaces->{$ns};
			}			
		}	
		return $attrs;	
	};
	
	my $flatten_attributes = sub {
		my $self = shift;
		my $tempattrs = $attributes{ $self->get_id };
		my $attrs;
		if ( $tempattrs ) {
			my %deref = %{ $tempattrs };
			$attrs = \%deref;
		}
		else {
			$attrs = {};
		}
		return $attrs;		
	};

	sub get_attributes {
		my $self = shift;
		my $attrs = $flatten_attributes->($self);
		if ( not exists $attrs->{'label'} and my $label = $self->get_name ) {
			$attrs->{'label'} = $XMLEntityEncode->($label);
		}
		if ( not exists $attrs->{'id'} ) {
			$attrs->{'id'} = $self->get_xml_id;
		}
		if ( $self->can('_get_container') ) {
			my $container = $self->_get_container;
			if ( $self->can('get_tree') ) {
				$container = $self->get_tree;
			}
			if ( $container ) {
				my @classes;
				for my $set ( @{ $container->get_sets } ) {
					if ( $container->is_in_set($self,$set) ) {
						push @classes, $set->get_xml_id;
					}
				} 
				$attrs->{'class'} = join ' ', @classes if scalar(@classes);
			}
		}
		if ( defined $self->is_identifiable and not $self->is_identifiable ) {
		    delete $attrs->{'id'};
		}
		if ( $self->can('get_taxa') ) {
			if ( my $taxa = $self->get_taxa ) {
				$attrs->{'otus'} = $taxa->get_xml_id if looks_like_instance($taxa,'Bio::Phylo');
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
		$attrs = $add_namespaces_to_attributes->($self,$attrs) unless $self->is_ns_suppressed;
		my $arg = shift;
		if ( $arg ) {
		    return $attrs->{$arg};
		}
		else {
		    return $attrs;
		}
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
			my $tag = $self->get_tag;
			$tag =~ s/:/_/;
			return $tag . $self->get_id;
		}
	}

=item get_dom_elt()

 Type    : Serializer
 Title   : get_dom_elt
 Usage   : $obj->get_dom_elt
 Function: Generates a DOM element from the invocant
 Returns : a DOM element object (default XML::Twig)
 Args    : DOM factory object

=cut

    sub get_dom_elt {
		my ($self,$dom) = @_;
		$dom ||= Bio::Phylo::NeXML::DOM->get_dom;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $elt = $dom->create_element( '-tag' => $self->get_tag );
		my %attrs = %{ $self->get_attributes };
		for my $key ( keys %attrs ) {
		    $elt->set_attributes( $key => $attrs{$key} );
		}
	
		for my $meta ( @{ $self->get_meta } ) {
			$elt->set_child( $meta->to_dom($dom) );
		}
		#my $dictionaries = $self->get_dictionaries;
		#if ( @{ $dictionaries } ) {
		#    $elt->set_child( $_->to_dom($dom) ) for @{ $dictionaries };
		#}
		if ( looks_like_implementor $self,'get_sets' ) {
		    my $sets = $self->get_sets;
		    $elt->set_child( $_->to_dom($dom) ) for @{ $sets };
		}
		return $elt;
    }

=back

=head2 TESTS

=over

=item is_identifiable()

By default, all XMLWritable objects are identifiable when serialized,
i.e. they have a unique id attribute. However, in some cases a serialized
object may not have an id attribute (governed by the nexml schema). This
method indicates whether that is the case.

 Type    : Test
 Title   : is_identifiable
 Usage   : if ( $obj->is_identifiable ) { ... }
 Function: Indicates whether IDs are generated
 Returns : BOOLEAN
 Args    : NONE

=cut

    sub is_identifiable {
        my $self = shift;
        return $identifiable{ $self->get_id };
    }

=item is_ns_suppressed()

 Type    : Test
 Title   : is_ns_suppressed
 Usage   : if ( $obj->is_ns_suppressed ) { ... }
 Function: Indicates whether namespace attributes should not
           be written on XML serialization
 Returns : BOOLEAN
 Args    : NONE

=cut

	sub is_ns_suppressed {
	    return $suppress_ns{ shift->get_id }
	}
	
=back

=head2 CLONER

=over

=item clone()

Clones invocant.

 Type    : Utility method
 Title   : clone
 Usage   : my $clone = $object->clone;
 Function: Creates a copy of the invocant object.
 Returns : A copy of the invocant.
 Args    : NONE.
 Comments: Cloning is currently experimental, use with caution.

=cut

	sub clone {
		my $self = shift;
		$logger->info("cloning $self");
		my %subs = @_;
		
		# some extra logic to copy characters from source to target
		if ( not exists $subs{'add_meta'} ) {
			$subs{'add_meta'} = sub {
				my ( $obj, $clone ) = @_;
				for my $meta ( @{ $obj->get_meta } ) {
					$clone->add_meta( $meta );
				}
			};	
		}
		
		return $self->SUPER::clone(%subs);
	
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
 Args    : None

=cut

	sub to_xml {
	    my $self = shift;
	    my $xml = '';
		if ( $self->can('get_entities') ) {
			for my $ent ( @{ $self->get_entities } ) {
				if ( looks_like_implementor $ent,'to_xml' ) {					
					$xml .= "\n" . $ent->to_xml;
				}
			}
		}
		if ( $xml ) {
			$xml = $self->get_xml_tag . $xml . sprintf( "</%s>", $self->get_tag );
		}
		else {
			$xml = $self->get_xml_tag(1);
		}
		return $xml;
	}		

=item to_dom()

 Type    : Serializer
 Title   : to_dom
 Usage   : $obj->to_dom
 Function: Generates a DOM subtree from the invocant and
           its contained objects
 Returns : a DOM element object (default: XML::Twig flavor)
 Args    : DOM factory object
 Note    : This is the generic function. It is redefined in the 
           classes below.
=cut

	sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= Bio::Phylo::NeXML::DOM->get_dom;
		if ( looks_like_object $dom, _DOMCREATOR_ ) {
			my $elt = $self->get_dom_elt($dom);
			if ( $self->can('get_entities') ) {
			    for my $ent ( @{ $self->get_entities } ) {
				if ( looks_like_implementor $ent,'to_dom' ) { 
					$elt->set_child( $ent->to_dom($dom) );
				}
			    }
			}
			return $elt;                
		}
		else {
			throw 'BadArgs' => 'DOM factory object not provided';
		}
	}

=item to_json()

Serializes object to JSON string

 Type    : Serializer
 Title   : to_json()
 Usage   : print $obj->to_json();
 Function: Serializes object to JSON string
 Returns : String 
 Args    : None
 Comments:

=cut

    sub to_json { 
	my $self = shift;
    	eval { require XML::XML2JSON };
    	if ( $@ ) {
    		throw 'ExtensionError' => "Can't load XML::XML2JSON - $@";    		
    	}
	return XML::XML2JSON->new->convert($self->to_xml);
    }

	sub _cleanup { 
		my $self = shift;
		my $id = $self->get_id;
		for my $field (@fields) {
			delete $field->{$id};
		} 
	}

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=head1 REVISION

 $Id$

=cut

}

1;
