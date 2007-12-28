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
);

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
        throw 'Bio::Phylo::Util::Exceptions::UnknownMethod' => "No such method: $method";
    }
}