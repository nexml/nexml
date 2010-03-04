# $Id: $

=head1 NAME

Bio::Phylo::NeXML::DOM::Document::Libxml - XML DOM document mappings to the 
C<XML::LibXML> package

=head1 SYNOPSIS

Don't use directly; use Bio::Phylo::NeXML::DOM->new( -format => 'libxml' ) instead.

=head1 DESCRIPTION

This module provides mappings the methods specified in the 
L<Bio::Phylo::NeXML::DOM::Document> abstract class to the C<XML::LibXML::Document>
package.

=head1 AUTHOR

Mark A. Jensen ( maj -at- fortinbras -dot- us )

=cut

package Bio::Phylo::NeXML::DOM::Document::Libxml;
use strict;
use Bio::Phylo::NeXML::DOM::Document ();
use Bio::Phylo::Util::CONSTANT qw(looks_like_instance);
use Bio::Phylo::NeXML::DOM::Element::Libxml (); # for blessing 
use Bio::Phylo::Util::Exceptions qw(throw);
use vars qw(@ISA);

BEGIN {
    # XML::LibXML::Document is a package within the same file as XML::LibXML,
    # no need to require-test it separately
    eval { require XML::LibXML };
    if ($@) {		
	throw 'ExtensionError' => "Failed to load XML::LibXML::Document: $@";
    }
    @ISA = qw( Bio::Phylo::NeXML::DOM::Document XML::LibXML::Document );
}


=head2 Constructor

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : $doc = Bio::Phylo::NeXML::DOM::Document->new(@args)
 Function: Create a Document object using the underlying package
 Returns : Document object or undef on fail
 Args    : Package-specific arguments

=cut

sub new {
    my ($class, @args) = @_;
    my $self = XML::LibXML::Document->new(@args);
    bless $self, $class;
    return $self;
}

=back

=cut 

=head2 Document property accessors/mutators

=over

=item set_encoding()

 Type    : Mutator
 Title   : set_encoding
 Usage   : $doc->set_encoding($enc)
 Function: Set encoding for document
 Returns : True on success
 Args    : Encoding descriptor as string

=cut

sub set_encoding {
    return shift->setEncoding(shift);
}

=item get_encoding()

 Type    : Accessor
 Title   : get_encoding
 Usage   : $doc->get_encoding()
 Function: Get encoding for document
 Returns : Encoding descriptor as string
 Args    : none

=cut

sub get_encoding {
    return shift->encoding;
}

=item set_root()

 Type    : Mutator
 Title   : set_root
 Usage   : $doc->set_root($elt)
 Function: Set the document's root element
 Returns : True on success
 Args    : Element object

=cut

sub set_root {
    my ($self, $root) = @_;
    if ( looks_like_instance $root, 'XML::LibXML::Element' ) {
	$self->setDocumentElement($root);
	return 1;
    }
    else {
	throw 'ObjectMismatch' => "Argument is not an XML::LibXML::Element";
    }
}

=item get_root()

 Type    : Accessor
 Title   : get_root
 Usage   : $doc->get_root()
 Function: Get the document's root element
 Returns : Element object or undef if DNE
 Args    : none

=cut

sub get_root {
    return shift->documentElement;
}

=back

=cut 

=head2 Document element accessors

=over 

=item get_element_by_id()

 Type    : Accessor
 Title   : get_element_by_id
 Usage   : $doc->get_element_by_id($id)
 Function: Get element having id $id
 Returns : Element object or undef if DNE
 Args    : id designator as string

=cut

# the XML::LibXML::Document::get_element_by_id() retrieves only 
# via @xml:id attributes in a general XML file. This is a kludge
# using an XPath expression to find an unqualified id attribute
# that matches.

sub get_element_by_id {
    my ($self, $id) = @_;
    unless ($id) {
	throw 'BadArgs' => "Argument 'id' required";
    }
    my $xp = "//*[\@id = '$id']";
    my $e = $self->get_root->find( $xp );
    return unless $e; # don't return undef explicitly, do it this way
    $e = $e->shift;
    return bless $e, 'Bio::Phylo::NeXML::DOM::Element::Libxml';
}

=item get_elements_by_tagname()

 Type    : Accessor
 Title   : get_elements_by_tagname
 Usage   : $elt->get_elements_by_tagname($tagname)
 Function: Get array of elements having given tag name 
 Returns : Array of elements or undef if no match
 Args    : tag name as string

=cut

sub get_elements_by_tagname {
    my ($self, $tagname, @args) = @_;
    my @a = $self->getElementsByTagName($tagname);
    bless($_, 'Bio::Phylo::NeXML::DOM::Element::Libxml') for (@a);
    return @a;
}

=back

=head2 Output methods

=over

=item to_xml()

 Type    : Serializer
 Title   : to_xml
 Usage   : $doc->to_xml
 Function: Create XML string from document
 Returns : XML string
 Args    : Formatting arguments as allowed by underlying package

=cut

sub to_xml {
    my ($self, @args) = @_;
    return $self->toString(@args);
}

=back

=cut    

1;
