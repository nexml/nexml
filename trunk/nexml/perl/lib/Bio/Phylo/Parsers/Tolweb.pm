package Bio::Phylo::Parsers::Tolweb;
use strict;
use warnings;
use Bio::Phylo::IO;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Factory;
use UNIVERSAL 'isa';
use vars qw(@ISA $VERSION);
@ISA = qw(Bio::Phylo::IO);

eval { require XML::Twig };
if ( $@ ) {
	throw 'ExtensionError' => "Error loading the XML::Twig extension: $@";
}

=head1 NAME

Bio::Phylo::Parsers::Tolweb - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module parses Tree of Life data. It is called by the L<Bio::Phylo::IO> facade,
don't call it directly. In addition to parsing from files, handles or strings (which
are specified by the -file, -handle and -string arguments) this parser can also parse
xml directly from a url (-url => $tolweb_output), provided you have L<LWP> installed.

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The ToL web parser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to parse Tree of Life data (or any other data Bio::Phylo supports).

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=item L<http://tolweb.org>

For more information about the Tree of Life xml format, visit 
L<http://tolweb.org/tree/home.pages/downloadtree.html>

=back

=head1 REVISION

 $Id$

=cut

# The factory object, to instantiate Bio::Phylo objects
my $factory = Bio::Phylo::Factory->new;

# We re-use the core Bio::Phylo version number.
$VERSION = $Bio::Phylo::VERSION;

# I factored the logging methods in Bio::Phylo (debug, info,
# warning, error, fatal) out of the inheritance tree and put
# them in a separate logging object.
my $logger = Bio::Phylo::Util::Logger->new;

# this is the constructor that gets called by Bio::Phylo::IO,
# here we create the object instance that will process the file/string
sub _new {
	my $class = shift;
	$logger->debug("instantiating $class");

	# this is the actual parser object, which needs to hold a reference
	# to the XML::Twig object and to the tree
	my $self = bless { 
		'_tree'      => undef,
		'_node_of'   => {},
		'_parent_of' => {}, 
	}, $class;

	# here we put the two together, i.e. create the actual XML::Twig object
	# with its handlers, and create a reference to it in the parser object
	$self->{'_twig'} = XML::Twig->new( 
		'TwigHandlers' => {
			'NODE' => sub { &_handle_node( $self, @_ ) },			
		}		
	);
	return $self;
}

# the official interface for Bio::Phylo::IO parser subclasses requires a
# _from_handle method (to process data on a file handle) and a _from_string
# method, for data in a string variable. Since XML::Twig can parse both
# from handle and string with the same XML::Twig->parse method call, we can
# suffice with aliases that point to the same method _from_both
*_from_handle = \&_from_both;
*_from_string = \&_from_both;
*_from_url    = \&_from_both;

# this method will be called by Bio::Phylo::IO, indirectly, through
# _from_handle if the parse function is called with the -file => $filename
# argument, or through _from_string if called with the -string => $string
# argument
sub _from_both {
	my $self = shift;
	$logger->debug("going to parse xml");
	my %opt = @_;

	$self->{'_tree'} = $factory->create_tree;
	
	# XML::Twig doesn't care if we parse from a handle or a string
	my $xml = $opt{'-handle'} || $opt{'-string'};
	if ( $xml ) {
		$self->{'_twig'}->parse($xml);
	}
	elsif ( $opt{'-url'} ) {
		$self->{'_twig'}->parseurl($opt{'-url'});
	}
	$logger->debug("done parsing xml");
	
	for my $node_id ( keys %{ $self->{'_node_of'} } ) {
		if ( defined( my $parent_id = $self->{'_parent_of'}->{$node_id} ) ) {
			my $child = $self->{'_node_of'}->{$node_id};
			my $parent = $self->{'_node_of'}->{$parent_id};
			$child->set_parent($parent);
		}
	}

	# we're done, now grab the tree from its field
	my $tree = $self->{'_tree'};

	# reset everything in its initial state: Bio::Phylo::IO caches parsers
	$self->{'_tree'}      = undef;
	$self->{'_node_of'}   = {};
	$self->{'_parent_of'} = {}; 	

	if ( $opt{'-project'} ) {
		my $forest = $factory->create_forest;
		$forest->insert($tree);
		my $taxa = $forest->make_taxa;
		$opt{'-project'}->insert($taxa,$forest);
		return $opt{'-project'};
	}
	elsif ( $opt{'-as_project'} ) {
		my $forest = $factory->create_forest;
		my $taxa = $forest->make_taxa;
		my $proj = $factory->create_project;
		$proj->insert($taxa,$forest);
		return $proj;
	}
	else {
		return $tree;
	}
}

sub _handle_node {
	my ( $self, $twig, $node_elt ) = @_;	
	my $node_obj = $factory->create_node;
	my $id = $node_elt->att('ID');
	$self->{'_node_of'}->{$id} = $node_obj;
	if ( my $parent = $node_elt->parent->parent ) {
		$self->{'_parent_of'}->{$id} = $parent->att('ID');
	}
	$self->{'_tree'}->insert($node_obj);
	my $dict = $factory->create_dictionary;
	for my $child_elt ( $node_elt->children ) {		
		if ( $child_elt->tag eq 'NODES' or $child_elt->tag eq 'OTHERNAMES' ) {
			next;
		}
		elsif ( $child_elt->tag eq 'NAME' ) {
			if (my $name = $child_elt->text) {
				$name =~ m/[ ()]/ ? $node_obj->set_name("'". $name . "'") : $node_obj->set_name($name);
			}			
		}
		elsif ( $child_elt->tag eq 'DESCRIPTION' ) {
		    $dict->insert(
		        $factory->create_annotation(
		            '-tag'    => 'string',
		            '-value'  => $child_elt->text,
		            '-xml_id' => 'description',
		        )
		    );
		}
		elsif ( my $text = $child_elt->text ) {
		    $dict->insert(
		        $factory->create_annotation(
		            '-tag'    => 'string',
		            '-value'  => $text,
		            '-xml_id' => $child_elt->tag,
		        )
		    );
		}		
	}
	for my $att_name ( $node_elt->att_names ) {
		if ( $att_name eq 'COMBINATION_DATE' ) {
		    $dict->insert(
		        $factory->create_annotation(
                    '-tag'    => 'string',
                    '-value'  => $node_elt->att($att_name),
                    '-xml_id' => $att_name,
		        )
		    );
		}
		else {
		    $dict->insert(
		        $factory->create_annotation(
		            '-tag'    => 'integer',
		            '-value'  => $node_elt->att($att_name),
		            '-xml_id' => $att_name,
		        )
		    );
		}
	}
	$node_obj->add_dictionary( $dict );
	$twig->purge;
}

1;