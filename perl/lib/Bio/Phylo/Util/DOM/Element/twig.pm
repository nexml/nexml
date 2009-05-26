#$Id$

=head1 NAME

Bio::Phylo::Util::DOM::Element::twig - XML DOM mappings to the 
C<XML::Twig> package

=head1 SYNOPSIS

Don't use directly; use Bio::Phylo::Util::DOM->new( -format => 'twig' ) instead.

=head1 DESCRIPTION

This module provides mappings the methods specified in the 
L<Bio::Phylo::Util::DOM::ElementI> interface.

=head1 AUTHOR

Mark A. Jensen ( maj -at- fortinbras -dot- us )

=cut

# note: we do our own updates of the Twig id list (the property
# $twig->{twig_id_list}, since according to the XML::Twig source
# "WARNING: at the moment the id list is not updated reliably" which
# evidently means that it isn't updated at all, unless the special
# add_id method is used. Since we want to create elements independent
# of the twig, I felt more in control doing it by by hand. The kludge
# allows the use of the Twig method elt_id() to "get_element_by_id"
# off a document object.


package Bio::Phylo::Util::DOM::Element::twig;
use strict;
use warnings;
use lib '../lib';
use UNIVERSAL qw(isa);
use Bio::Phylo::Util::DOM::ElementI;
use Bio::Phylo::Util::Exceptions qw(throw);
use vars qw(@ISA %extant_ids);

BEGIN {
    eval { require "XML/Twig.pm"; };
    if ($@) {
	throw 'ExtensionError' => "Failed to load XML::Twig: $@";
    }
    @ISA = qw( Bio::Phylo::Util::DOM::ElementI XML::Twig::Elt );
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
    my ($class, $tag, $attrs, @args) = @_;
    my $self = XML::Twig::Elt->new($tag);
    bless($self, $class);
    return $self unless $attrs;
    my @attrs = ($attrs, @args);
    $self->set_attributes(@attrs);
    $self->_manage_ids('ADD', @attrs);
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
    return shift->gi;
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
    $self->set_gi($tagname);
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
    my @ret = map { $self->att($_) } @attr_names;
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
    $self->set_att( @attrs );
    $self->_manage_ids('ADD',@attrs);
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
    my %ret;
    $ret{$_} = $self->att($_) for @attr_names;
    $self->_manage_ids('DEL', @attr_names); # must come before actual removal
    $self->del_att( @attr_names );
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
    my $t = XML::Twig::Elt->new('#PCDATA', $text);
    $t->paste( last_child => $self );
    return 1;
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
    return $self->text;
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
    my @res;
    @res = map { $_->is_text ? do { $_->delete; 1 }  : () } $self->children;
    return 1 if @res;
    return 0;
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
    return shift->parent();
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
    return shift->children();
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
    return shift->first_child();
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
    return shift->last_child();
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
    return shift->next_sibling();
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
    return shift->prev_sibling();
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
    return $self->descendants_or_self( $tagname );
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
    unless ( isa($child, 'XML::Twig::Elt') ) {
	throw 'ObjectMismatch' => 'Argument is not an XML::Twig::Elt';
    }
    $child->paste( last_child => $self );
    $self->_manage_ids('ADD');
    return $child;
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
    unless ( isa($child, 'XML::Twig::Elt') ) {
	throw 'ObjectMismatch' => 'Argument is not an XML::Twig::Elt';
    }
    my $par = $child->parent;
    return undef unless ( $par && ($par == $self) );
    # or delete?
    $child->_manage_ids('DEL');
    $child->cut;
    return $child;
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
    return shift->sprint(@_);
}

=back

=cut    

sub _manage_ids {
    my ($self, $action, @attrs) = @_;
    for ($action) {
	$_ eq 'ADD' && do {
	    my %attrs = @attrs;
	    if (%attrs) { # changing/adding id attribute
		my $id = $attrs{id};
		if ($id) {
		    $extant_ids{$id} = $self; # log this id 
		    ${$self->twig->{twig_id_list}}{$id} = $self if $self->twig;
		}
		else {
		    return 0;
		}
	    }
	    else { # add this element and its descendants
		# if all elements were created with new(), they all should
		# logged in %extant_ids
		if ($self->twig) {
		    for ($self->descendants_or_self) {
			${$self->twig->{twig_id_list}}{$_->att('id')} = $_ if $_->att('id');
		    }
		}
	    }
	    last;
	};
	$_ eq 'DEL' && do {
	    if (@attrs) {
		if (grep /^id$/, @attrs) {
		    my $id = $self->att('id');
		    delete $extant_ids{$id}; # clear this id 
		    delete ${$self->twig->{twig_id_list}}{$id} if $self->twig;
		}
		else {
		    return 0;
		}
	    }
	    else {
		if ($self->twig) {
		    delete $extant_ids{$_->att('id')} for $self->descendants_or_self;
		    delete ${$self->twig->{twig_id_list}}{$_->att('id')} for $self->descendants_or_self;
		}
	    }
	    last;
	};
	do { 
	    throw 'BadArgs' => 'Unknown action for _manage_ids()';
	};
    }
    return 1;
}


1;
