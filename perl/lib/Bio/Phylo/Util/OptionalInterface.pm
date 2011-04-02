package Bio::Phylo::Util::OptionalInterface;
use strict;
use Bio::Phylo::Util::CONSTANT 'looks_like_class';
use Bio::Phylo::Util::Logger;

my $logger = Bio::Phylo::Util::Logger->new;

sub import {
    my $class = shift;
    my ( $caller ) = caller;
    for my $iface ( @_ ) {
        eval { looks_like_class $iface };
        if ( $@ ) {
            $logger->info("Couldn't load optional interface $iface");
            undef($@);
        }
        else {
            eval "push \@${caller}::ISA, '$iface'";
            $logger->info("Added interface $iface to $caller");
        }
    }
    
}
1;