# $Id$
package Bio::Phylo::Treedrawer;
use strict;
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Forest::DrawTree;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(_TREE_ looks_like_number looks_like_object looks_like_hash looks_like_class);
my @fields = qw(WIDTH HEIGHT MODE SHAPE PADDING NODE_RADIUS TIP_RADIUS TEXT_HORIZ_OFFSET TEXT_VERT_OFFSET TEXT_WIDTH TREE _SCALEX _SCALEY SCALE FORMAT);

my $tips = 0.000_000_000_000_01;
my $logger = Bio::Phylo::Util::Logger->new;


=head1 NAME

Bio::Phylo::Treedrawer - Visualizer of tree shapes

=head1 SYNOPSIS

 use Bio::Phylo::IO 'parse';
 use Bio::Phylo::Treedrawer;

 my $string = '((A:1,B:2)n1:3,C:4)n2:0;';
 my $tree = parse( -format => 'newick', -string => $string )->first;

 my $treedrawer = Bio::Phylo::Treedrawer->new(
    -width  => 800,
    -height => 600,
    -shape  => 'CURVY', # curvogram
    -mode   => 'PHYLO', # cladogram
    -format => 'SVG'
 );

 $treedrawer->set_scale_options(
    -width => '100%',
    -major => '10%', # major cross hatch interval
    -minor => '2%',  # minor cross hatch interval
    -label => 'MYA',
 );

 $treedrawer->set_tree($tree);
 print $treedrawer->draw;

=head1 DESCRIPTION

This module prepares a tree object for drawing (calculating coordinates for
nodes) and calls the appropriate format-specific drawer.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Treedrawer constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $treedrawer = Bio::Phylo::Treedrawer->new(
               %args 
           );
 Function: Initializes a Bio::Phylo::Treedrawer object.
 Alias   :
 Returns : A Bio::Phylo::Treedrawer object.
 Args    : none.

=cut

sub new {
    my $class = shift;
    my $self = {
        'WIDTH'             => 500,
        'HEIGHT'            => 500,
        'MODE'              => 'PHYLO',
        'SHAPE'             => 'CURVY',
        'PADDING'           => 50,
        'NODE_RADIUS'       => 1,
        'TIP_RADIUS'        => 1,
        'TEXT_HORIZ_OFFSET' => 6,
        'TEXT_VERT_OFFSET'  => 4,
        'TEXT_WIDTH'        => 150,
        'TREE'              => undef,
        '_SCALEX'           => 1,
        '_SCALEY'           => 1,
        'FORMAT'            => 'Svg',
        'SCALE'             => undef,
    };
    bless $self, $class;
    
    if (@_) {
        my %opts = looks_like_hash @_;
        for my $key ( keys %opts ) {
            my $mutator = lc $key;
            $mutator =~ s/^-/set_/;
            $self->$mutator( $opts{$key} );
        }
    }
    return $self;
}

=back

=head2 MUTATORS

=over

=item set_format()

Sets image format.

 Type    : Mutator
 Title   : set_format
 Usage   : $treedrawer->set_format('Svg');
 Function: Sets the drawer submodule.
 Returns :
 Args    : Name of an image format (currently 
           only Svg supported)

=cut

sub set_format {
    my ( $self, $format ) = @_;
    $format = ucfirst( lc( $format ) );
    if ( looks_like_class __PACKAGE__ . '::' . $format ) {
        $self->{'FORMAT'} = $format;
        return $self;
    }
    else {
        throw 'BadFormat' => "'$format' is not a valid image format";
    }    
}

=item set_width()

Sets image width.

 Type    : Mutator
 Title   : set_width
 Usage   : $treedrawer->set_width(1000);
 Function: sets the width of the drawer canvas.
 Returns :
 Args    : Integer width in pixels.

=cut

sub set_width {
    my ( $self, $width ) = @_;
    if ( looks_like_number $width && $width > 0 ) {
        $self->{'WIDTH'} = $width;
    }
    else {
    	throw 'BadNumber' => "'$width' is not a valid image width";
    }
    return $self;
}

=item set_height()

Sets image height.

 Type    : Mutator
 Title   : set_height
 Usage   : $treedrawer->set_height(1000);
 Function: sets the height of the canvas.
 Returns :
 Args    : Integer height in pixels.

=cut

sub set_height {
    my ( $self, $height ) = @_;
    if ( looks_like_number $height && $height > 0 ) {
        $self->{'HEIGHT'} = $height;
    }
    else {
    	throw 'BadNumber' => "'$height' is not a valid image height";
    }
    return $self;
}

=item set_mode()

Sets tree drawing mode.

 Type    : Mutator
 Title   : set_mode
 Usage   : $treedrawer->set_mode('clado');
 Function: Sets the tree mode, i.e. cladogram 
           or phylogram.
 Returns : Invocant.
 Args    : String, [clado|phylo]

=cut

sub set_mode {
    my ( $self, $mode ) = @_;
    if ( $mode =~ m/^(?:clado|phylo)$/i ) {
        $self->{'MODE'} = uc $mode;
    }
    else {
    	throw 'BadFormat' => "'$mode' is not a valid drawing mode";
    }
    return $self;
}

=item set_shape()

Sets tree drawing shape.

 Type    : Mutator
 Title   : set_shape
 Usage   : $treedrawer->set_shape('rect');
 Function: Sets the tree shape, i.e. 
           rectangular, diagonal or curvy.
 Returns : Invocant.
 Args    : String, [rect|diag|curvy]

=cut

sub set_shape {
    my ( $self, $shape ) = @_;
    if ( $shape =~ m/^(?:rect|diag|curvy)$/i ) {
        $self->{'SHAPE'} = uc $shape;
    }
    else {
    	throw 'BadFormat' => "'$shape' is not a valid drawing shape";
    }
    return $self;
}

=item set_padding()

Sets image padding.

 Type    : Mutator
 Title   : set_padding
 Usage   : $treedrawer->set_padding(100);
 Function: Sets the canvas padding.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_padding {
    my ( $self, $padding ) = @_;
    if ( looks_like_number $padding && $padding > 0 ) {
        $self->{'PADDING'} = $padding;
    }
    else {
    	throw 'BadNumber' => "'$padding' is not a valid padding value";
    }
    return $self;
}

=item set_node_radius()

Sets node radius.

 Type    : Mutator
 Title   : set_node_radius
 Usage   : $treedrawer->set_node_radius(20);
 Function: Sets the node radius in pixels.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_node_radius {
    my ( $self, $radius ) = @_;
    if ( looks_like_number $radius && $radius >= 0 ) {
        $self->{'NODE_RADIUS'} = $radius;
    }
    else {
    	throw 'BadNumber' => "'$radius' is not a valid node radius value";
    }
    return $self;
}

=item set_tip_radius()

Sets tip radius.

 Type    : Mutator
 Title   : set_tip_radius
 Usage   : $treedrawer->set_tip_radius(20);
 Function: Sets the tip radius in pixels.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_tip_radius {
    my ( $self, $radius ) = @_;
    if ( looks_like_number $radius && $radius >= 0 ) {
        $self->{'TIP_RADIUS'} = $radius;
    }
    else {
    	throw 'BadNumber' => "'$radius' is not a valid tip radius value";
    }
    return $self;
}

=item set_text_horiz_offset()

Sets text horizontal offset.

 Type    : Mutator
 Title   : set_text_horiz_offset
 Usage   : $treedrawer->set_text_horiz_offset(5);
 Function: Sets the distance between 
           tips and text, in pixels.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_text_horiz_offset {
    my ( $self, $offset ) = @_;
    if ( looks_like_number $offset ) {
        $self->{'TEXT_HORIZ_OFFSET'} = $offset;
    }
    else {
    	throw 'BadNumber' => "'$offset' is not a valid text horizontal offset value";
    }
    return $self;
}

=item set_text_vert_offset()

Sets text vertical offset.

 Type    : Mutator
 Title   : set_text_vert_offset
 Usage   : $treedrawer->set_text_vert_offset(3);
 Function: Sets the text baseline 
           relative to the tips, in pixels.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_text_vert_offset {
    my ( $self, $offset ) = @_;
    if ( looks_like_number $offset ) {
        $self->{'TEXT_VERT_OFFSET'} = $offset;
    }
    else {
    	throw 'BadNumber' => "'$offset' is not a valid text vertical offset value";
    }
    return $self;
}

=item set_text_width()

Sets text width.

 Type    : Mutator
 Title   : set_text_width
 Usage   : $treedrawer->set_text_width(150);
 Function: Sets the canvas width for 
           terminal taxon names.
 Returns :
 Args    : Integer value in pixels.

=cut

sub set_text_width {
    my ( $self, $width ) = @_;
    if ( looks_like_number $width && $width > 0 ) {
        $self->{'TEXT_WIDTH'} = $width;
    }
    else {
    	throw 'BadNumber' => "'$width' is not a valid text width value";
    }
    return $self;
}

=item set_tree()

Sets tree to draw.

 Type    : Mutator
 Title   : set_tree
 Usage   : $treedrawer->set_tree($tree);
 Function: Sets the Bio::Phylo::Forest::Tree 
           object to unparse.
 Returns :
 Args    : A Bio::Phylo::Forest::Tree object.

=cut

sub set_tree {
    my ( $self, $tree ) = @_;
    if ( looks_like_object $tree, _TREE_ ) {
    	if ( not $tree->isa('Bio::Phylo::Forest::DrawTree') ) {
    		$tree = Bio::Phylo::Forest::DrawTree->new( '-tree' => $tree );
    	}
        $self->{'TREE'} = $tree->negative_to_zero;
    }
    return $self;
}

=item set_scale_options()

Sets time scale options.

 Type    : Mutator
 Title   : set_scale_options
 Usage   : $treedrawer->set_scale_options(
                -width => 400,
                -major => '10%', # major cross hatch interval
                -minor => '2%',  # minor cross hatch interval
                -label => 'MYA',
            );
 Function: Sets the options for time (distance) scale
 Returns :
 Args    : -width => (if a number, like 100, pixel 
                      width is assumed, if a percentage, 
                      scale width relative to longest root
                      to tip path)
           -major => ( ditto, value for major tick marks )
           -minor => ( ditto, value for minor tick marks )
           -label => ( text string displayed next to scale )

=cut

sub set_scale_options {
    my $self = shift;
    if ( ( @_ && !scalar @_ % 2 ) || ( scalar @_ == 1 && ref $_[0] eq 'HASH' ) ) {
        my %o; # %options
        if ( scalar @_ == 1 && ref $_[0] eq 'HASH' ) {
            %o = %{ $_[0] };
        }
        else {
            %o = looks_like_hash @_;
        }
        if ( looks_like_number $o{'-width'} or $o{'-width'} =~ m/^\d+%$/ ) {
            $self->{'SCALE'}->{'-width'} = $o{'-width'};
        }
        else {
            throw 'BadArgs' => "\"$o{'-width'}\" is invalid for '-width'";
        }
        if ( looks_like_number $o{'-major'} or $o{'-major'} =~ m/^\d+%$/ ) {
            $self->{'SCALE'}->{'-major'} = $o{'-major'};
        }
        else {
        	throw 'BadArgs' => "\"$o{'-major'}\" is invalid for '-major'";
        }
        if ( looks_like_number $o{'-minor'} or $o{'-minor'} =~ m/^\d+%$/ ) {
            $self->{'SCALE'}->{'-minor'} = $o{'-minor'};
        }
        else {
            throw 'BadArgs' => "\"$o{'-minor'}\" is invalid for '-minor'"; 
        }
        $self->{'SCALE'}->{'-label'} = $o{'-label'};
    }
    else {
        throw 'OddHash' => 'Odd number of elements in hash assignment';
    }
    return $self;
}

=back

=head2 ACCESSORS

=over

=item get_format()

Gets image format.

 Type    : Mutator
 Title   : get_format
 Usage   : my $format = $treedrawer->get_format;
 Function: Gets the image format.
 Returns :
 Args    : None.

=cut

sub get_format { shift->{'FORMAT'} }

=item get_width()

Gets image width.

 Type    : Mutator
 Title   : get_width
 Usage   : my $width = $treedrawer->get_width;
 Function: Gets the width of the drawer canvas.
 Returns :
 Args    : None.

=cut

sub get_width { shift->{'WIDTH'} }

=item get_height()

Gets image height.

 Type    : Accessor
 Title   : get_height
 Usage   : my $height = $treedrawer->get_height;
 Function: Gets the height of the canvas.
 Returns :
 Args    : None.

=cut

sub get_height { shift->{'HEIGHT'} }

=item get_mode()

Gets tree drawing mode.

 Type    : Accessor
 Title   : get_mode
 Usage   : my $mode = $treedrawer->get_mode('clado');
 Function: Gets the tree mode, i.e. cladogram or phylogram.
 Returns :
 Args    : None.

=cut

sub get_mode { shift->{'MODE'} }

=item get_shape()

Gets tree drawing shape.

 Type    : Accessor
 Title   : get_shape
 Usage   : my $shape = $treedrawer->get_shape;
 Function: Gets the tree shape, i.e. rectangular, 
           diagonal or curvy.
 Returns :
 Args    : None.

=cut

sub get_shape { shift->{'SHAPE'} }

=item get_padding()

Gets image padding.

 Type    : Accessor
 Title   : get_padding
 Usage   : my $padding = $treedrawer->get_padding;
 Function: Gets the canvas padding.
 Returns :
 Args    : None.

=cut

sub get_padding { shift->{'PADDING'} }

=item get_node_radius()

Gets node radius.

 Type    : Accessor
 Title   : get_node_radius
 Usage   : my $node_radius = $treedrawer->get_node_radius;
 Function: Gets the node radius in pixels.
 Returns : SCALAR
 Args    : None.

=cut

sub get_node_radius { shift->{'NODE_RADIUS'} }

=item get_tip_radius()

Gets tip radius.

 Type    : Accessor
 Title   : get_tip_radius
 Usage   : my $tip_radius = $treedrawer->get_tip_radius;
 Function: Gets the tip radius in pixels.
 Returns : SCALAR
 Args    : None.

=cut

sub get_tip_radius { shift->{'TIP_RADIUS'} }

=item get_text_horiz_offset()

Gets text horizontal offset.

 Type    : Accessor
 Title   : get_text_horiz_offset
 Usage   : my $text_horiz_offset = 
           $treedrawer->get_text_horiz_offset;
 Function: Gets the distance between 
           tips and text, in pixels.
 Returns : SCALAR
 Args    : None.

=cut

sub get_text_horiz_offset { shift->{'TEXT_HORIZ_OFFSET'} }

=item get_text_vert_offset()

Gets text vertical offset.

 Type    : Accessor
 Title   : get_text_vert_offset
 Usage   : my $text_vert_offset = 
           $treedrawer->get_text_vert_offset;
 Function: Gets the text baseline relative 
           to the tips, in pixels.
 Returns :
 Args    : None.

=cut

sub get_text_vert_offset { shift->{'TEXT_VERT_OFFSET'} }

=item get_text_width()

Gets text width.

 Type    : Accessor
 Title   : get_text_width
 Usage   : my $textwidth = 
           $treedrawer->get_text_width;
 Function: Returns the canvas width 
           for terminal taxon names.
 Returns :
 Args    : None.

=cut

sub get_text_width { shift->{'TEXT_WIDTH'} }

=item get_tree()

Gets tree to draw.

 Type    : Accessor
 Title   : get_tree
 Usage   : my $tree = $treedrawer->get_tree;
 Function: Returns the Bio::Phylo::Forest::Tree 
           object to unparse.
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : None.

=cut

sub get_tree { shift->{'TREE'} }

=item get_scale_options()

Gets time scale option.

 Type    : Accessor
 Title   : get_scale_options
 Usage   : my %options = %{ 
               $treedrawer->get_scale_options  
           };
 Function: Returns the time/distance 
           scale options.
 Returns : A hash ref.
 Args    : None.

=cut

sub get_scale_options { shift->{'SCALE'} }

=begin comment

 Type    : Internal method.
 Title   : _set_scalex
 Usage   : $treedrawer->_set_scalex($scalex);
 Function:
 Returns :
 Args    :

=end comment

=cut

sub _set_scalex {
    my $self = shift;
    if ( looks_like_number $_[0] ) {
        $self->{'_SCALEX'} = $_[0];
    }
    else {
    	throw 'BadNumber' => "\"$_[0]\" is not a valid number value";
    }
    return $self;
}

sub _get_scalex { shift->{'_SCALEX'} }

=begin comment

 Type    : Internal method.
 Title   : _set_scaley
 Usage   : $treedrawer->_set_scaley($scaley);
 Function:
 Returns :
 Args    :

=end comment

=cut

sub _set_scaley {
    my $self = shift;
    if ( looks_like_number $_[0] ) {
        $self->{'_SCALEY'} = $_[0];
    }
    else {
    	throw 'BadNumber' => "\"$_[0]\" is not a valid integer value";
    }
    return $self;
}

sub _get_scaley { shift->{'_SCALEY'} }

=back

=head2 TREE DRAWING

=over

=item draw()

Creates tree drawing. Requires L<SVG>;

 Type    : Unparsers
 Title   : draw
 Usage   : my $drawing = $treedrawer->draw;
 Function: Unparses a Bio::Phylo::Forest::Tree 
           object into a drawing.
 Returns : SCALAR
 Args    :
 Notes   : This will only work if you have the SVG module
           from CPAN installed on your system.

=cut

sub draw {
    my $self = shift;
    if ( !$self->get_tree ) {
        throw 'BadArgs' => "Can't draw an undefined tree";
    }
    my $root = $self->get_tree->get_root;

    #Reset the stored data in the tree
    $self->_reset_internal($root);    
        
    if ( $self->get_mode eq 'CLADO' ) {
        $self->_compute_rooted_clado_coordinates;
    }
    elsif ( $self->get_mode eq 'PHYLO' ) {
        $self->_compute_rooted_phylo_coordinates;
    }    
    
    return $self->render;
}

sub _compute_rooted_clado_coordinates {
	my $self = shift;
	my $root = $self->get_tree->get_root;
    my $tips = $self->get_tree->calc_number_of_terminals;
    my ( $width,   $height )    = ( $self->get_width,   $self->get_height );
    my ( $padding, $textwidth ) = ( $self->get_padding, $self->get_text_width );
    my $maxpath = $root->calc_max_nodes_to_tips;
    $self->_set_scalex(
        ( ( $width - ( ( 2 * $padding ) + $textwidth ) ) / $maxpath ) );
    $self->_set_scaley( ( ( $height - ( 2 * $padding ) ) / ( $tips + 1 ) ) );
    $self->_x_positions_clado;    
    $self->_y_terminals(0);
    $self->_y_terminals($root);
    $self->get_tree->get_root->set_y(0);
    $self->_y_internals;	
}

sub _compute_rooted_phylo_coordinates {
	my $self = shift;
	my $root = $self->get_tree->get_root;
    my $tips = $self->get_tree->calc_number_of_terminals;
    my ( $width,   $height )    = ( $self->get_width,   $self->get_height );
    my ( $padding, $textwidth ) = ( $self->get_padding, $self->get_text_width );
    my $maxpath = $root->calc_max_path_to_tips;
    if ( not $maxpath ) {
        $logger->warn("no branch lengths on tree, switching to clado mode");
        $self->_compute_clado_coordinates;
        return;
    }
    $self->_set_scalex(
        ( ( $width - ( ( 2 * $padding ) + $textwidth ) ) / $maxpath ) );
    $self->_set_scaley( ( ( $height - ( 2 * $padding ) ) / ( $tips + 1 ) ) );
    $self->_x_positions_phylo;    
    $self->_y_terminals(0);
    $self->_y_terminals($root);
    $self->get_tree->get_root->set_y(0);
    $self->_y_internals;	
}

=item render()

Renders tree based on pre-computed node coordinates. You would typically use
this method if you have passed a Bio::Phylo::Forest::DrawTree on which you
have already calculated the node coordinates separately.

 Type    : Unparsers
 Title   : render
 Usage   : my $drawing = $treedrawer->render;
 Function: Unparses a Bio::Phylo::Forest::DrawTree 
           object into a drawing.
 Returns : SCALAR
 Args    :
 Notes   : This will only work if you have the SVG module
           from CPAN installed on your system.

=cut

sub render {
	my $self = shift;
    my $library = looks_like_class __PACKAGE__ . '::' . ucfirst( lc( $self->get_format ) );
    my $drawer = $library->_new(
        '-tree'   => $self->get_tree,
        '-drawer' => $self
    );
    return $drawer->_draw;	
}

=begin comment

 Type    : Internal method.
 Title   : _reset_internal
 Usage   : $treedrawer->_reset_internal;
 Function: resets the set_generic values stored by Treedrawer, this must be 
           called at the start of each draw command or weird results are obtained!
 Returns : nothing
 Args    : treedrawer, node being processed

=end comment

=cut

sub _reset_internal {
    my ($self, $node) = @_;
    my $tree = $self->get_tree;
    $node->set_x(undef);
    $node->set_y(undef);
    my $children = $node->get_children;
    foreach $node (@$children) {
        _reset_internal($self,$node);
    }

}

=begin comment

 Type    : Internal method.
 Title   : _x_positions
 Usage   : $treedrawer->_x_positions;
 Function:
 Returns :
 Args    :

=end comment

=cut

sub _x_positions_phylo {
    my $self    = shift;
    my $tree    = $self->get_tree;
    my $root    = $tree->get_root;
    my $scalex  = $self->_get_scalex;
    my $padding = $self->get_padding;
    foreach my $node ( @{ $tree->get_entities } ) {
        my $x = ( $node->calc_path_to_root * $scalex ) + $padding;
        $node->set_x( $x );
    }
}

=begin comment

 Type    : Internal method.
 Title   : _x_positions_clado
 Usage   : $treedrawer->_x_positions_clado;
 Function:
 Returns :
 Args    :

=end comment

=cut

sub _x_positions_clado {
    my $self    = shift;
    my $tree    = $self->get_tree;
    my $root    = $tree->get_root;
    my $longest = $root->calc_max_nodes_to_tips;
    my $scalex  = $self->_get_scalex;
    my $padding = $self->get_padding;
    for my $tip ( @{ $tree->get_terminals } ) {
        $tip->set_x( $longest * $scalex );
    }
    for my $internal ( @{ $tree->get_internals } ) {
        my $id = $internal->get_id;
        my $longest1 = 0;
        for my $node ( @{ $tree->get_entities } ) {
            my ( $n, $current1 ) = ( $node, 0 );
            if ( $n->is_terminal && $n->get_parent ) {
                while ( $n->get_parent ) {
                    $current1++;
                    $n = $n->get_parent;
                    if ( $n->get_id == $id && $current1 > $longest1 ) {
                        $longest1 = $current1;
                    }
                }
            }
        }
        my $xc = $longest - $longest1;
        $internal->set_x( ( $xc * $scalex ) + $padding );
    }
}

=begin comment

 Type    : Internal method.
 Title   : _y_terminals
 Usage   : $treedrawer->_y_terminals;
 Function:
 Returns :
 Args    : tree root

=end comment

=cut

{

    sub _y_terminals {
        my ($self, $node) = @_;
        if ($node == 0) { $tips = 0.000_000_000_000_01; return; }
        if ( !$node->get_first_daughter ) {
            $tips++;
            $node->set_y( ( $tips * $self->_get_scaley ) + $self->get_padding );
        }
        else {
            $node = $node->get_first_daughter;
            $self->_y_terminals($node);
            while ( $node->get_next_sister ) {
                $node = $node->get_next_sister;
                $self->_y_terminals($node);
            }
        }
    }
}

=begin comment

 Type    : Internal method.
 Title   : _y_internals
 Usage   : $treedrawer->_y_internals;
 Function:
 Returns :
 Args    :

=end comment

=cut

sub _y_internals {
    my $self = shift;
    my $tree = $self->get_tree;
    while ( !$tree->get_root->get_y ) {
        foreach my $e ( @{ $tree->get_internals } ) {
            my $y1 = $e->get_first_daughter->get_y;
            my $y2 = $e->get_last_daughter->get_y;
            if ( $y1 && $y2 ) {
                my $y = ( $y1 + $y2 ) / 2;
                $e->set_y( $y );
            }
        }
    }
}

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo>

The L<Bio::Phylo::Treedrawer> object inherits from the L<Bio::Phylo> object.
Look there for more methods applicable to the treedrawer object.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
