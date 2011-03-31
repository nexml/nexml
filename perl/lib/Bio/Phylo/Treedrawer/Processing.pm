package Bio::Phylo::Treedrawer::Processing;
use strict;
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Treedrawer::Abstract;
use vars '@ISA';
@ISA=qw(Bio::Phylo::Treedrawer::Abstract);

=head1 NAME

Bio::Phylo::Treedrawer::Processing - Graphics format writer used by treedrawer,
no serviceable parts inside

=head1 DESCRIPTION

This module creates a Processing graphic from a Bio::Phylo::Forest::DrawTree
object. It is called by the L<Bio::Phylo::Treedrawer> object, so look there to
learn how to create tree drawings.

=cut

my $logger = Bio::Phylo::Util::Logger->new;
my $black = 0;
my $white = 255;

sub _new {
    my $class = shift;
    my %args = @_;
    my $tmpl = <<'TEMPLATE';

ArrayList coordinates = new ArrayList();

void mouseClicked() {
    println("mouseX = " + mouseX + " mouseY = " + mouseY);
    for (int i = coordinates.size()-1; i >= 0; i--) {
        HashMap co = (HashMap) coordinates.get(i);
        int minX = (Integer) co.get("minX");
        int maxX = (Integer) co.get("maxX");
        int minY = (Integer) co.get("minY");
        int maxY = (Integer) co.get("maxY");
        if ( mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY ) {
            link((String)co.get("url"));
        }
    }  
}

void drawText(String textString, int x, int y, int textColor) {
    fill(textColor);
    text(textString,x,y);
    noFill();    
}

void drawLine(int x1, int y1, int x2, int y2, int lineColor, int lineWidth) {
    stroke(lineColor);
    strokeWeight(lineWidth);    
    line(x1,y1,x2,y2);
    strokeWeight(1);
    noStroke();
}    

void drawMulti(int x1, int y1, int x2, int y2, int lineColor, int lineWidth) {
    drawLine(x1,y1,x1,y2,lineColor,lineWidth);
    drawLine(x1,y2,x2,y2,lineColor,lineWidth);
}

void drawCircle(int x, int y, int radius, int lineColor, int lineWidth, int fillColor, String url) {
    fill(fillColor);
    stroke(lineColor);
    strokeWeight(lineWidth);
    ellipse(x, y, radius, radius);
    strokeWeight(1);
    noStroke();
    noFill();
    if ( url != null ) {
        HashMap coordinate = new HashMap();
        coordinate.put("url",url);
        coordinate.put("minX",x-radius);
        coordinate.put("maxX",x+radius);
        coordinate.put("minY",y-radius);
        coordinate.put("maxY",y+radius);
        coordinates.add(coordinate);
        println(coordinate);
    }
}

void drawCurve(float x1, float y1, float x3, float y3, int lineColor, int lineWidth) {
    stroke(lineColor);
    strokeWeight(lineWidth);
    noFill();
    float ellipseWidth = abs(x1-x3) * 2;
    float ellipseHeight = abs(y1-y3) * 2;
    float start;
    float stop;
    if ( y1 < y3 ) {
        start = PI / 2;
        stop = PI;
    }
    else {
        start = PI;
        stop = TWO_PI - PI / 2;
    }
    arc(x3,y1,ellipseWidth,ellipseHeight,start,stop);    
    strokeWeight(1);
    noStroke();
}

void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int lineColor, int lineWidth, int fillColor) {
    fill(fillColor);
    stroke(lineColor);
    strokeWeight(lineWidth);
    triangle(x1, y1, x2, y2, x3, y3);
    strokeWeight(1);
    noStroke();
    noFill();    
}

void setup() {
    size(%s, %s);
    background(%s);
    smooth();
TEMPLATE
    my $canvas = sprintf(
        $tmpl,
        $args{'-drawer'}->get_width,
        $args{'-drawer'}->get_height,
        $white
    );
    my $self = $class->SUPER::_new(%args,'-api'=>\$canvas);    
    return bless $self, $class;
}

sub _draw_pies {
    my $self = shift;
    $logger->warn(ref($self) . " can't draw pies");
}

sub _draw_legend {
    my $self = shift;
    $logger->warn(ref($self) . " can't draw a legend");    
}

sub _finish {
    $logger->debug("finishing");
    my $self = shift;
    my $api = $self->_api;
    return $$api . "\n}\n\nvoid draw() {}\n\n";
}

sub _draw_text {
    $logger->debug("drawing text @_");
    my $self = shift;
    my %args = @_;
    my ( $x, $y, $text, $url, $stroke ) = @args{qw(-x -y -text -url -color)};
    $stroke = $black if not defined $stroke;
    my $api = $self->_api;
    $$api .= "    drawText(\"$text\",$x,$y,$stroke);\n"
}

sub _draw_line {
    $logger->debug("drawing line @_");
    my $self = shift;
    my %args = @_;
    my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
    my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
    $color = $black if not defined $color;
    $width = 1 if not defined $width;
    my $api = $self->_api;
    $$api .= "    drawLine($x1,$y1,$x2,$y2,$color,$width);\n";
}

sub _draw_curve {
    $logger->debug("drawing curve @_");
    my $self = shift;
    my $api = $self->_api;
    my %args = @_;
    my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
    my ( $x1, $y1, $x3, $y3, $width, $color ) = @args{@keys};
    $x1 = sprintf("%.3f", $x1);
    $x3 = sprintf("%.3f", $x3);
    $y1 = sprintf("%.3f", $y1);
    $y3 = sprintf("%.3f", $y3);
    $color = $black if not defined $color;
    $width = 1 if not defined $width;
    $$api .= "    drawCurve($x1,$y1,$x3,$y3,$color,$width);\n";
}

sub _draw_multi {
    $logger->debug("drawing multi @_");
    my $self = shift;
    my $api = $self->_api;    
    my %args = @_;
    my @keys = qw(-x1 -y1 -x2 -y2 -width -color);
    my ( $x1, $y1, $x2, $y2, $width, $color ) = @args{@keys};
    $color = $black if not defined $color;
    $width = 1 if not defined $width;    
    $$api .= sprintf("    drawMulti(%u,%u,%u,%u,%u,%u);\n", $x1,$y1,$x2,$y2,$color,$width);
}

sub _draw_triangle {
    $logger->debug("drawing multi @_");
    my $self = shift;
    my $api = $self->_api;    
    my %args = @_;
    my @coord = qw(-x1 -y1 -x2 -y2 -x3 -y3);
    my (           $x1,$y1,$x2,$y2,$x3,$y3) = @args{@coord};
    my @optional = qw(-fill -stroke -width -url -api);
    my $fill   = $args{'-fill'}   || $white;
    my $stroke = $args{'-stroke'} || $black;
    my $width  = $args{'-width'}  || 1;
    my $url    = $args{'-url'};
    $$api .= "    drawTriangle($x1,$y1,$x2,$y2,$x3,$y3,$stroke,$width,$fill);\n";

}

sub _draw_circle {
    $logger->debug("drawing circle @_");
    my $self = shift;
    my $api = $self->_api;
    my %args = @_;
    my (     $x, $y, $radius, $width, $stroke, $fill, $url) =
    @args{qw(-x  -y  -radius  -width  -stroke  -fill  -url)};
    $stroke = $black if not defined $stroke;
    $width = 1 if not defined $width;
    $fill = $white if not defined $fill;
    $$api .= sprintf("    drawCircle(%u,%u,%u,%u,%u,%u,\"%s\");\n",$x,$y,$radius,$stroke,$width,$fill,$url);
}

=head1 SEE ALSO

=over

=item L<http://processing.org>

This treedrawer produces a tree description in Processing language syntax. Visit
the website to learn more about how to deploy such graphics.

=item L<Bio::Phylo::Treedrawer>

The processing treedrawer is called by the L<Bio::Phylo::Treedrawer> object.
Look there to learn how to create tree drawings.

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

 $Id: Svg.pm 1593 2011-02-27 15:26:04Z rvos $

=cut

1;
