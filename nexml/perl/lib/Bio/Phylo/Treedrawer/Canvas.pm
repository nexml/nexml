package Bio::Phylo::Treedrawer::Canvas;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::Logger ':levels';
use strict;
use warnings;
use Bio::Phylo::Treedrawer::Abstract;
use vars '@ISA';
@ISA=qw(Bio::Phylo::Treedrawer::Abstract);

my $logger = Bio::Phylo::Util::Logger->new;

=head1 NAME

Bio::Phylo::Treedrawer::Canvas - Graphics format writer used by treedrawer, no
serviceable parts inside

=head1 DESCRIPTION

This module creates an HTML5 canvas graphic from a Bio::Phylo::Forest::DrawTree
object. It is called by the L<Bio::Phylo::Treedrawer> object, so look there to
learn how to create tree drawings.

=cut

sub _new {
    my $class = shift;
    my %args = @_;
    my $tmpl = do { local $/; <DATA> };
    my $canvas = sprintf(
        $tmpl,
        'myCanvas',
        $args{'-drawer'}->get_width,
        $args{'-drawer'}->get_height,
        'myCanvas'
    );
    my $self = $class->SUPER::_new(%args,'-api'=>\$canvas);    
    return bless $self, $class;
}

sub _finish {
    my $self = shift;
    my $api = $self->_api;
    my $shape = $self->_drawer->get_shape;
    if ( $shape =~ /^c/i ) {
        $$api .= "drawCurvedTree(branches);\n";
    }
    elsif ( $shape =~ /^r/i ) {
        $$api .= "drawRectangularTree(branches);\n";
    }
    elsif ( $shape =~ /^d/i ) {
        $$api .= "drawDiagonalTree(branches);\n";
    }
    $$api .= '</script>';
    return $$api;
}

sub _draw_text {
    my $self = shift;
    my %args = @_;
    my ( $x, $y, $text, $url ) = @args{qw(-x -y -text -url)};
    my $api = $self->_api;
    $$api .= "drawText(ctx,$x,$y,'$text');\n";
}

sub _draw_circle {
    my $self = shift;
    my %args = @_;
    my (     $x, $y, $radius, $width, $stroke, $fill, $api, $url) =
    @args{qw(-x  -y  -radius  -width  -stroke  -fill  -api  -url)};
    if ( $radius ) {
	my $api = $self->_api;
        $$api .= "drawCircle(ctx,$x,$y,$radius);\n";

    }	
}

sub _draw_line {
    my $self = shift;
    my %args = @_;
    my @keys = qw(-x1  -y1  -x2  -y2  -width  -color );
    my (          $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
    my $api = $self->_api;
    $$api .= "drawLine(ctx,$x1,$y1,$x2,$y2);\n";
}

sub _draw_curve {
    my $self = shift;
    my %args = @_;
    my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
    my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
    my $api = $self->_api;
    $$api .= "drawCurve(ctx,$x1,$y1,$x2,$y2);\n";
}

sub _draw_multi {
    my $self = shift;
    my %args = @_;
    my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
    my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
    my $api = $self->_api;
    $$api .= "drawMulti(ctx,$x1,$y1,$x2,$y2);\n";

}

sub _draw_triangle {
    my $self = shift;
    my %args = @_;
    my @coord = qw(-x1 -y1 -x2 -y2 -x3 -y3);
    my (           $x1,$y1,$x2,$y2,$x3,$y3) = @args{@coord};
    my @optional = qw(-fill -stroke -width -url -api);
    my $fill   = $args{'-fill'}   || 'white';
    my $stroke = $args{'-stroke'} || 'black';
    my $width  = $args{'-width'}  || 1;
    my $api = $self->_api;
    $$api .= "drawTriangle(ctx,$x1,$y1,$x2,$y2,$x3,$y3);\n";
}

sub _draw_branch {
    my ( $self, $node ) = @_;
    $logger->info("Drawing branch for ".$node->get_internal_name);
    if ( my $parent = $node->get_parent ) {
	my ( $x1, $x2 ) = ( int $parent->get_x, int $node->get_x );
	my ( $y1, $y2 ) = ( int $parent->get_y, int $node->get_y );
	my $width = $self->_drawer->get_branch_width($node);
	my $shape = $self->_drawer->get_shape;
        my $drawer = '_draw_curve';
	if ( $shape =~ m/CURVY/i ) {
            $drawer = '_draw_curve';
	}
	elsif ( $shape =~ m/RECT/i ) {
            $drawer = '_draw_multi';
	}
	elsif ( $shape =~ m/DIAG/i ) {
            $drawer = '_draw_line';
	}
        my $api = $self->_api;
        $$api .= "branches.push({x1:$x1,y1:$y1,x2:$x2,y2:$y2});\n";
    }
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Treedrawer>

The canvas treedrawer is called by the L<Bio::Phylo::Treedrawer> object. Look
there to learn how to create tree drawings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;

__DATA__
<canvas id="%s" width="%s" height="%s">
    <p>Your browser doesn't support canvas.</p>
</canvas>
<script type="text/javascript">
function drawCurve( ctx, x1, y1, x2, y2 ) {
    ctx.beginPath();    
    ctx.moveTo( x1, y1 );
    ctx.bezierCurveTo( x1, (y1+y2)/2, (x1+x2)/2, y2, x2, y2 );
    ctx.stroke();    
}

function drawLine( ctx, x1, y1, x2, y2 ) {
    ctx.beginPath();
    ctx.moveTo( x1, y1 );    
    ctx.lineTo( x2, y2 );
    ctx.stroke();
}

function drawMulti( ctx, x1, y1, x2, y2 ) {
    ctx.beginPath();
    ctx.moveTo( x1, y1 );    
    ctx.lineTo( x1, y2 );
    ctx.lineTo( x2, y2 );
    ctx.stroke();    
}

function drawTriangle( ctx, x1, y1, x2, y2, x3, y3 ) {
    ctx.beginPath();
    ctx.moveTo( x1, y1 );
    ctx.lineTo( x2, y2 );
    ctx.lineTo( x3, y3 );
    ctx.lineTo( x1, y1 );
    ctx.fill();
}

function drawCircle( ctx, x, y, radius ) {
    ctx.beginPath();
    ctx.arc( x, y, radius, 0, Math.PI * 2, false );
    ctx.fill();
}

function drawText( ctx, x, y, text ) {
    ctx.fillText( text, x, y );
}

function drawCurvedTree (allBranches) {
    for ( var i = 0; i < allBranches.length; i++ ) {
        var branch = allBranches[i];
        drawCurve(ctx,branch.x1,branch.y1,branch.x2,branch.y2);
    }
}

function drawDiagonalTree (allBranches) {
    for ( var i = 0; i < allBranches.length; i++ ) {
        var branch = allBranches[i];
        drawLine(ctx,branch.x1,branch.y1,branch.x2,branch.y2);
    }
}

function drawRectangularTree (allBranches) {
    for ( var i = 0; i < allBranches.length; i++ ) {
        var branch = allBranches[i];
        drawMulti(ctx,branch.x1,branch.y1,branch.x2,branch.y2);
    }
}

var canvas = document.getElementById('%s');
var ctx = canvas.getContext('2d');
var branches = new Array();
