# $Id$
package Bio::Phylo::Treedrawer::Svg;
use strict;
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';
use Bio::Phylo::Util::Exceptions 'throw';

eval { require SVG };
if ( $@ ) {
	throw 'ExtensionError' => "Error loading the SVG extension: $@";
}
SVG->import(
    '-nocredits' => 1,
    '-inline'    => 1,
    '-indent'    => '    ',
);

my @fields = qw(TREE SVG DRAWER);
my $PI = '3.14159265358979323846';

my %colors;

=head1 NAME

Bio::Phylo::Treedrawer::Svg - Graphics format writer used by treedrawer, no serviceable parts
inside

=head1 DESCRIPTION

This module creates a scalable vector graphic from a Bio::Phylo::Forest::DrawTree
object. It is called by the L<Bio::Phylo::Treedrawer> object, so look there to
learn how to create tree drawings. (For extra per-node formatting, attach a hash 
reference to the node, like so: 
$node->set_generic( 'svg' => { 'stroke' => 'red' } ), which outlines
the node, and branch leading up to it, in red.)


=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $svg = Bio::Phylo::Treedrawer::SVG->_new(%args);
 Function: Initializes a Bio::Phylo::Treedrawer::SVG object.
 Alias   :
 Returns : A Bio::Phylo::Treedrawer::SVG object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;
    my $self = {};
    my %opt = looks_like_hash @_;
    $self->{'TREE'}   = $opt{'-tree'};
    $self->{'DRAWER'} = $opt{'-drawer'};
    return bless $self, $class;
}

=begin comment

 Type    : Internal method.
 Title   : _draw
 Usage   : $svg->_draw;
 Function: Main drawing method.
 Returns :
 Args    : None.

=end comment

=cut

sub _draw {
    my $self = shift;
    my $drawer = $self->{'DRAWER'};    
    $self->{'SVG'} = SVG->new( 'width' => $drawer->get_width, 'height' => $drawer->get_height );
    my $svg = $self->{'SVG'};
    $svg->tag( 'style', type => 'text/css' )
      ->CDATA( "\n\tpolyline { fill: none; stroke: black; stroke-width: 2 }\n"
          . "\tpath { fill: none; stroke: black; stroke-width: 2 }\n"
          . "\tline { fill: none; stroke: black; stroke-width: 2 }\n"
          . "\tcircle.node_circle  {}\n"
          . "\tcircle.taxon_circle {}\n"
          . "\ttext.node_text      {}\n"
          . "\ttext.taxon_text     {}\n"
          . "\tline.scale_bar      {}\n"
          . "\ttext.scale_label    {}\n"
          . "\tline.scale_major    {}\n"
          . "\tline.scale_minor    {}\n" );
    for my $node ( @{ $self->{'TREE'}->get_entities } ) {
        my $name = $node->get_name || ' ';
        my $url  = $node->get_url;         
        $name =~ s/_/ /g;
        $name =~ s/^'(.*)'$/$1/;
        $name =~ s/^"(.*)"$/$1/;        
        my ( %class, $r );
        my $is_terminal  = $node->is_terminal;
        $class{'circle'} = $is_terminal ? 'taxon_circle' : 'node_circle';
        $class{'text'}   = $is_terminal ? 'taxon_text'   : 'node_text';
        $r = $is_terminal ? int($drawer->get_tip_radius) : int($drawer->get_node_radius);
        my %circle = ( 
        	'cx'    => int($node->get_x), 
        	'cy'    => int($node->get_y), 
        	'r'     => $r, 
        	'class' => $class{'circle'} 
        );
        my $x = int( $node->get_x + $drawer->get_text_horiz_offset );
        my $y = int( $node->get_y + $drawer->get_text_vert_offset );
        my %text = ( 'x' => $x, 'y' => $y, 'class' => $class{'text'} );
        my %args;
        if ( my $style = $node->get_generic('svg') ) {
        	%args = ( 'style' => $style );
        }
	my $invocant = $url ? $svg->tag( 'a', 'xlink:href' => $url ) : $svg;
        $invocant->tag( 'circle', %circle, %args );
        $svg->tag( 'text', %text, %args )->cdata( $name );        
        if ( $node->get_parent ) {
            $self->_draw_line($node);
        }
    }
    $self->_draw_pies;
    $self->_draw_scale;
    $self->_draw_legend;
    undef %colors;
    return $self->{'SVG'}->render;
}

=begin comment

 Type    : Internal method.
 Title   : _draw_pies
 Usage   : $svg->_draw_pies();
 Function: Draws likelihood pies
 Returns :
 Args    : None.
 Comments: Code cribbed from SVG::PieGraph

=end comment

=cut

sub _draw_pies {
    my $self = shift;
    foreach my $node ( @{ $self->{'TREE'}->get_entities } ) {
        my $cx = int $node->get_x;
        my $cy = int $node->get_y;
        my $r  = int $self->{'DRAWER'}->get_node_radius;
        my $x  =
          int( $node->get_x +
              $self->{'DRAWER'}->get_text_horiz_offset );
        my $y =
          int(
            $node->get_y + $self->{'DRAWER'}->get_text_vert_offset );
        if ( my $pievalues = $node->get_generic('pie') ) {
            my @keys  = keys %{$pievalues};
            my $start = -90;
            my $total;
            foreach my $key (@keys) {
                $total += $pievalues->{$key};
            }
            my $pie = $self->{'SVG'}->tag(
                'g',
                'id'        => 'pie_' . $node->get_id,
                'transform' => "translate($cx,$cy)",
            );
            for ( my $i = 0 ; $i <= $#keys ; $i++ ) {
                next if not $pievalues->{ $keys[$i] };
                my $slice = $pievalues->{ $keys[$i] } / $total * 360;
                my $color = $colors{ $keys[$i] };
                if ( not $color ) {
                    my $gray = int( ( $i / $#keys ) * 256 );
                    $color = sprintf 'rgb(%d,%d,%d)', $gray, $gray, $gray;
                    $colors{ $keys[$i] } = $color;
                }
                my $do_arc  = 0;
                my $radians = $slice * $PI / 180;
                $do_arc++ if $slice > 180;
                my $radius = $r - 2;
                my $ry     = ( $radius * sin($radians) );
                my $rx     = $radius * cos($radians);
                my $g      = $pie->tag( 'g', 'transform' => "rotate($start)" );
                $g->path(
                    'style' => { 'fill' => "$color", 'stroke' => 'none' },
                    'd'     =>
"M $radius,0 A $radius,$radius 0 $do_arc,1 $rx,$ry L 0,0 z"
                );
                $start += $slice;
            }
        }
    }
}

=begin comment

 Type    : Internal method.
 Title   : _draw_scale
 Usage   : $svg->_draw_scale();
 Function: Draws scale for phylograms
 Returns :
 Args    : None

=end comment

=cut

sub _draw_scale {
    my $self    = shift;
    my $drawer  = $self->{'DRAWER'};
    my $svg     = $self->{'SVG'};
    my $tree    = $self->{'TREE'};
    my $root    = $tree->get_root;
    my $rootx   = $root->get_x;
    my $height  = $drawer->get_height;
    my $options = $drawer->get_scale_options;
    if ( $options ) {
        my ( $major, $minor ) = ( $options->{'-major'}, $options->{'-minor'} );
        my $width = $options->{'-width'};
        if ( $width =~ m/^(\d+)%$/ ) {
            $width =
              ( $1 / 100 ) * ( $tree->get_tallest_tip->get_x -
                  $rootx );
        }
        if ( $major =~ m/^(\d+)%$/ ) {
            $major = ( $1 / 100 ) * $width;
        }
        if ( $minor =~ m/^(\d+)%$/ ) {
            $minor = ( $1 / 100 ) * $width;
        }
        my $major_text  = 0;
        my $major_scale = ( $major / $width ) * $root->calc_max_path_to_tips;
        $svg->line(
            'class' => 'scale_bar',
            'x1'    => $rootx,
            'y1'    => ( $height - 5 ),
            'x2'    => $rootx + $width,
            'y2'    => ( $height - 5 ),
        );
        $svg->tag(
            'text',
            'x'     => ( $rootx + $width + $drawer->get_text_horiz_offset ),
            'y'     => ( $height - 5 ),
            'class' => 'scale_label',
        )->cdata( $options->{'-label'} ? $options->{'-label'} : ' ' );
        for ( my $i = $rootx; $i <= ( $rootx + $width ); $i += $major ) {
            $svg->line(
                'class' => 'scale_major',
                'x1'    => $i,
                'y1'    => ( $height - 5 ),
                'x2'    => $i,
                'y2'    => ( $height - 25 ),
            );
            $svg->tag(
                'text',
                'x'     => $i,
                'y'     => ( $height - 35 ),
                'class' => 'major_label',
            )->cdata( $major_text ? $major_text : ' ' );
            $major_text += $major_scale;
        }
        for ( my $i = $rootx; $i <= ( $rootx + $width ); $i += $minor ) {
            next if not $i % $major;
            $svg->line(
                'class' => 'scale_minor',
                'x1'    => $i,
                'y1'    => ( $height - 5 ),
                'x2'    => $i,
                'y2'    => ( $height - 15 ),
            );
        }
    }
}

=begin comment

 Type    : Internal method.
 Title   : _draw_legend
 Usage   : $svg->_draw_legend();
 Function: Draws likelihood pie legend
 Returns :
 Args    : None

=end comment

=cut

sub _draw_legend {
    my $self = shift;
    if (%colors) {
        my $svg       = $self->{'SVG'};
        my $tree      = $self->{'TREE'};
        my $draw      = $self->{'DRAWER'};
        my @keys      = keys %colors;
        my $increment =
          ( $tree->get_tallest_tip->get_x -
              $tree->get_root->get_x ) / scalar @keys;
        my $x = $tree->get_root->get_x + 5;
        foreach my $key (@keys) {
            $svg->rectangle(
                'x'      => $x,
                'y'      => ( $draw->get_height - 90 ),
                'width'  => ( $increment - 10 ),
                'height' => 10,
                'id'     => 'legend_' . $key,
                'style'  => {
                    'fill'         => $colors{$key},
                    'stroke'       => 'black',
                    'stroke-width' => '2',
                },
            );
            $svg->tag(
                'text',
                'x'     => $x,
                'y'     => ( $draw->get_height - 60 ),
                'class' => 'legend_label',
            )->cdata( $key ? $key : ' ' );
            $x += $increment;
        }
        $svg->tag(
            'text',
            'x' => (
                $tree->get_tallest_tip->get_x +
                  $draw->get_text_horiz_offset
            ),
            'y'     => ( $draw->get_height - 80 ),
            'class' => 'legend_text',
        )->cdata('Node value legend');
    }
}

=begin comment

 Type    : Internal method.
 Title   : _draw_line
 Usage   : $svg->_draw_line($node);
 Function: Draws internode between $node and $node->get_parent
 Returns :
 Args    : A node that is not the root.

=end comment

=cut

sub _draw_line {
    my ( $self, $node ) = @_;
    my $pnode      = $node->get_parent;
    my $node_hash  = $node->get_generic;
    my $pnode_hash = $pnode->get_generic;
    my ( $x1, $x2, $style ) =
      ( int $pnode->get_x, int $node->get_x, $node_hash->{'svg'} );
    my ( $y1, $y2 ) = ( int $pnode->get_y, int $node->get_y );
    if ( $self->{'DRAWER'}->get_shape eq 'CURVY' ) {
        my $points = qq{M$x1,$y1 C$x1,$y2 $x2,$y2 $x2,$y2};
        if ($style) {
            $self->{'SVG'}->path(
                'd'     => $points,
                'style' => $style,
            );
        }
        else {
            $self->{'SVG'}->path( 'd' => $points, );
        }
    }
    elsif ( $self->{'DRAWER'}->get_shape eq 'RECT' ) {
        my $points = qq{$x1,$y1 $x1,$y2 $x2,$y2};
        if ($style) {
            $self->{'SVG'}->polyline(
                'points' => $points,
                'style'  => $style,
            );
        }
        else {
            $self->{'SVG'}->polyline( 'points' => $points, );
        }
    }
    elsif ( $self->{'DRAWER'}->get_shape eq 'DIAG' ) {
        if ($style) {
            $self->{'SVG'}->line(
                'x1'    => $x1,
                'y1'    => $y1,
                'x2'    => $x2,
                'y2'    => $y2,
                'style' => $style,
            );
        }
        else {
            $self->{'SVG'}->line(
                'x1' => $x1,
                'y1' => $y1,
                'x2' => $x2,
                'y2' => $y2,
            );
        }
    }
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Treedrawer>

The svg treedrawer is called by the L<Bio::Phylo::Treedrawer> object. Look there
to learn how to create tree drawings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
