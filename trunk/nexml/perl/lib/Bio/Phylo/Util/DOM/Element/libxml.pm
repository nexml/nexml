#$Id$

=head1 NAME

Bio::Phylo::Util::DOM::Element::libxml - XML DOM element mappings to the 
C<XML::LibXML> package

=head1 SYNOPSIS

Don't use directly; use Bio::Phylo::Util::DOM->new( -format => 'libxml' ) instead.

=head1 DESCRIPTION

This module provides mappings the methods specified in the 
L<Bio::Phylo::Util::DOM::ElementI> interface to the 
C<XML::LibXML::Element> package.

=head1 AUTHOR

Mark A. Jensen ( maj -at- fortinbras -dot- us )

=cut

package Bio::Phylo::Util::DOM::Element::libxml;
use strict;
use warnings;
use lib '../lib';
use Bio::Phylo::Util::DOM::ElementI;
use Bio::Phylo::Util::Exceptions qw(throw);
use UNIVERSAL qw(isa);
use vars qw(@ISA);

BEGIN {
    eval { require 'XML/LibXML.pm';
           XML::LibXML->import(':libxml') };
    if (@_) {
	throw 'ExtensionError' => "Failed to load XML::LibXML: $@";
    }
    @ISA = qw( Bio::Phylo::Util::DOM::ElementI XML::LibXML::Element );
}


=head2 Constructor

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : $elt = Bio::Phylo::Util::DOM::Element->new($tag, $attr)
 Function: Create a new XML DOM element
 Returns : DOM element object
 Args    : Optional: 
           $tag  - tag name as string
           $attr - hashref of attributes/values

=cut


sub new {
    my ($class, $tag, @attr_aa) = @_;
    unless ($tag) {
	throw 'BadArgs' => "Tag name required for XML::LibXML::Element";
    }
    my $self = XML::LibXML::Element->new($tag);
    bless($self, $class);
    $self->set_attributes(@attr_aa);
    return $self;
}

=back

=head2 Tagname mutators/accessors

=over

=item get_tagname()

 Type    : Accessor
 Title   : get_tagname
 Usage   : $elt->get_tagname()
 Function: Get tag name
 Returns : Tag name as scalar string
 Args    : none

=cut

sub get_tagname {
    return shift->tagName;
}

=item set_tagname()

 Type    : Mutator
 Title   : set_tagname
 Usage   : $elt->set_tagname( $tagname )
 Function: Set tagname
 Returns : True on success
 Args    : Tag name as scalar string

=cut

sub set_tagname {
    my ($self, $tagname, @args) = @_;
    $self->setNodeName($tagname);
    return 1;
}

=back 

=head2 Attribute mutators/accessors

=over

=item get_attributes()

 Type    : Accessor
 Title   : get_attributes
 Usage   : $elt->get_attributes( @attribute_names )
 Function: Get attribute values
 Returns : Array of attribute values
 Args    : [an array of] attribute name[s] as string[s]

=cut

sub get_attributes {
    my ($self, @attr_names) = @_;
    my @ret;
    push @ret, $self->getAttribute($_) for @attr_names;
    return @ret > 1 ? @ret : $ret[0];
}

=item set_attributes()

 Type    : Mutator
 Title   : set_attributes
 Usage   : $elt->set_attributes( @attribute_assoc_array )
 Function: Set attribute values
 Returns : True on success
 Args    : An associative array of form ( $name => $value, ... )

=cut

sub set_attributes {
    my ($self, $attrs, @attrs) = @_;
    return 0 if !$attrs;
    if (ref($attrs) eq 'HASH') {
	@attrs = map { $_, $attrs->{$_} } keys %$attrs;
    }
    else {
	@attrs = ($attrs, @attrs);
    }
    if (@attrs % 2) {
	throw 'OddHash' => 'Attribute list not of form (key => value, ...)';
    }
    my %attrs = @attrs;
    $self->setAttribute($_, $attrs{$_}) for keys %attrs;
    return 1;
}

=item clear_attributes()

 Type    : Mutator
 Title   : clear_attributes
 Usage   : $elt->clear_attributes( @attribute_names )
 Function: Remove attributes from element
 Returns : Hash of removed attributes/values
 Args    : Array of attribute names

=cut

sub clear_attributes {
    my ($self, @attr_names) = @_;
    return 0 unless @attr_names;
    my %ret;
    $ret{$_} = $self->getAttribute($_) for @attr_names;
    $self->removeAttribute( $_ ) for @attr_names;
    return %ret;
}

=back

=head2 Namespace accessors/mutators

=over

=item

 Type    : 
 Title   :
 Usage   :
 Function:
 Returns :
 Args    :

=cut

=back

=head2 Content mutators/accessors

=over

=item set_text()

 Type    : Mutator
 Title   : set_text
 Usage   : $elt->set_text($text_content)
 Function: Add a #TEXT node to the element 
 Returns : True on success
 Args    : scalar string

=cut

sub set_text {
    my ($self, $text, @args) = @_;
    unless ($text) {
	throw 'BadArgs' => "No text specified";
    }
    $self->appendTextNode( $text );
    return 1;
}

=item get_text()

 Type    : Accessor
 Title   : get_text
 Usage   : $elt->get_text()
 Function: Retrieve direct #TEXT descendants as (concatenated) string
 Returns : scalar string (the text content) or undef if no text nodes
 Args    : none

=cut
no strict;
sub get_text {
    my ($self, @args) = @_;
    my $text;
    for ($self->childNodes) {
	$text .= $_->nodeValue if $_->nodeType == XML_TEXT_NODE;
    }
    return $text || undef;
}
use strict;
=item clear_text()

 Type    : Mutator
 Title   : clear_text
 Usage   : $elt->clear_text()
 Function: Remove direct #TEXT descendant nodes from element
 Returns : True on success; false if no #TEXT nodes removed
 Args    : none

=cut
no strict;
sub clear_text {
    my ($self, @args) = @_;
    my @res = map { 
	$_->nodeType == XML_TEXT_NODE ? $self->removeChild($_) : ()
    } $self->childNodes;
    return 1 if @res;
    return 0;
}
use strict;
=back

=head2 Traversal methods

=over

=item get_parent()

 Type    : Accessor
 Title   : get_parent
 Usage   : $elt->get_parent()
 Function: Get parent DOM node of invocant 
 Returns : Element object or undef if invocant is root
 Args    : none

=cut

sub get_parent {
    my $e = shift->parentNode;
    return bless( $e, __PACKAGE__ );
}

=item get_children()

 Type    : Accessor
 Title   : get_children
 Usage   : $elt->get_children()
 Function: Get child nodes of invocant
 Returns : Array of Elements
 Args    : none

=cut

sub get_children {
    my @ret = shift->childNodes;
    bless( $_, __PACKAGE__ ) for (@ret);
    return @ret;
}

=item get_first_child()

 Type    : Accessor
 Title   : get_first_child
 Usage   : $elt->get_first_child()
 Function: Get first child (as defined by underlying package) of invocant
 Returns : Element object or undef if invocant is childless
 Args    : none

=cut

sub get_first_child {
    my $e = shift->firstChild;
    return bless($e, __PACKAGE__);
}

=item get_last_child()

 Type    : Accessor
 Title   : get_last_child
 Usage   : $elt->get_last_child()
 Function: Get last child (as defined by underlying package) of invocant
 Returns : Element object or undef if invocant is childless
 Args    : none

=cut

sub get_last_child {
    my $e = shift->lastChild;
    return bless($e, __PACKAGE__);
}

=item get_next_sibling()

 Type    : Accessor
 Title   : get_next_sibling
 Usage   : $elt->get_next_sibling()
 Function: Gets next sibling (as defined by underlying package) of invocant
 Returns : Element object or undef if invocant is the rightmost element
 Args    : none

=cut

sub get_next_sibling {
    my $e = shift->nextSibling;
    return bless($e, __PACKAGE__);
}

=item get_prev_sibling()

 Type    : Accessor
 Title   : get_prev_sibling
 Usage   : $elt->get_prev_sibling()
 Function: Get previous sibling (as defined by underlying package) of invocant
 Returns : Element object or undef if invocant is leftmost element
 Args    : none

=cut

sub get_prev_sibling {
    my $e = shift->previousSibling;
    return bless($e, __PACKAGE__);
}

=item get_elements_by_tagname()

 Type    : Accessor
 Title   : get_elements_by_tagname
 Usage   : $elt->get_elements_by_tagname($tagname)
 Function: Get array of elements having given tag name from invocant's 
           descendants
 Returns : Array of elements or undef if no match
 Args    : tag name as string

=cut

sub get_elements_by_tagname {
    my ($self, $tagname, @args) = @_;
    my @a = $self->getElementsByTagName($tagname);
    bless($_, __PACKAGE__) for (@a);
    return @a;
}

=back

=head2 Prune and graft methods

=over

=item set_child()

 Type    : Mutator
 Title   : set_child
 Usage   : $elt->set_child($child)
 Function: Add child element object to invocant's descendants
 Returns : the element object added
 Args    : Element object
 Note    : See caution at 
           L<http://search.cpan.org/~pajas/XML-LibXML-1.69/lib/XML/LibXML/Node.pod#addChild>

=cut

sub set_child {
    my ($self, $child, @args) = @_;
    unless (isa($child, 'XML::LibXML::Node')) {
	throw 'ObjectMismatch' => "Argument is not an XML::LibXML::Node";
    }
    return $self->addChild($child);
}

=item prune_child()

 Type    : Mutator
 Title   : prune_child
 Usage   : $elt->prune_child($child)
 Function: Remove the subtree rooted by $child from among the invocant's
           descendants
 Returns : $child or undef if $child is not among the children of invocant
 Args    : Element object

=cut

sub prune_child {
    my ($self, $child, @args) = @_;
    unless (isa($child, 'XML::LibXML::Node')) {
	throw 'ObjectMismatch' => "Argument is not an XML::LibXML::Node";
    }
    return $self->removeChild($child);
}

=back

=head2 Output methods

=over

=item to_xml_string()

 Type    : Serializer
 Title   : to_xml_string
 Usage   : $elt->to_xml_string
 Function: Create XML string from subtree rooted by invocant
 Returns : XML string
 Args    : Formatting arguments as allowed by underlying package

=cut

sub to_xml_string {
    my ($self, @args) = @_;
    return $self->toString(@args);
}

=back

=cut

1;
