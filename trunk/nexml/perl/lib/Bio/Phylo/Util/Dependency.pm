package Bio::Phylo::Util::Dependency;
BEGIN {
    use Bio::Phylo::Util::Exceptions 'throw';
    use Bio::Phylo::Util::CONSTANT 'looks_like_class';
    sub import {
        my $class = shift;
        looks_like_class $_ for @_;
    }
}
1;