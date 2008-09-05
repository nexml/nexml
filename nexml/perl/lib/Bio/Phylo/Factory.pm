package Bio::Phylo::Factory;
use vars '$AUTOLOAD';
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw'looks_like_hash looks_like_class';

my %class = (
    'taxa'   => 'Bio::Phylo::Taxa',
    'taxon'  => 'Bio::Phylo::Taxa::Taxon',
    'datum'  => 'Bio::Phylo::Matrices::Datum',
    'matrix' => 'Bio::Phylo::Matrices::Matrix',
    'forest' => 'Bio::Phylo::Forest',
    'node'   => 'Bio::Phylo::Forest::Node',
    'tree'   => 'Bio::Phylo::Forest::Tree',
    'logger' => 'Bio::Phylo::Util::Logger',
    'drawer' => 'Bio::Phylo::Treedrawer',
);

=head1 NAME

Bio::Phylo::Factory - Object to instantiate other objects.

=head1 SYNOPSIS

 use Bio::Phylo::Factory;
 my $fac = Bio::Phylo::Factory->new;
 my $node = $fac->create_node( '-name' => 'node1' );

 # prints 'Bio::Phylo::Forest::Node'
 print ref $node;

=head1 DESCRIPTION

The factory module is used to create other objects without having to 'use' 
their classes.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Factory constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $fac = Bio::Phylo::Factory->new;
 Function: Initializes a Bio::Phylo::Factory object.
 Returns : A Bio::Phylo::Factory object.
 Args    : (optional) a hash keyed on short names, with
           class names for values. For example, 
           'node' => 'Bio::Phylo::Forest::Node', which 
           will allow you to subsequently call $fac->create_node,
           which will return a Bio::Phylo::Forest::Node object.
           (Note that this example is enabled by default, so you
           don't need to specify it.)

=cut

sub new { 
    my $class = shift;
    if ( @_ ) {
        my %args = looks_like_hash @_;
        while ( my ( $key, $value ) = each %args ) {
            if ( looks_like_class $value ) {
                $class{$key} = $value;
            }
        }
    }
    bless \$class, $class;
}

=back

=head2 FACTORY METHODS

=over

=item create($class, %args)

 Type    : Factory methods
 Title   : create
 Usage   : my $foo = $fac->create('Foo::Class');
 Function: Creates an instance of $class, with constructor arguments %args
 Returns : A Bio::Phylo::Forest object.
 Args    : $class, a class name (required),
           %args, constructor arguments (optional)

=cut

sub create {
    my $self  = shift;
    my $class = shift;
    if ( looks_like_class $class ) {
        return $class->new(@_);
    }
}

sub AUTOLOAD {
    my $self   = shift;
    my $method = $AUTOLOAD;
    $method    =~ s/.*://;
    my $type   = $method;
    $type =~ s/^create_//;
    if ( exists $class{$type} ) {
        my $class = $class{$type};
        my $path = $class;
        $path =~ s|::|/|g;
        $path .= '.pm';
        if ( not $INC{$path} ) {
            require $path;
        }
        return $class{$type}->new(@_);
    }
    elsif ( $method =~ qr/^[A-Z]+$/ ) {
        return;
    }
    else {
        throw 'UnknownMethod' => "No such method: $method";
    }
}

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;