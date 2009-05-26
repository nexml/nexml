#$Id$

# argument passthrus

package Bio::Phylo::Util::DOM::DocumentI;
use strict;
use warnings;
use lib '../lib';
use Bio::Phylo::Util::Exceptions qw(throw);

=head1 NAME

Bio::Phylo::Util::DOM::DocumentI - Abstract interface class for
flexible XML document object model implementation

=head1 SYNOPSIS

Not used directly.

=head1 DESCRIPTION

This module describes an abstract implementation of a DOM document as
expected by Bio::Phylo. The methods here must be overridden in any
concrete implementation. The idea is that different implementations
use a particular XML DOM package, binding the methods here to
analogous package methods.

This set of methods is intentionally minimal. The concrete instances
of this class should inherit both from DocumentI and the underlying XML DOM
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
 Usage   : $doc = Bio::Phylo::Util::Dom::Document->new(@args)
 Function: Create a Document object using the underlying package
 Returns : Document object or undef on fail
 Args    : Package-specific arguments

=cut

sub new {
    my ($class, @args) = @_;
    $class->throw_not_implemented;
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
    my ($self, $encoding, @args) = @_;
    $self->throw_not_implemented;
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
    my ($self, @args) = @_;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
    my $self = shift;
    $self->throw_not_implemented;
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

sub get_element_by_id {
    my ($self, $id, @args) = @_;
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
    $self->throw_not_implemented;
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
