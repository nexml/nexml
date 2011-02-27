# $Id$
package Bio::Phylo::Treedrawer::Svg;
use strict;
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Treedrawer::Abstract;
use vars '@ISA';
@ISA=qw(Bio::Phylo::Treedrawer::Abstract);

my $logger = Bio::Phylo::Util::Logger->new;

eval { require SVG };
if ( $@ ) {
	throw 'ExtensionError' => "Error loading the SVG extension: $@";
}
SVG->import(
    '-nocredits' => 1,
    '-inline'    => 1,
    '-indent'    => '    ',
);

my $PI = '3.14159265358979323846';

my %colors;

=head1 NAME

Bio::Phylo::Treedrawer::Svg - Graphics format writer used by treedrawer, no
serviceable parts inside

=head1 DESCRIPTION

This module creates a scalable vector graphic from a Bio::Phylo::Forest::DrawTree
object. It is called by the L<Bio::Phylo::Treedrawer> object, so look there to
learn how to create tree drawings.


=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $svg = Bio::Phylo::Treedrawer::Svg->_new(%args);
 Function: Initializes a Bio::Phylo::Treedrawer::Svg object.
 Alias   :
 Returns : A Bio::Phylo::Treedrawer::Svg object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;
    my %opt = looks_like_hash @_;
    my $self = $class->SUPER::_new(
	%opt, '-api' => SVG->new(
	    'width'  => $opt{'-drawer'}->get_width,
	    'height' => $opt{'-drawer'}->get_height
	)
    );
    $self->_api->tag( 'style', type => 'text/css' )->CDATA(
	"\n\tpolyline { fill: none; stroke: black; stroke-width: 2 }\n"
        . "\tpath { fill: none; stroke: black; stroke-width: 2 }\n"
        . "\tline { fill: none; stroke: black; stroke-width: 2 }\n"
        . "\tcircle.node_circle  {}\n"
        . "\tcircle.taxon_circle {}\n"
        . "\ttext.node_text      {}\n"
        . "\ttext.taxon_text     {}\n"
        . "\tline.scale_bar      {}\n"
        . "\ttext.scale_label    {}\n"
        . "\tline.scale_major    {}\n"
        . "\tline.scale_minor    {}\n"
    );
    return bless $self, $class;
}

sub _finish {
	my $self = shift;
	undef %colors;
	return $self->_api->render;	
}

=begin comment

# required:
# -x1 => $x1,
# -y1 => $y1,
# -x2 => $x2,
# -y2 => $y2,
# -x3 => $x3,
# -y3 => $y3,

# optional:
# -fill   => $fill,
# -stroke => $stroke,
# -width  => $width,
# -url    => $url,
# -api    => $api,

=end comment

=cut

sub _draw_triangle {
	my $self = shift;
	my %args = @_;
	my @coord = qw(-x1 -y1 -x2 -y2 -x3 -y3);
	my (           $x1,$y1,$x2,$y2,$x3,$y3) = @args{@coord};
	my @optional = qw(-fill -stroke -width -url -api);
	my $fill   = $args{'-fill'}   || 'white';
	my $stroke = $args{'-stroke'} || 'black';
	my $width  = $args{'-width'}  || 1;
	my $api = $args{'-api'} || $self->_api;
	$api = $api->tag( 'a', 'xlink:href' => $args{'-url'} ) if $args{'-url'};
	my $points = $self->_api->get_path(
		'x' => [ int $x1, int $x2, int $x3, int $x1 ],
		'y' => [ int $y1, int $y2, int $y3, int $y1 ],
		'-type' => 'polygon',
	);
	delete @args{@coord};
	delete @args{@optional};
	return $api->polygon( 
		%$points, 
		'style' => {
			'fill'   => $fill,
			'stroke' => $stroke,
			'stroke-width' => $width,
		},
		%args
	);	
}

=begin comment

# required:
# -x => $x,
# -y => $y,
# -text => $text,
#
# optional:
# -api  => $api,
# -url  => $url,

=end comment

=cut

sub _draw_text {
	my $self = shift;
	my %args = @_;
	my ( $x, $y, $text ) = @args{qw(-x -y -text)};
	my $api = $args{'-api'} || $self->_api;
	my $url = $args{'-url'};
	delete @args{qw(-x -y -text -api -url)};
	if ( $url ) {
		$api = $api->tag( 'a', 'xlink:href' => $url );
	}
	return $api->tag( 'text', 'x' => $x, 'y' => $y, %args )->cdata($text);
}

=begin comment

# -x => $x,
# -y => $y,
# -width  => $width,
# -stroke => $color,
# -radius => $radius,
# -fill   => $file,
# -api    => $api,
# -url    => $url,

=end comment

=cut

sub _draw_circle {
	my $self = shift;
	my %args = @_;
	my (     $x, $y, $radius, $width, $stroke, $fill, $api, $url) =
	@args{qw(-x  -y  -radius  -width  -stroke  -fill  -api  -url)};
	if ( $radius ) {
		my $svg = $api || $self->_api;
		if ( $url ) {
			$svg = $svg->tag( 'a', 'xlink:href' => $url );
		}
		my %circle = (
			'cx'    => int $x,
			'cy'    => int $y,
			'r'     => int $radius,
			'style' => {
				'fill'   => $fill || 'white',
				'stroke' => $stroke || 'black',
				'stroke-width' => $width || 1,
			},
		);	
		return $svg->tag( 'circle', %circle );
	}	
}

=begin comment

# -x1 => $x1,
# -x2 => $x2,
# -y1 => $y1,
# -y2 => $y2,
# -width => $width,
# -color => $color

=end comment

=cut

sub _draw_curve {
	my $self = shift;
	my %args = @_;
	my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
	my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
	delete @args{@keys};
	my $points = qq{M$x1,$y1 C$x1,$y2 $x2,$y2 $x2,$y2};
	return $self->_api->path( 
		'd'     => $points,
		'style' => {
			'stroke'       => $color || 'black',
			'stroke-width' => $width || 1,
		}
	);
}

=begin comment

# -x1 => $x1,
# -x2 => $x2,
# -y1 => $y1,
# -y2 => $y2,
# -width => $width,
# -color => $color

=end comment

=cut

sub _draw_multi {
	my $self = shift;
	my %args = @_;
	my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
	my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
	delete @args{@keys};
	my $points = qq{$x1,$y1 $x1,$y2 $x2,$y2};
	return $self->_api->polyline(
		'points' => $points,
		'style' => {
			'stroke'       => $color || 'black',
			'stroke-width' => $width || 1,
		},
		%args,
	);	
}

=begin comment

# -x1 => $x1,
# -x2 => $x2,
# -y1 => $y1,
# -y2 => $y2,
# -width => $width,
# -color => $color

=end comment

=cut

sub _draw_line {
	my $self = shift;
	my %args = @_;
	my @keys = qw(-x1  -y1  -x2  -y2  -width  -color );
	my (          $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
	delete @args{@keys};
	return $self->_api->line(
		'x1' => $x1,
		'y1' => $y1,
		'x2' => $x2,
		'y2' => $y2,
		'style' => {
			'stroke'       => $color || 'black',
			'stroke-width' => $width || 1,
		},
		%args
	);
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
	$self->_tree->visit_level_order(
		sub{
			my $node = shift;
			if ( not $node->get_collapsed ) {
				my $cx = int $node->get_x;
				my $cy = int $node->get_y;
				my $r;
				if ( $node->is_internal ) {
				    $r = int $self->_drawer->get_node_radius($node);
				}
				else {
				    $r = int $self->_drawer->get_tip_radius($node);
				}
				if ( my $pievalues = $node->get_generic('pie') ) {
					my @keys  = keys %{$pievalues};
					my $start = -90;
					my $total;
					$total += $pievalues->{$_} for @keys;
					my $pie = $self->_api->tag(
						'g',
						'id'        => 'pie_' . $node->get_id,
						'transform' => "translate($cx,$cy)",
					);
					for my $i ( 0 .. $#keys ) {
						next if not $pievalues->{ $keys[$i] };
						my $slice = $pievalues->{ $keys[$i] } / $total * 360;
						my $color = $colors{ $keys[$i] };
						if ( not $color ) {
							my $gray = int( ( $i / $#keys ) * 256 );
							$colors{ $keys[$i] } = "rgb($gray,$gray,$gray)";
						}
						my $do_arc  = 0;
						my $radians = $slice * $PI / 180;
						$do_arc++ if $slice > 180;
						my $radius = $r - 2;
						my $ry     = $radius * sin($radians);
						my $rx     = $radius * cos($radians);
						my $g      = $pie->tag( 'g', 'transform' => "rotate($start)" );
						$g->path(
							'style' => { 'fill' => "$color", 'stroke' => 'none' },
							'd'     => "M $radius,0 A $radius,$radius 0 $do_arc,1 $rx,$ry L 0,0 z"
						);
						$start += $slice;
					}
				}
			}
		}
	);
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
        my $svg  = $self->_api;
        my $tree = $self->_tree;
        my $draw = $self->_drawer;
        my @keys = keys %colors;
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
	    $self->_draw_text(
		'-x' => $x,
		'-y' => ( $draw->get_height - 60 ),
		'-text' => $key || ' ',
		'class' => 'legend_label'
	    );
            $x += $increment;
        }
	$self->_draw_text(
	    '-x' => ( $tree->get_tallest_tip->get_x + $draw->get_text_horiz_offset ),
	    '-y' => ( $draw->get_height - 80 ),
	    '-text' => 'Node value legend',
	    'class' => 'legend_text',
		
	);
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

=head1 CITATION

If you use Bio::Phylo in published research, please cite it:

B<Rutger A Vos>, B<Jason Caravas>, B<Klaas Hartmann>, B<Mark A Jensen>
and B<Chase Miller>, 2011. Bio::Phylo - phyloinformatic analysis using Perl.
I<BMC Bioinformatics> B<12>:63.
L<http://dx.doi.org/10.1186/1471-2105-12-63>

=head1 REVISION

 $Id$

=cut

1;
