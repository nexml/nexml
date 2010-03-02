#$Id$

=head1 NAME

Bio::Phylo::Util::DOM::Document::libxml - XML DOM document mappings to the 
C<XML::LibXML> package

=head1 SYNOPSIS

Don't use directly; use Bio::Phylo::Util::DOM->new( -format => 'libxml' ) instead.

=head1 DESCRIPTION

This module provides mappings the methods specified in the 
L<Bio::Phylo::Util::DOM::DocumentI> interface to the C<XML::LibXML::Document>
package.

=head1 AUTHOR

Mark A. Jensen ( maj -at- fortinbras -dot- us )

=cut

package Bio::Phylo::Util::DOM::Document::libxml;
use strict;
use warnings;
use lib '../lib';
use Bio::Phylo::Util::DOM::DocumentI;
use Bio::Phylo::Util::DOM::Element::libxml; # for blessing 
use Bio::Phylo::Util::Exceptions qw(throw);
use UNIVERSAL qw(isa);
use vars qw(@ISA);

BEGIN {
    eval { 
        require XML::LibXML };
    if ($@) {
		throw 'ExtensionError' => "Failed to load XML::LibXML::Document: $@";
    }
    @ISA = qw( Bio::Phylo::Util::DOM::DocumentI XML::LibXML::Document );
}


=head2 Constructor

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : $doc = Bio::Phylo::Util::Dom::Document->new(@args)
 Function: Create a Document object using the underlying package
 Returns : Document object or undef on fail
 Args    : Package-specific arguments

=cut

sub new {
    my ($class, @args) = @_;
    my $self = XML::LibXML::Document->new(@args);
    bless($self, $class);
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
    unless (isa($root, 'XML::LibXML::Element')) {
	throw 'ObjectMismatch' => "Argument is not an XML::LibXML::Element";
    }
    $self->setDocumentElement($root);
    return 1;
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
    return unless $e;
    $e = $e->shift;
    return bless( $e, 'Bio::Phylo::Util::DOM::Element::libxml');
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
    bless($_, 'Bio::Phylo::Util::DOM::Element::libxml') for (@a);
    return @a;
}

=back

=head2 Output methods

=over

=item to_xml_string()

 Type    : Serializer
 Title   : to_xml_string
 Usage   : $doc->to_xml_string
 Function: Create XML string from document
 Returns : XML string
 Args    : Formatting arguments as allowed by underlying package

=cut

sub to_xml_string {
    my ($self, @args) = @_;
    return $self->toString(@args);
}

=item to_xml_file()

 Type    : Serializer
 Title   : to_xml_file
 Usage   : $doc->to_xml_file()
 Function: Create XML file from document
 Returns : True on success
 Args    : filename, formatting arguments as allowed by underlying package

=cut

sub to_xml_file {
    my ($self, $file, @args) = @_;
    # catch error here?
    $self->toFile($file, @args);
    return 1;
}

=back

=cut    

1;
