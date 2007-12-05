#!/usr/bin/perl
use lib '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
use strict;
use warnings;
use CGI ':standard';
use XML::Twig;

####################################################################################################
package xs::anyThing;
sub new { return bless {}, shift }
sub file {
    my $self = shift;
    if ( @_ ) {
        $self->{'file'} = shift;
    }
    return $self->{'file'};
}

sub url {
    my $self = shift;
    my $name = $self->name;
    if ( $name =~ m/^xs:(.*)$/ ) {
        return 'http://www.w3.org/TR/xmlschema-2/#' . $1;
    }
    else {
        return $self->file . '#' . $name;
    }
}

sub name {
    my $self = shift;
    if ( @_ ) {
        $self->{'name'} = shift;
    }
    return $self->{'name'};
}

####################################################################################################
package xs::anyType;
our @ISA=qw(xs::anyThing);

sub abstract {
    my $self = shift;
    if ( @_ ) {
        $self->{'abstract'} = shift;
    }
    return $self->{'abstract'};
}

sub base {
    my $self = shift;
    if ( @_ ) {
        $self->{'base'} = shift;
    }
    return $self->{'base'};
}

sub inheritance {
    my $self = shift;
    if ( @_ ) {
        $self->{'inheritance'} = shift;
    }
    return $self->{'inheritance'};
}

sub documentation {
    my $self = shift;
    if ( @_ ) {
        $self->{'documentation'} = shift;
    }
    return $self->{'documentation'};
}

sub type { ref shift }

####################################################################################################
package xs::simpleType;
our @ISA=qw(xs::anyType);

sub explain { 'A simpleType is an atomic type such as a number or a string.' }

sub facets {
    my $self = shift;
    if ( @_ ) {
        $self->{'facets'} = \@_;
    }
    return $self->{'facets'} ? @{ $self->{'facets'} } : ();
}

####################################################################################################
package xs::complexType;
our @ISA=qw(xs::anyType);

sub explain { 'A complexType is an element with any attributes and child elements.' }

sub elementPatterns {
    my $self = shift;
    if ( @_ ) {
        $self->{'elementPatterns'} = \@_;
    }
    return $self->{'elementPatterns'} ? @{ $self->{'elementPatterns'} } : ();
}

sub attributes {
    my $self = shift;
    if ( @_ ) {
        $self->{'attributes'} = \@_;
    }
    return $self->{'attributes'} ? @{ $self->{'attributes'} } : ();
}

####################################################################################################
package xs::attribute;
our @ISA=qw(xs::anyThing);

sub explain { 'An attribute is a key/value pair such as id="MyID" inside the pointy bits of xml.' }

sub xs::attribute::use {
    my $self = shift;
    if ( @_ ) {
        $self->{'use'} = shift;
    }
    return $self->{'use'};
}

sub type {
    my $self = shift;
    if ( @_ ) {
        $self->{'type'} = shift;
    }
    return $self->{'type'};
}

####################################################################################################
package xs::elementPattern;
sub new { return bless {}, shift }
sub minOccurs {
    my $self = shift;
    if ( @_ ) {
        $self->{'minOccurs'} = shift;
    }
    return $self->{'minOccurs'};
}

sub maxOccurs {
    my $self = shift;
    if ( @_ ) {
        $self->{'maxOccurs'} = shift;
    }
    return $self->{'maxOccurs'};
}
####################################################################################################
package xs::recursiveElementPattern;
our @ISA=qw(xs::elementPattern);
sub elementPatterns {
    my $self = shift;
    if ( @_ ) {
        $self->{'elementPatterns'} = \@_;
    }
    return $self->{'elementPatterns'} ? @{ $self->{'elementPatterns'} } : ();
}
####################################################################################################
package xs::element;
our @ISA=qw(xs::elementPattern);
sub explain { 'An element is one of those pointy things.' }
sub type {
    my $self = shift;
    if ( @_ ) {
        $self->{'type'} = shift;
    }
    return $self->{'type'};
}
sub name {
    my $self = shift;
    if ( @_ ) {
        $self->{'name'} = shift;
    }
    return $self->{'name'};
}
####################################################################################################
package xs::choice;
our @ISA=qw(xs::recursiveElementPattern);
sub explain { 'A choice means that out of the items in the list only one may appear.' }
####################################################################################################
package xs::sequence;
our @ISA=qw(xs::recursiveElementPattern);
sub explain { 'A choice means that out of the items in the list multiple may appear.' }
####################################################################################################
package xs::schema;
use Cwd;
use Data::Dumper;
sub new {
    my $class = shift;
    my $file  = shift;
    my $self  = {
        'files'           => {},
        'simpleType'      => {},
        'complexType'     => {},
        'targetNamespace' => undef,
        'handlers'        => undef,
        'currentFile'     => undef,
    };
    bless $self, $class;
    $self->{'handlers'} = {
        'xs:simpleType'  => sub { _simpleType( @_, $self ) },
        'xs:complexType' => sub { _complexType( @_, $self ) },
        'xs:schema'      => sub { _schema( @_, $self ) },
        'xs:include'     => sub { _include( @_, $self ) },
    };
    $self->_parseFile( Cwd::realpath( $file ) );
    return $self;
}

sub _parseFile {
    my ( $self, $file ) = @_;
    if ( not exists $self->{'files'}->{$file} ) {
        $self->{'currentFile'} = $file;
        my $twig = XML::Twig->new(
            'twig_handlers' => $self->{'handlers'},
        );
        $twig->parsefile( $file );
        $self->{'files'}->{$file} = 1;
    }
}

sub _include {
    my ( $twig, $elt, $self ) = @_;
    my ( $volume, $directories, $file ) = File::Spec->splitpath( $twig->{'Base'} );
    my ( $newvol, $newdirs, $newfile  ) = File::Spec->splitpath( $elt->att('schemaLocation') );
    my $newpath = Cwd::realpath(       # collapse foo/../bar patterns in the following:
        File::Spec->canonpath(              # make a clean path from the following:
            File::Spec->catfile(                 # concatenate the following fragments:
                File::Spec->splitdir( $directories ), # directories to file making the include
                File::Spec->splitdir( $newdirs ),     # relative path from file making include to called file
                $newfile                              # the called file
            )
        )
    );
    $self->_parseFile( $newpath );
}

sub _schema {
    my ( $twig, $elt, $self ) = @_;
    $self->targetNamespace( $elt->att('targetNamespace') );
}

sub simpleType {
    my $self = shift;
    if ( @_ ) {
        my $name = shift;
        my $obj  = shift;
        $self->{'simpleType'}->{$name} = $obj;
    }
    return $self->{'simpleType'};
}

sub _simpleType {
    my ( $twig, $elt, $self ) = @_;
    my $st = xs::simpleType->new;
    $self->_anyType( $twig, $elt, $st );
    if ( my $list = $elt->first_child( 'xs:list' ) ) {
        $st->inheritance( 'list' );
        $st->base( $list->att('itemType') );
    }
    elsif ( my $restriction = $elt->first_child( 'xs:restriction' ) ) {
        my %facets;
        for my $facet ( $restriction->children ) {
            my $name = $facet->tag;
            $name =~ s/^xs://;
            $facets{$name} = $facet->att('value');
        }
        $st->facets( %facets );
    }
    $self->simpleType( $st->name => $st );
}

sub complexType {
    my $self = shift;
    if ( @_ ) {
        my $name = shift;
        my $obj  = shift;
        $self->{'complexType'}->{$name} = $obj;
    }
    return $self->{'complexType'};
}

sub _complexType {
    my ( $twig, $elt, $self ) = @_;
    my $ct = xs::complexType->new;
    $self->_anyType( $twig, $elt, $ct );
    my @attributes;
    for my $att ( $elt->descendants('xs:attribute') ) {
        my $attribute = xs::attribute->new;
        $attribute->name( $att->att('name') );
        $attribute->type( $att->att('type') );
        $attribute->use( $att->att('use') );
        push @attributes, $attribute;
    }
    $ct->attributes( @attributes );
    if ( my $complexContent = $elt->first_child('xs:complexContent') ) {
        if ( my $restriction = $complexContent->first_child('xs:restriction') ) {
            $self->_elementPattern( $twig, $restriction, $ct );
        }
        elsif ( my $extension = $complexContent->first_child('xs:extension') ) {
            $self->_elementPattern( $twig, $extension, $ct );
        }        
    }
    $self->complexType( $ct->name => $ct );
}

sub _elementPattern {
    my ( $self, $twig, $elt, $ct ) = @_;
    my @elementPatterns;
    for my $child ( $elt->children ) {
        my $elementPattern;
        for my $tag ( qw(xs:element xs:sequence xs:choice) ) {
            if ( $child->tag eq $tag ) {
                my $class = $tag;
                $class =~ s/:/::/;
                $elementPattern = $class->new;
            }
        }
        next if not defined $elementPattern;
        for my $attr ( qw(minOccurs maxOccurs name type) ) {
            if ( defined( my $value = $child->att($attr) ) ) {
                $elementPattern->$attr($value);
            }
        }
        if ( $elementPattern->isa('xs::recursiveElementPattern') ) {
            $self->_elementPattern( $twig, $child, $elementPattern );
        }
        push @elementPatterns, $elementPattern;
    }
    $ct->elementPatterns( @elementPatterns );
}

sub _anyType {
    my ( $self, $twig, $elt, $type ) = @_;
    my $name = $elt->att('name');
    $type->name( $name );
    my $doc = '';
    for my $annotation ( $elt->children('xs:annotation') ) {
        for my $documentation ( $annotation->children('xs:documentation') ) {
            $doc .= $documentation->text;
        }
    } 
    $type->documentation( $doc );    
    $type->file( Cwd::realpath( $twig->{'Base'} ) );
    $type->abstract( $elt->att('abstract') || 'false' ); 
    my $inheritance;
    if ( my @res = $elt->descendants('xs:restriction') ) {
        $type->inheritance('restriction');
        $inheritance = $res[0];
    }    
    if ( my @res = $elt->descendants('xs:extension') ) {
        $type->inheritance('extension');
        $inheritance = $res[0];
    }
    if ( $inheritance ) {
        $type->base( $inheritance->att('base') );
    }
}

sub targetNamespace {
    my $self = shift;
    if ( @_ ) {
        $self->{'targetNamespace'} = shift;
    }
    return $self->{'targetNamespace'};
}

package main;
use Data::Dumper;
my $schema = xs::schema->new( $ARGV[0] );
print '<pre>', Dumper( $schema ), '</pre>';