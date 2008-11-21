package Bio::Phylo::Dictionary;
use strict;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT qw(_DICTIONARY_ _NONE_);
use Bio::Phylo::Util::Exceptions 'throw';
use vars '@ISA';
@ISA=qw(Bio::Phylo::Listable);
{    
    sub new { 
        return shift->SUPER::new( '-tag' => 'dict', '-identifiable' => 0, @_ ); 
    }   
    sub add_dictionary {
        throw 'BadArgs' => "Can't attach dictionaries recursively"
    }
    my $TYPE_CONSTANT      = _DICTIONARY_;
    my $CONTAINER_CONSTANT = _NONE_;
    sub _type      { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }     
}
1;