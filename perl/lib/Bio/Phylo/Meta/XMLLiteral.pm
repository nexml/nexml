package Bio::Phylo::Meta::XMLLiteral;
use Bio::Phylo::Util::CONSTANT qw'_META_';
use Bio::Phylo::Util::Exceptions 'throw';
use UNIVERSAL qw'isa can';
{
    my $TYPE_CONSTANT      = _META_;
    my $CONTAINER_CONSTANT = $TYPE_CONSTANT;
    
=head1 NAME

Bio::Phylo::Meta::XMLLiteral - Annotation value adaptor, no direct usage

=head1 SYNOPSIS

 # no direct usage

=head1 DESCRIPTION

No direct usage, is used internally by L<Bio::Phylo::Meta> to
wrap objects into a common adaptor class for serialization to
XML.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : my $lit = Bio::Phylo::Meta::XMLLiteral->new;
 Function: Initializes a Bio::Phylo::Meta::XMLLiteral object.
 Returns : A Bio::Phylo::Meta::XMLLiteral object.
 Args    : An object (or array ref of objects) to wrap,
           either a 'RDF::Core::Model' (or subclass),
           an 'XML::XMLWriter' or (subclass) or any
           of the following serialization methods
           used for duck-typing one of the following classes:
 	       Bio::Phylo                        => to_xml, 
 	       XML::DOM, XML::GDOME, XML::LibXML => toString, 
 	       XML::Twig                         => sprint,
	       XML::DOM2                         => xmlify, 
	       XML::DOMBacked                    => as_xml,
	       XML::Handler                      => dump_tree, 
	       XML::Element                      => as_XML
	       XML::API                          => _as_string, 
	       XML::Code                         => code	   
 

=cut       
    
    sub new {
        my ( $class, $obj ) = @_;
        return bless \$obj, $class;
    }
    
=back

=head2 SERIALIZERS

=over

=item to_xml()

Serializes invocant to xml.

 Type    : Serializer
 Title   : to_xml
 Usage   : my $xml = $obj->to_xml;
 Function: Turns the invocant object (and its descendants) into an XML string.
 Returns : SCALAR
 Args    : NONE

=cut
    
    sub to_xml {
        my $self = shift;
        my $objs = $$self;
        my @objs = ref($objs) eq 'ARRAY' ? @{ $objs } : ( $objs );
        my $xml = '';
		for my $obj ( @objs ) {
	        # for RDF::Core::Model objects
	        if ( isa($obj, 'RDF::Core::Model') ) {
	            eval {
	                require RDF::Core::Model::Serializer;
	                my $serialized_model = '';
	                my $serializer = RDF::Core::Model::Serializer->new(
	                    'Model'  => $obj,
	                    'Output' => \$serialized_model,
	                );   
	                $xml .= $serialized_model;     			
	            };
	            if ( $@ ) {
	                throw 'API' => $@;
	            }
	        }	        
	        # for XML::XMLWriter object
	        elsif ( isa($obj, 'XML::XMLWriter') ) {
	            $xml .= $obj->get;
	        }	        
	        else {
	            # duck-typing
	            # Bio::Phylo => to_xml, XML::DOM,XML::GDOME,XML::LibXML => toString, XML::Twig => sprint
	            # XML::DOM2 => xmlify, XML::DOMBacked => as_xml,
	            # XML::Handler => dump_tree, XML::Element => as_XML
	            # XML::API => _as_string, XML::Code => code	            
	            my @methods = qw(to_xml toString sprint _as_string code xmlify as_xml dump_tree as_XML);
	            SERIALIZER: for my $method ( @methods ) {
	                if ( can($obj,$method) ) {
	                    $xml .= $obj->$method;
	                    last SERIALIZER;
	                }
	            }
	        }
		}
        return $xml;        
    }
    
=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Meta>

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Annotation.pm 1040 2009-05-28 04:26:49Z rvos $

=cut  
    
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }    
    sub _cleanup {}    
}
1;