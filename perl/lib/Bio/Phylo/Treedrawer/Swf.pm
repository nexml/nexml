package Bio::Phylo::Treedrawer::Swf;
use strict;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';

eval { require SWF::Builder };
if ( $@ ) {
    throw 'ExtensionError' => "Error loading the SWF::Builder extension: $@";
}
my $PI = '3.14159265358979323846';
my %colors;

=head1 NAME

Bio::Phylo::Treedrawer::Swf - Graphics format writer used by treedrawer, no serviceable parts
inside

=head1 DESCRIPTION

This module creates a flash movie from a Bio::Phylo::Forest::DrawTree
object. It is called by the L<Bio::Phylo::Treedrawer> object, so look there to
learn how to create tree drawings.


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
    my %opt = looks_like_hash @_;
    my $self = {
        'TREE'   => $opt{'-tree'},
        'DRAWER' => $opt{'-drawer'},
        'API'    => SWF::Builder->new(
            'FrameRate' => 15,
            'FrameSize' => [
                0,
                0,
                $opt{'-drawer'}->get_width,
                $opt{'-drawer'}->get_height
            ],
            'BackgroundColor' => 'ffffff'
        )
    };    
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
    $self->{'TREE'}->visit_depth_first(
        '-post' => sub {
            my $node = shift;
            my $name = $node->get_name || ' ';
            $name =~ s/_/ /g;
            $name =~ s/^'(.*)'$/$1/;
            $name =~ s/^"(.*)"$/$1/;                          
            if ( $node->get_parent ) {
                $self->_draw_line($node);
            }
            $self->_draw_circle_node( $node );
            $self->_draw_text( $node, $name );
        }
    );
    $self->_draw_scale;
    return $self->_finish;
}

sub _finish {
    my $self = shift;
    require File::Temp;
    my ($fh, $filename) = File::Temp::tempfile();
    $self->_api->save('file.swf');
}

sub _draw_line {
    my ( $self, $node ) = @_;
    my $parent = $node->get_parent;
    my $d = $self->{'DRAWER'};
    my $shape = $d->get_shape;
    my $line;
    if ( $shape =~ m/^c/i ) {
        $line = $self->_draw_curve(
            $node->get_x, $node->get_y,
            $parent->get_x, $node->get_y,
            $parent->get_x, $parent->get_y,
            $node->get_branch_width,
            $node->get_branch_color
        )
    }
    elsif ( $shape =~ m/^r/i ) {
        $line = $self->_draw_multi(
            $node->get_x, $node->get_y,
            $parent->get_x, $node->get_y,
            $parent->get_x, $parent->get_y,
            $node->get_branch_width,
            $node->get_branch_color
        )
    }
    else {
        $line = $self->_draw_diagonal(
            $node->get_x, $node->get_y,
            $parent->get_x, $parent->get_y,
            $node->get_branch_width,
            $node->get_branch_color
        )
    }
    return $line->place;
}

sub _draw_curve {
    my ( $self, $x1, $y1, $x2, $y2, $x3, $y3, $width, $color ) = @_;
    return $self->_api
        ->new_shape
        ->linestyle($width || 1,$color || 'cccccc')
        ->moveto($x1,$y1)
        ->curveto($x1,$y1,$x1,$y1,$x2,$y2,$x3,$y3);
}

sub _draw_diagonal {
    my ( $self, $x1, $y1, $x2, $y2, $width, $color ) = @_;
    return $self->_api
        ->new_shape
        ->linestyle($width || 1,$color || 'cccccc')
        ->moveto($x1,$y1)        
        ->lineto( $x1,$y1, $x2,$y2 );
}

sub _draw_multi {
    my ( $self, $x1, $y1, $x2, $y2, $x3, $y3, $width, $color ) = @_;
    return $self->_api
        ->new_shape
        ->linestyle($width || 1,$color || 'cccccc')
        ->moveto($x1,$y1)        
        ->lineto( $x1,$y1, $x2,$y2, $x3,$y3 );
}

sub _draw_text {
    my ( $self, $node, $text ) = @_;
    my $mc = $self->_api;
    my $style = $node->get_font_style;
    my $html;
    if ( not $self->{'FONT'} ) {
        $self->{'FONT'} = $mc->new_font('/Users/rvosa/Desktop/AppleGothic.ttf');
    }
    if ( my $url = $node->get_url ) {
        $html = "<a href='$url' style='$style'>$text</a>";
    }
    else {
        $html = "<span style='$style'>$text</span>";
    }
    return $self->_draw_raw_text(
        $node->get_x + $node->get_text_horiz_offset,
        $node->get_y + $node->get_text_vert_offset,
        $html,
        $node->get_font_size
    );
}

sub _draw_raw_text {
    my ($self,$x,$y,$text,$size) = @_;
    my $textobj = $self->_api->new_html_text
        ->font($self->{'FONT'})
        ->size($size || 12)
        ->text($text);
    return $textobj->place->moveto($x,$y);    
}

sub _draw_circle_node {
    my ( $self, $node ) = @_;
    my $circle = $self->_api
        ->new_shape
        ->fillstyle( $node->get_node_colour || '000000' )
        ->linestyle(
            $node->get_branch_width
            || 1,
            $node->get_node_outline_colour
            || $node->get_branch_color
            || '000000' )
        ->circle( $node->get_radius );        
    warn $circle;
    return $circle->place->moveto( int($node->get_x), int($node->get_y) );
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
    my $swf     = $self->_api;
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
        $self->_draw_diagonal(
            $rootx,        # x1
            $height-5,     # y1
            $rootx+$width, # x2
            $height-5,     # y2
            $self->{'TREE'}->get_branch_width,
            $self->{'TREE'}->get_branch_color,
        )->place;

        # scale legend
        $self->_draw_raw_text(
            ( $rootx + $width + $drawer->get_text_horiz_offset ),
            ( $height -25 ),
            $options->{'-label'},
            $tree->get_font_size,            
        );
        
        for ( my $i = $rootx; $i <= ( $rootx + $width ); $i += $major ) {
            $self->_draw_diagonal(
                $i,
                $height-5,
                $i,
                $height-25,                            
                $self->{'TREE'}->get_branch_width,
                $self->{'TREE'}->get_branch_color,                
            )->place;

            # hash numbers
            $self->_draw_raw_text(
                $i - 5,
                ( $height - 45 ),
                defined($major_text) ? $major_text : ' ',
                $tree->get_font_size,            
            );
            
            $major_text += $major_scale;
        }
        for ( my $i = $rootx; $i <= ( $rootx + $width ); $i += $minor ) {
            next if not $i % $major;
            $self->_draw_diagonal(
                $i,
                $height-5,
                $i,
                $height-15,
                $self->{'TREE'}->get_branch_width,
                $self->{'TREE'}->get_branch_color,                
            )->place;
        }
    }
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
    for my $node ( @{ $self->{'TREE'}->get_entities } ) {
        my $cx = int $node->get_x;
        my $cy = int $node->get_y;
        my $r  = int $self->{'DRAWER'}->get_node_radius;
        my $x  = int( $node->get_x + $self->{'DRAWER'}->get_text_horiz_offset );
        my $y  = int( $node->get_y + $self->{'DRAWER'}->get_text_vert_offset );
        if ( my $pievalues = $node->get_generic('pie') ) {
            my @keys  = keys %{$pievalues};
            my $start = -90;
            my $total;
            $total += $pievalues->{$_} for @keys;

            #my $pie = $self->{'SVG'}->tag(
            #    'g',
            #    'id'        => 'pie_' . $node->get_id,
            #    'transform' => "translate($cx,$cy)",
            #);
            for my $i ( 0 .. $#keys ) {
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
#                my $g      = $pie->tag( 'g', 'transform' => "rotate($start)" );
#                $g->path(
#                    'style' => { 'fill' => "$color", 'stroke' => 'none' },
#                    'd'     =>
#"M $radius,0 A $radius,$radius 0 $do_arc,1 $rx,$ry L 0,0 z"
#                );
                $start += $slice;
            }
        }
    }
}

sub _api { shift->{'API'} }

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Treedrawer>

The svg treedrawer is called by the L<Bio::Phylo::Treedrawer> object. Look there
to learn how to create tree drawings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Svg.pm 1264 2010-03-08 16:15:24Z rvos $

=cut

1;
