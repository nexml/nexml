package Bio::Phylo::Set;
use vars '@ISA';
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT '_NONE_';
@ISA=qw(Bio::Phylo::Listable);

=head1 NAME

Bio::Phylo::Set - Object to manage subsets of the contents of listable containers 

=head1 SYNOPSIS

 use Bio::Phylo::Forest;
 use Bio::Phylo::Forest::Tree;
 use Bio::Phylo::Set;
 
 my $forest = Bio::Phylo::Forest->new;
 my $tree = Bio::Phylo::Forest::Tree->new;
 $forest->insert($tree);
 
 my $set = Bio::Phylo::Set->new( -name => 'TreeSet1' );
 $forest->add_set($set);
 $forest->add_to_set($tree,$set); # $tree is now part of TreeSet1

=head1 DESCRIPTION

Many Bio::Phylo objects are segmented, i.e. they contain one or more subparts 
of the same type. For example, a matrix contains multiple rows; each row 
contains multiple cells; a tree contains nodes, and so on. (Segmented objects
all inherit from L<Bio::Phylo::Listable>.) In many cases it is useful to be
able to define subsets of the contents of segmented objects, for example
sets of taxon objects inside a taxa block. The Bio::Phylo::Listable object
allows this through a number of methods (add_set, remove_set, add_to_set,
remove_from_set etc.). Those methods delegate the actual management of the set
contents to the Bio::Phylo::Set object, the class whose documentation you're
reading now. Consult the documentation for L<Bio::Phylo::Listable/SETS MANAGEMENT> 
for more information on how to use this feature.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : my $anno = Bio::Phylo::Set->new;
 Function: Initializes a Bio::Phylo::Set object.
 Returns : A Bio::Phylo::Set object.
 Args    : optional constructor arguments are key/value
 		   pairs where the key corresponds with any of
 		   the methods that starts with set_ (i.e. mutators) 
 		   and the value is the permitted argument for such 
 		   a method. The method name is changed such that,
 		   in order to access the set_value($val) method
 		   in the constructor, you would pass -value => $val

=cut

{
    my $NONE = _NONE_;
    sub new {
        return shift->SUPER::new( '-tag' => 'class', @_ );
    }
    
=back

=head2 TESTS

=over

=item can_contain()

Tests if argument can be inserted in invocant.

 Type    : Test
 Title   : can_contain
 Usage   : &do_something if $listable->can_contain( $obj );
 Function: Tests if $obj can be inserted in $listable
 Returns : BOOL
 Args    : An $obj to test

=cut    

    sub can_contain {
        my ( $self, @obj ) = @_;
        for my $obj ( @obj ) {
            return 0 if ref $obj;
        }
        return 1;        
    }
    sub _container { $NONE }
    sub _type { $NONE }
}

=back

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

Consult the documentation for L<Bio::Phylo::Listable/SETS MANAGEMENT> for more info 
on how to define subsets of the contents of segmented objects.

=head2 Superclasses

=over

=item L<Bio::Phylo::Listable>

This object inherits from L<Bio::Phylo::Listable>, so methods
defined there are also applicable here.

=back

=head1 REVISION

 $Id: Listable.pm 677 2008-10-22 02:19:41Z rvos $

=cut

1;