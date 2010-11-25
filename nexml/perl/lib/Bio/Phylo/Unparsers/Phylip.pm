package Bio::Phylo::Unparsers::Phylip;
use strict;
use Bio::Phylo::IO ();
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(:objecttypes looks_like_object);
use vars qw(@ISA);

@ISA=qw(Bio::Phylo::IO);

=head1 NAME

Bio::Phylo::Unparsers::Phylip - Serializer used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module unparses a Bio::Phylo data structure into an input file for
PHYLIP and RAxML. The file format (as it is interpreted
here) consists of:

=over

=item first line

the number of species, a space, the number of characters

=item subsequent lines

ten-character species name, sequence

=back

Here is an example of what the output might look like:

 4 2
 Species_1 AC
 Species_2 AG
 Species_3 GT
 Species_4 GG

To the unparse() function pass a matrix object or a project
(whose first matrix will be serialized) as value of the '-phylo' 
argument. After serialization, any shortened phylip-specific 
names (which need to be 10 characters long) will have been assigned
to the 'phylip_name' slot of set_generic. Example:

 my $phylip_string = unparse(
 	-format => 'phylip',
 	-phylo  => $matrix,
 );
 for my $seq ( @{ $matrix->get_entities } ) {
    # this returns the shortened name, which is unique to the matrix
 	my $phylip_name = $seq->get_generic('phylip_name');
 }

The phylip module is called by the L<Bio::Phylo::IO> object, so
look there to learn about parsing and serializing in general.

=begin comment

 Type    : Constructor
 Title   : new
 Usage   : my $phylip = Bio::Phylo::Unparsers::Phylip->_new;
 Function: Initializes a Bio::Phylo::Unparsers::Phylip object.
 Alias   :
 Returns : A Bio::Phylo::Unparsers::Phylip object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;
    my $self  = {};
    if (@_) {
        my %opts = @_;
        foreach my $key ( keys %opts ) {
            my $localkey = uc $key;
            $localkey =~ s/-//;
            unless ( ref $opts{$key} ) {
                $self->{$localkey} = uc $opts{$key};
            }
            else {
                $self->{$localkey} = $opts{$key};
            }
        }
    }
    bless $self, $class;
    return $self;
}

=begin comment

 Type    : Unparser
 Title   : _to_string
 Usage   : my $str = $phylip->_to_string;
 Function: Unparses a Bio::Phylo::Matrices::Matrix object into a phylip formatted string.
 Returns : SCALAR
 Args    : Bio::Phylo::Matrices::Matrix

=end comment

=cut

sub _to_string {
    my $self = shift;
    my $obj = $self->{'PHYLO'};
    my $matrix;
    eval {
    	$matrix = $obj if looks_like_object $obj, _MATRIX_;
    };
    if ( $@ ) {
    	undef($@);
    	eval {
    		($matrix) = @{ $obj->get_matrices } if looks_like_object $obj, _PROJECT_;
    	};
    	if ( $@ or not $matrix ) {
			throw 'ObjectMismatch'  => 'Invalid object!';
    	}
    }
	my $string = $matrix->get_ntax() . ' ' . $matrix->get_nchar() . "\n";
	my ( %seq_for_id, %phylip_name_for_id, @ids, %seen_name );
	for my $seq ( @{ $matrix->get_entities } ) {
		my $id = $seq->get_id;
		$seq_for_id{$id} = $seq->get_char;
		my $name = $seq->get_internal_name;
		push @ids, $id;
		if ( length($name) <= 10 ) {
			my $phylip_name = $name . ( ( 10 - length($name) ) x ' ');
			if ( ! $seen_name{$phylip_name} ) {
				$seen_name{$phylip_name}++;
				$phylip_name_for_id{$id} = $phylip_name;				
			}
			else {
				my $counter = 1;
				while( $seen_name{$phylip_name} ) {
					$phylip_name = substr( $phylip_name, 0, (10-length($counter)));
					$phylip_name .= $counter;
					$counter++;
				}
				$phylip_name_for_id{$id} = $phylip_name;
			}			
		}
		elsif ( length($name) > 10 ) {
			my $phylip_name = substr($name,0,10);
			if ( ! $seen_name{$phylip_name} ) {
				$seen_name{$phylip_name}++;
				$phylip_name_for_id{$id} = $phylip_name;				
			}
			else {
				my $counter = 1;
				while($seen_name{$phylip_name}) {
					$phylip_name = substr($phylip_name,0, (10-length($counter)));
					$phylip_name .= $counter;
					$counter++;
				}
				$phylip_name_for_id{$id} = $phylip_name;
			}
		}
		$seq->set_generic('phylip_name' => $phylip_name_for_id{$id});
	}
	for my $id ( @ids ) {
		$string .= $phylip_name_for_id{$id} . ' ' . $seq_for_id{$id} . "\n";
	}
    return $string;
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The phylip unparser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to create phylip formatted files.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
