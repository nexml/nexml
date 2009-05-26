#$Id$

# need separate interfaces for Element, Document, ... instead?

package Bio::Phylo::Util::DOM::ElementI;
use strict;
use warnings;
use lib '../lib';
use Bio::Phylo::Util::Exceptions qw(throw);

=head1 NAME

Bio::Phylo::Util::DOM::ElementI - Abstract interface class for
flexible XML document object model implementation

=head1 SYNOPSIS

Not used directly.

=head1 DESCRIPTION

This module describes an abstract implementation of a DOM object as
expected by Bio::Phylo. The methods here must be overridden in any
concrete implementation. The idea is that different implementations
use a particular XML DOM package, binding the methods here to
analogous package methods.

This set of methods is intentionally minimal. The concrete instances
of this class should inherit both from ElementI and the underlying XML DOM
object class, so that package-specific methods can be directly
accessed from the instantiated object.

=head1 AUTHOR

Mark A. Jensen - maj -at- fortinbras -dot- us

=cut

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
    my ($class, $tag, $attrs, @args) = @_;
    $class->throw_not_implemented;
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
    my ($self, @args) = @_;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
    my ($self, @attr_aa) = @_;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
}

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
    $self->throw_not_implemented;
}

=item get_text()

 Type    : Accessor
 Title   : get_text
 Usage   : $elt->get_text()
 Function: Retrieve direct #TEXT descendants as (concatenated) string
 Returns : scalar string (the text content)
 Args    : none

=cut

sub get_text {
    my ($self, @args) = @_;
    $self->throw_not_implemented;
}

=item clear_text()

 Type    : Mutator
 Title   : clear_text
 Usage   : $elt->clear_text()
 Function: Remove direct #TEXT descendant nodes from element
 Returns : True on success; false if no #TEXT nodes removed
 Args    : none

=cut

sub clear_text {
    my ($self, @args) = @_;
    $self->throw_not_implemented;
}

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
    my $self = shift;
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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

=cut

sub set_child {
    my ($self, $child, @args) = @_;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
}

=back

=head2 Internal methods

=over

=item throw_not_implemented()

 Type    : Exception
 Title   : throw_not_implemented
 Usage   : $elt->throw_not_implemented
 Function: Throw exception to indicate a method is not overridden in an
           instance class
 Returns : Bio::Phylo::Util::Exceptions object
 Args    : none

=cut

sub throw_not_implemented {
    my $self = shift;
    throw 'NotImplemented' => "This DOM method is not implemented in the instance class";
}

=item  _rearrange()

 Type    : 
 Title   : _rearrange
 Usage   : my ($arg1, $arg2, ...) = _rearrange( [qw( arg1 arg2 ... )], @input_args
 Function: Assign a named argument list to subroutine-local variables
 Returns : rearranged argument values
 Args    : arrayref to argument names, copy of argument array
 Note    : Ripped from BioPerl RootI.pm

=cut

sub _rearrange {
#    my $dummy = shift;
    my $order = shift;
    
    return @_ unless (substr($_[0]||'',0,1) eq '-');
    push @_,undef unless $#_ %2;
    my %param;
    while( @_ ) {
	(my $key = shift) =~ tr/a-z\055/A-Z/d; #deletes all dashes!
	$param{$key} = shift;
    }
    map { $_ = uc($_) } @$order;
    return @param{@$order};
}

=back

=cut

1;
