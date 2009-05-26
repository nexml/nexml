#$Id$

=head1 NAME

Bio::Phylo::Util::DOM::Document::twig - XML DOM document mappings to the
C<XML::Twig> package

=head1 SYNOPSIS

Don't use directly; use Bio::Phylo::Util::DOM->new( -format => 'twig' ) instead.

=head1 DESCRIPTION

This module provides mappings the methods specified in the 
L<Bio::Phylo::Util::DOM::DocumentI> interface to the 
C<XML::Twig> package.

=head1 AUTHOR

Mark A. Jensen ( maj -at- fortinbras -dot- us )

=cut

package Bio::Phylo::Util::DOM::Document::twig;
use strict;
use warnings;
use lib '../lib';
use Bio::Phylo::Util::DOM::DocumentI;
use Bio::Phylo::Util::Exceptions qw(throw);
use UNIVERSAL qw(isa);
use vars qw(@ISA);

BEGIN {
    eval { require 'XML/Twig.pm' };
    if (@_) {
	throw 'ExtensionError' => "Failed to load XML::Twig: $@";
    }
    @ISA = qw( Bio::Phylo::Util::DOM::DocumentI XML::Twig );
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
    my $self = XML::Twig->new();
    $self->set_encoding();
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
    my ($self, $encoding, @args) = @_;
    $self->set_encoding($encoding);
    return 1;
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
    unless (isa($root, 'XML::Twig::Elt')) {
	throw 'ObjectMisMatch' => 'Argument is not an XML::Twig::Elt';
    }
    XML::Twig::set_root($self, $root);
    # manage ids
    for ($root->descendants_or_self) {
	${$self->{twig_id_list}}{$_->att('id')} = $_ if $_->att('id');
    }
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
    return shift->root;
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
    return shift->elt_id(shift);
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
    return $self->get_root->get_elements_by_tagname($tagname);
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
    return $self->sprint(@args);
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
    my $fh;
    unless ( open $fh, ">$file" ) {
	throw 'FileError' => "Problem with XML output file: $!";
    }
    $self->print($fh, @args);
    return 1;
}

=back

=cut

1;
