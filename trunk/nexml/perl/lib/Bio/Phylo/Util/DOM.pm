# $Id $
package Bio::Phylo::Util::DOM;
use strict;
use Bio::Phylo;
use Bio::Phylo::Util::CONSTANT qw(_DOMCREATOR_);
use Bio::Phylo::Util::Exceptions qw( throw );
use File::Spec::Unix;
use vars qw(@ISA $DOM);

# store DOM factory object as a global here, to avoid proliferation of 
# function arguments

@ISA = qw(Bio::Phylo);

my $CONSTANT_TYPE = _DOMCREATOR_;
my (%format);
my $PERLNS = 'Bio::Phylo::Util::DOM';

=head1 NAME

Bio::Phylo::Util::DOM - Drop-in XML DOM support for C<Bio::Phylo>

=head1 SYNOPSIS

 use Bio::Phylo::Util::DOM;
 use Bio::Phylo::IO qw( parse );
 Bio::Phylo::Util::DOM->new(-format => 'twig');
 my $project = parse( -file=>'my.nex', -format=>'nexus' );
 my $nex_twig = $project->doc();

=head1 DESCRIPTION

This module adds C<to_dom> methods to L<Bio::Phylo::Util::XMLWritable>
classes, which provide NeXML-valid objects for document object model
manipulation. DOM formats currently available are C<XML::Twig> and
C<XML::LibXML>.  For any C<XMLWritable> object, use C<to_dom> in place
of C<to_xml> to create DOM nodes.

The C<doc()> method is also added to the C<Bio::Phylo::Project> class. It returns a NeXML document as a DOM object populated by the current contents of the C<Bio::Phylo::Project> object.

=head1 MOTIVATION

The NeXML parsing/writing capability of C<Bio::Phylo> goes a long way
towards wider adoption of this useful standard.

However, while C<Bio::Phylo> can write NeXML-valid XML, the way in
which it does this natively is somewhat hard-coded and therefore
restricted, and is essentially oriented toward text file output. As
such, there is a mismatch between the sophisticated C<Bio::Phylo> data
structure and its own ability to manipulate and serialize that
structure in sophisticated but interoperable ways. Finer manipulations
of XML-represented data are possible via through a variety of Perl
packages that can store and control XML according to a document
object model (DOM). Many of these packages allow extremely flexible
computation over large datasets stored in XML format, and admit the
use of XML-related facilities such as XPath and XSLT programmatically.

The purpose of C<Bio::Phylo::Util::DOM> is to introduce integrated DOM
object creation and manipulation to C<Bio::Phylo>, both to make DOM
computation in C<Bio::Phylo> more convenient, and also to provide a
platform for potentially more sophisticated C<Bio::Phylo> modules to
come.

=head1 DESIGN

Besides the notion that DOM capability should be optional for the user,
there are two main design ideas. First, for each C<Bio::Phylo> object
that can be parsed/written as NeXML (i.e., for each
C<Bio::Phylo::Util::XMLWritable> object), we provide analogous method
for creating a representative DOM object, or element. These elements
are aggregatable in a DOM document object, whose native stringifying
method can be used to generate valid NeXML. 

Second, we allow flexibility and extensibility in the choice of the
underlying DOM package, while maintaining a consistent DOM interface
that is similar in semantic and syntactic style to the accessors and
mutators that act on the C<Bio::Phylo> objects themselves. This is
achieved through the DOM::DocumentI and DOM::ElementI interfaces,
which define a minimal subset of DOM accessors and mutators, their
inputs and outputs. Concrete instances of these interface classes
provide the bindings between the abstract methods and their
counterparts in the desired DOM implementation. Currently, there are
bindings for two popular packages, C<XML::Twig> and C<XML::LibXML>.

Another priority was simplicity of use; most of the details remain
under the hood in practice. The C<Bio/Phylo/Util/DOM.pm> file defines the
C<to_dom()> method for each C<XMLWritable> package, as well as the
C<Bio::Phylo::Util::DOM> package proper. The C<DOM> object is a
factory that is used to create Element and Document objects; it is an
inside-out object that subclasses C<Bio::Phylo>. To curb the
proliferation of method arguments, a DOM factory instance (set by the
latest invocation of C<Bio::Phylo::Util::DOM-E<gt>new()>) is maintained in
a package global. This is used by default for object creation with DOM
methods if a DOM factory object is not explicitly provided in the
argument list.

The underlying DOM implementation is set with the C<DOM> factory
constructor's single argument, C<-format>. Even this can be left out;
the default implementation is C<XML::Twig>, which is already required
by C<Bio::Phylo>. Thus, for example, one can use the DOM to convert
a Nexus file to a DOM representation as follows:

 use Bio::Phylo::Util::DOM;
 use Bio::Phylo::IO qw( parse );
 Bio::Phylo::Util::DOM->new();
 my $project = parse( -file=>'my.nex', -format=>'nexus' );
 my $nex_twig =  $project->doc();
 # The end.

Underlying DOM packages are loaded at runtime as specified by the
C<-format> argument. Packages for unused formats do not need to be
installed.

=head1 INTERFACE METHODS

The minimal DOM interface specifies the following methods. Details can be obtained from the C<ElementI> and C<DocumentI> POD.

=head2 Bio::Phylo::Util::DOM::ElementI - DOM Element Interface

 get_tagname()
 set_tagname()
 get_attributes()
 set_attributes()
 clear_attributes()
 get_text()
 set_text()
 clear_text()

 get_parent()
 get_children()
 get_first_child()
 get_last_child()
 get_next_sibling()
 get_prev_sibling()
 get_elements_by_tagname()

 set_child()
 prune_child()

 to_xml_string()

=head2 Bio::Phylo::Util::DOM::DocumentI - DOM Document Interface

 get_encoding()
 set_encoding()

 get_root()
 set_root()

 get_element_by_id()
 get_elements_by_tagname()

 to_xml_string()
 to_xml_file()

=head1 METHODS

=head2 CONSTRUCTORS

=over

=item new()

 Type    : Factory constructor
 Title   : new
 Usage   : $dom = Bio::Phylo::Util::DOM->new(-format=>$format)
 Function: Create a new DOM factory
 Returns : DOM object
 Args    : format - DOM format (defaults to 'twig')

=cut

sub new {
    my $class = shift;
    my @args = @_;
    my ($format) = _rearrange([qw(format)],@args);
    my $self = $class->SUPER::new(@args);
    unless ($self->get_format) {
		$self->set_format('twig'); # use XML::Twig bindings as default
    }
    $self->_load_dom_modules();
    $Bio::Phylo::Util::DOM::DOM = $self; 
    return $self;
}

=item create_element()

 Type    : Creator
 Title   : create_element
 Usage   : $elt = Bio::Phylo::Util::DOM->new_document(-format=>$format)
 Function: Create a new XML DOM element
 Returns : DOM document
 Args    : Optional:
           -tag => $tag_name
           -attr => \%attr_hash

=cut

sub create_element { 
    my $self = shift;
    my @args = @_;
    unless ($self->get_format) {
		throw 'BadArgs' => 'DOM creator format not set';
    }
    my $format = $self->get_format;
    return "Bio::Phylo::Util::DOM::Element::$format"->new(@args);
}

=item create_document()

 Type    : Creator
 Title   : create_document
 Usage   : $doc = Bio::Phylo::Util::DOM->new_document(-format=>$format)
 Function: Create a new XML DOM document
 Returns : DOM document
 Args    : Package-specific args

=cut

sub create_document {
    my $self = shift;
    my @args = @_;
    unless ($self->get_format) {
		throw 'BadArgs' => 'DOM creator format not set';
    }
    my $format = $self->get_format;
    return "Bio::Phylo::Util::DOM::Document::$format"->new(@args);
}

=item set_format()

 Type    : Mutator
 Title   : set_format
 Usage   : $dom->set_format($format)
 Function: Set the format (underlying DOM package bindings) for this object
 Returns : format designator as string
 Args    : format designator as string

=cut

sub set_format {
    my $self = shift;
    return $format{$$self} = shift;
}
    
=item get_format()

 Type    : Accessor
 Title   : get_format
 Usage   : $dom->get_format()
 Function: Get the format designator for this object
 Returns : format designator as string
 Args    : none

=cut

sub get_format {
    my $self = shift;
    return $format{$$self};
}
    
=item _load_dom_module()

 Type    : Internal
 Title   : _load_dom_modules
 Usage   : $obj->_load_dom_modules()
 Function: Loads requested DOM format packages
 Returns : True on success
 Args    : format

=cut

sub _load_dom_modules {
    # much ripped from BioPerl (Bio::Root::Root)/maj
    my $self = shift;
    my ($name, $load);
    my $fmt = $self->get_format;
    return 0 unless $fmt;
    foreach (qw( Element Document ) ) {
		$name = $PERLNS."::${_}::$fmt";
		if ($name !~ /^([\w:]+)$/) {
		    throw 'ExtensionError' => "$name is an illegal perl package name";
		} 
		else { 
		    $name = $1;
		}
	
		$load = "$name.pm";
		$load = File::Spec::Unix->catfile((split(/::/,$load)));
		eval {
		    require $load;
		};
		if ( $@ ) {
		    throw 'ExtensionError' => "Failed to load module $name. ".$@;
		}
    }
    return 1;
}

=begin comment

 Type    : Internal method
 Title   : _type
 Usage   : $node->_type;
 Function:
 Returns : CONSTANT
 Args    :

=end comment

=cut

sub _type { $CONSTANT_TYPE }


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
    map { $_ = uc($_) } @$order; # for bug #1343, but is there perf hit here?
    return @param{@$order};
}

=back

=cut

package Bio::Phylo::Util::XMLWritable;
use strict;
use Bio::Phylo::Util::Exceptions qw( throw );
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_);
{

=head1 METHODS

=over

=item Bio::Phylo::Util::XMLWritable::get_dom_elt()

Analog to get_xml_tag.

 Type    : Serializer
 Title   : get_dom_elt
 Usage   : $obj->get_dom_elt
 Function: Generates a DOM element from the invocant
 Returns : an XML::LibXML::Element object
 Args    : DOM factory object

=cut

    sub get_dom_elt {
		my ($self,$dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $elt = $dom->create_element($self->get_tag);
		my %attrs = %{ $self->get_attributes };
		for my $key ( keys %attrs ) {
		    $elt->set_attributes( $key => $attrs{$key} );
		}
	
		my $dictionaries = $self->get_dictionaries;
		if ( @{ $dictionaries } ) {
		    $elt->set_child( $_->to_dom($dom) ) for @{ $dictionaries };
		}
		if ( UNIVERSAL::can($self,'get_sets') ) {
		    my $sets = $self->get_sets;
		    $elt->set_child( $_->to_dom($dom) ) for @{ $sets };
		}
		return $elt;
    }

=item Bio::Phylo::Util::XMLWritable::to_dom()

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $obj->to_dom
 Function: Generates a DOM subtree from the invocant and
           its contained objects
 Returns : an XML::LibXML::Element object
 Args    : DOM factory object
 Note    : This is the generic function. It is redefined in the 
           classes below.
=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $elt = $self->get_dom_elt($dom);
		if ( $self->can('get_entities') ) {
		    for my $ent ( @{ $self->get_entities } ) {
				if ( UNIVERSAL::can($ent,'to_dom') ) { 
				    $elt->set_child( $ent->to_dom($dom) );
				}
		    }
		}
		return $elt;
    }

}

package Bio::Phylo::Forest::Node;
use strict;
use Bio::Phylo::Util::Exceptions qw(throw);
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_ );
{

=item  Bio::Phylo::Forest::Node::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $node->to_dom($dom)
 Function: Generates an array of DOM elements from the invocant's
           descendants
 Returns : an array of Element objects
 Args    : DOM factory object

=cut

    sub to_dom {
		my ($self, $dom) = shift;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my @nodes = ( $self, @{ $self->get_descendants } );
		my @elts;
		# first write out the node elements
		for my $node ( @nodes ) {
		    if ( my $taxon = $node->get_taxon ) {
				$node->set_attributes( 'otu' => $taxon->get_xml_id );
		    }
		    if ( $node->is_root ) {
				$node->set_attributes( 'root' => 'true' );
		    }
		    push @elts, $node->get_dom_elt($dom);		    
		}
		
		# then the rootedge?
		if ( my $length = shift(@nodes)->get_branch_length ) {
		    my $target = $self->get_xml_id;
		    my $id = "edge" . $self->get_id;
		    my $elt = $dom->create_element('rootedge');
		    $elt->set_attributes('target' => $target);
		    $elt->set_attributes('id' => $id);
		    $elt->set_attributes('length' => $length);
		    push @elts, $elt;
		}
		
		# then the subtended edges
		for my $node ( @nodes ) {
		    my $source = $node->get_parent->get_xml_id;
		    my $target = $node->get_xml_id;
		    my $id     = "edge" . $node->get_id;
		    my $length = $node->get_branch_length;
		    my $elt = $dom->create_element('edge');
		    $elt->set_attributes('source' => $source);
		    $elt->set_attributes('target' => $target);
		    $elt->set_attributes('id' => $id);
		    $elt->set_attributes('length' => $length) if ( defined $length );
		    push @elts, $elt;
		}
		return @elts; 
    }
}

package Bio::Phylo::Forest::Tree;
use strict;
use Bio::Phylo::Util::Exceptions qw(throw);
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_ );
{

=item  Bio::Phylo::Forest::Tree::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $tree->to_dom($dom)
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an Element object
 Args    : DOM factory object

=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $xsi_type = 'nex:IntTree';
		for my $node ( @{ $self->get_entities } ) {
		    my $length = $node->get_branch_length;
		    if ( defined $length and $length !~ /^[+-]?\d+$/ ) {
				$xsi_type = 'nex:FloatTree';
		    }
		}
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $elt = $self->get_dom_elt($dom);
		if ( my $root = $self->get_root ) {
		    $elt->set_child( $_ ) for $root->to_dom($dom);
		}
		return $elt;
    }
}

package Bio::Phylo::Matrices::Datatype;
use strict;
use UNIVERSAL qw(isa);
use Bio::Phylo::Util::Exceptions qw(throw);
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_ );

{

=item  Bio::Phylo::Matrices::Datatype::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $type->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub to_dom {
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( isa($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::Util::DOM::DOM;
		    unless ($dom) {
			throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
		my $elt;
		my $normalized   = $args[0] || {};
		my $polymorphism = $args[1];
		if ( my $lookup  = $self->get_lookup ) {
		    $elt = $self->get_dom_elt($dom);
		    my $id_for_state = $self->get_ids_for_states;
		    my @states = sort  { $id_for_state->{$a} <=> $id_for_state->{$b} } 
		    keys %{ $id_for_state };
		    my $max_id = 0;
		    for my $state ( @states ) {
				my $state_id = $id_for_state->{ $state };
				$id_for_state->{ $state } = 's' . $state_id;
				$max_id = $state_id;
		    }
		    for my $state ( @states ) {
				$elt->set_child( 
				    $self->_state_to_dom(
						$dom,
						$state, 
						$id_for_state, 
						$lookup, 
						$normalized, 
						$polymorphism 
		 		    )
			    );
		    }
		    my ( $missing, $gap ) = ( $self->get_missing, $self->get_gap );
		    my $special = $self->get_ids_for_special_symbols;
		    if ( %{ $special } ) {
				my $uss;
				$uss = $dom->create_element('uncertain_state_set');
				$uss->set_attributes( 'id' => 's'.$special->{$gap} );
				$uss->set_attributes( 'symbol' => '-' );
				$elt->set_child($uss);
				$uss = $dom->create_element('uncertain_state_set');
				$uss->set_attributes( 'id' => 's'.$special->{$missing} );
				$uss->set_attributes( 'symbol' => '?' );
				my $mbr;
				for (@states) {
				    $mbr = $dom->create_element('member');
				    $mbr->set_attributes( 'state' => $id_for_state->{$_} );
				    $uss->set_child($mbr);
				}
				$mbr = $dom->create_element('member');
				$mbr->set_attributes( 'state' => 's'.$special->{$gap} );
				$uss->set_child($mbr);
				$elt->set_child($uss);
		    }		
		    
		}	
		return $elt;
    }

    sub _state_to_dom {
		my ( $self, $dom, $state, $id_for_state, $lookup, $normalized, $polymorphism ) = @_;
	    my $state_id = $id_for_state->{ $state };
	    my @mapping = @{ $lookup->{$state} };
	    my $symbol = exists $normalized->{$state} ? $normalized->{$state} : $state;
		my $elt;
		
	        # has ambiguity mappings
	        if ( scalar @mapping > 1 ) {
	            my $tag = $polymorphism ? 'polymorphic_state_set' : 'uncertain_state_set';
	
			    $elt = $dom->create_element($tag);
			    $elt->set_attributes( 'id' => $state_id );
			    $elt->set_attributes( 'symbol' => $symbol );
	            for my $map ( @mapping ) {
					my $mbr = $dom->create_element('member');
					$mbr->set_attributes('state' => $id_for_state->{ $map } );
					$elt->set_child($mbr);
	            }
		    
	        }
	        
	        # no ambiguity
	        else {
		    $elt = $dom->create_element('state');
		    $elt->set_attributes( 'id' => $state_id );
		    $elt->set_attributes( 'symbol' => $symbol ); 
	        }
		return $elt;
    }
}

package Bio::Phylo::Matrices::Datum;
use strict;
use UNIVERSAL qw(isa);
use Bio::Phylo::Util::Exceptions qw( throw );
use Bio::Phylo::Util::CONSTANT qw(:objecttypes looks_like_number looks_like_hash looks_like_object _DOMCREATOR_);

{

=item  Bio::Phylo::Matrices::Datum::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $datum->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub to_dom {
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( isa($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::Util::DOM::DOM;
		    unless ($dom) {
				throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
	
		##### make sure argument handling works here....
	
		my %args = looks_like_hash @args;
	
		my $char_ids  = $args{'-chars'};
		my $state_ids = $args{'-states'};
		my $special   = $args{'-special'};
		if ( my $taxon = $self->get_taxon ) {
		    $self->set_attributes( 'otu' => $taxon->get_xml_id );
		}
		my @char = $self->get_char;
		my ( $missing, $gap ) = ( $self->get_missing, $self->get_gap );
	
		my $elt = $self->get_dom_elt($dom);
	
		if ( not $args{'-compact'} ) {
		    for my $i ( 0 .. $#char ) {
				if ( $missing ne $char[$i] and $gap ne $char[$i] ) {
				    my ( $c, $s );
				    if ( $char_ids and $char_ids->[$i] ) {
						$c = $char_ids->[$i];
				    }
				    else {
						$c = $i;
				    }
				    if ( $state_ids and $state_ids->{uc $char[$i]} ) {
						$s = $state_ids->{uc $char[$i]};
				    }
				    else {
						$s = uc $char[$i];
				    }
				    my $cell_elt = $dom->create_element('cell'); 
				    $cell_elt->set_attributes( 'char' => $c );
				    $cell_elt->set_attributes('state' => $s ); 
				    $elt->set_child($cell_elt);
				}
				elsif ( $missing eq $char[$i] or $gap eq $char[$i] ) {
				    my ( $c, $s );
				    if ( $char_ids and $char_ids->[$i] ) {
						$c = $char_ids->[$i];
				    }
				    else {
						$c = $i;
				    }
				    if ( $special and $special->{$char[$i]} ) {
						$s = $special->{$char[$i]};
				    }
				    else {
						$s = $char[$i];
				    }
				    my $cell_elt = $dom->create_element('cell');
				    $cell_elt->set_attributes('char' => $c);
				    $cell_elt->set_attributes( 'state' => $s );
				    $elt->set_child($cell_elt);
				    
				}
		    }
		}
		else {
		    my @tmp = map { uc $_ } @char;
		    my $seq = $self->get_type_object->join(\@tmp);
		    my $seq_elt = $dom->create_element('seq');
	#### create a text node here....
		    $seq_elt->set_child( XML::LibXML::Text->new($seq) );
	####
		    $elt->set_child($seq_elt);
		}
		return $elt;
    }

}

package Bio::Phylo::Matrices::Matrix;
use strict;
use UNIVERSAL qw(isa);
use Bio::Phylo::Util::Exceptions qw( throw );
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_ );
{

=item  Bio::Phylo::Matrices::Matrix::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $matrix->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an Element object
 Args    : Optional:
           -compact => 1 : renders characters as sequences,
                           not individual cells

=cut

    sub to_dom {	
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( isa($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::Util::DOM::DOM;
		    unless ($dom) {
				throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
		#### make sure argument handling works here...
		my ( %args, $ids_for_states );
		%args = @args if @args;
	
		my $type = $self->get_type;
		my $verbosity = $args{'-compact'} ? 'Seqs' : 'Cells';
		my $xsi_type = 'nex:' . ucfirst($type) . $verbosity;
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $elt = $self->get_dom_elt($dom);
		my $normalized = $self->_normalize_symbols;
		
		# the format block
	 	my $format_elt = $dom->create_element('format');
		my $to = $self->get_type_object;
		$ids_for_states = $to->get_ids_for_states(1);
		
		# write state definitions
	
		$format_elt->set_child( $to->to_dom( $dom, $normalized, $self->get_polymorphism ) );
		
		# write column definitions
		$format_elt->set_child($_) for $self->_package_char_labels( $dom, %{ $ids_for_states } ? $to->get_xml_id : undef );
	
		$elt->set_child($format_elt);
	
		# the matrix block
	
		my $mx_elt = $dom->create_element('matrix');
		my @char_ids;
		for ( 0 .. $self->get_nchar ) {
		    push @char_ids, 'c' . ($_+1);
		}
		
		# write rows
		my $special = $self->get_type_object->get_ids_for_special_symbols(1);
		for my $row ( @{ $self->get_entities } ) {
		    # $row->to_dom is calling ...::Datum::to_dom...
		    $mx_elt->set_child( 
				$row->to_dom( $dom,
				    '-states'  => $ids_for_states,
				    '-chars'   => \@char_ids,
				    '-symbols' => $normalized,
				    '-special' => $special,
				    %args,
				)
			);
		}
		$elt->set_child($mx_elt);
		return $elt;
    }

    # returns an array of elements
    sub _package_char_labels {
		my ( $self, $dom, $states_id ) = @_;
		my @elts;
		my $labels = $self->get_charlabels;
		for my $i ( 1 .. $self->get_nchar ) {
		    my $char_id = 'c' . $i;
		    my $label   = $labels->[ $i - 1 ];
		    my $elt = $dom->create_element('char');
		    $elt->set_attributes( 'id' => $char_id );
		    $elt->set_attributes( 'label' => $label ) if $label;
		    $elt->set_attributes( 'states' => $states_id ) if $states_id;
		    push @elts, $elt;
		}	
		return @elts;
    }
}

package Bio::Phylo::Project;
use strict;
use UNIVERSAL qw(isa);
use Bio::Phylo::Util::Exceptions qw( throw );
use Bio::Phylo::Util::CONSTANT qw( looks_like_object _DOMCREATOR_ );
{

=item  Bio::Phylo::Project::doc()

 Type    : Serializer
 Title   : doc
 Usage   : $proj->doc()
 Function: Creates a DOM Document object, containing the 
           present state of the project by default
 Returns : a Document object
 Args    : a DOM factory object
           Optional: pass 1 to obtain a document node without 
           content

=cut

    sub doc {
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( isa($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::Util::DOM::DOM;
		    unless ($dom) {
				throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
	###	# make sure argument handling works here...
		my $empty = shift @args;
		my $doc = $dom->create_document();
		my $root;
	
		unless ($empty) {
		    $root = $self->to_dom($dom);
		    $doc->set_root($root);
		}
		return $doc;
    }

=item  Bio::Phylo::Project::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $node->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : a DOM factory object

=back

=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $elt = $self->get_dom_elt($dom);
	
		my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
		my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
		for ( values %taxa, @linked ) {
		    $elt->set_child( $_->to_dom($dom, @_) );
		}
		return $elt;
    }
}

### TODO: Annotations are harder (i.e., I have to learn stuff)

package Bio::Phylo::Annotation;
use strict;
1;

=head1 SEE ALSO

The DOM creator interfaces: L<Bio::Phylo::Util::DOM::ElementI>, L<Bio::Phylo::Util::DOM::DocumentI>

=head1 AUTHOR

Mark A. Jensen  (maj -at- fortinbras -dot- us)

=head1 TODO

The C<Bio::Phylo::Annotation> class is not yet DOMized.

=cut
