# $Id$
package xs::schema;
use strict;
use warnings;
use XML::Twig;
use xs::simpleType;
use xs::complexType;
use xs::attribute;
use xs::element;
use xs::sequence;
use xs::choice;
use File::Spec;
use Cwd;

my %imports = (
    'xml:base'   => 'xs:anyURI',
    'xml:lang'   => 'xs:string',
    'xml:id'     => 'xs:NCName',
    'xlink:href' => 'xs:anyURI',
);

sub new {
    my $class = shift;
    my $file  = shift;
    my $self  = {
        'files'           => {},
        'simpleType'      => {},
        'complexType'     => {},
        'documentation'   => {},        
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

sub inheritsFromType {
    my ( $self, $type ) = @_;
    if ( $type->isa('xs::complexType') ) {
        return grep { $_->base && $type->name eq $_->base } $self->complexType;
    }
    elsif ( $type->isa('xs::simpleType') ) {
        return grep { $_->base && $type->name eq $_->base } $self->simpleType;
    }
}

sub files {
    my $self = shift;
    return sort { $a cmp $b } keys %{ $self->{'files'} };
}

sub includesInFile {
    my ( $self, $file ) = @_;
    if ( not exists $self->{'files'}->{$file} ) {
        return ();
    }
    else {
        return @{ $self->{'files'}->{$file} };
    }
}

sub filesIncludingFile {
    my ( $self, $file ) = @_;    
    if ( not exists $self->{'files'}->{$file} ) {
        return ();
    }
    else {
        my @files;
        for my $includer ( $self->files ) {
            for my $includes ( $self->includesInFile( $includer ) ) {
                push @files, $includer if $includes eq $file;
            }
        }
        return @files;
    }    
}

sub _parseFile {
    my ( $self, $file ) = @_;
    if ( not exists $self->{'files'}->{$file} ) {
        $self->{'files'}->{$file} = [];
        $self->{'currentFile'} = $file;
        my $twig = XML::Twig->new(
            'twig_handlers' => $self->{'handlers'},
            'pretty_print'  => 'indented',
            'comments'      => 'drop',
        );
        $twig->parsefile( $file );        
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
    my $oldpath = Cwd::realpath( $twig->{'Base'} );
    $self->{'files'}->{$oldpath} = [] if not $self->{'files'}->{$oldpath};
    push @{ $self->{'files'}->{$oldpath} }, $newpath;
    $self->_parseFile( $newpath );
}

sub _schema {
    my ( $twig, $elt, $self ) = @_;
    $self->targetNamespace( $elt->att('targetNamespace') );
    if ( my $annotation = $elt->first_child('xs:annotation') ) {
        if ( my $documentation = $annotation->first_child('xs:documentation') ) {
            $documentation->set_tag('p');       
            $self->docsInFile( Cwd::realpath( $twig->{'Base'} ) => $documentation->sprint );
        }
    }
}

sub docsInFile {
    my ( $self, $file, $docs ) = @_; 
    if ( $docs ) {
        $self->{'documentation'}->{$file} = $docs;
    }
    return $self->{'documentation'}->{$file};
}

sub simpleType {
    my $self = shift;
    if ( 2 == @_ ) {
        my $name = shift;
        my $obj  = shift;
        $self->{'simpleType'}->{$name} = $obj;
    }
    elsif ( 1 == @_ ) {
        my $name = shift;
        return $self->{'simpleType'}->{$name};
    }
    else {
        return map { 
            $self->{'simpleType'}->{$_} 
        } sort { $a cmp $b } keys %{ $self->{'simpleType'} };
    }
}

sub simpleTypeInFile {
    my ( $self, $file ) = @_;
    return grep { $_->file eq $file } $self->simpleType;
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
    if ( 2 == @_ ) {
        my $name = shift;
        my $obj  = shift;
        $self->{'complexType'}->{$name} = $obj;
    }
    elsif ( 1 == @_ ) {
        my $name = shift;
        return $self->{'complexType'}->{$name};
    }
    else {
        return map { 
            $self->{'complexType'}->{$_} 
        } sort { $a cmp $b } keys %{ $self->{'complexType'} };
    }
}

sub complexTypeInFile {
    my ( $self, $file ) = @_;
    return grep { $_->file eq $file } $self->complexType;
}

sub _complexType {
    my ( $twig, $elt, $self ) = @_;
    my $ct = xs::complexType->new;
    $self->_anyType( $twig, $elt, $ct );
    my @attributes;
    for my $att ( $elt->descendants('xs:attribute') ) {
        my $attribute = xs::attribute->new;
        if ( my $name = $att->att('name') ) {
            $attribute->name( $name );
            $attribute->type( $att->att('type') );
            $attribute->use( $att->att('use') );
        }
        # included from external schema
        elsif ( my $ref = $att->att('ref') ) {
            $attribute->name( $ref );
            $attribute->type( $imports{$ref} );
            $attribute->use( $att->att('use') );            
        }
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
        $annotation->delete;
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
    $type->xml( $elt->sprint );
}

sub targetNamespace {
    my $self = shift;
    if ( @_ ) {
        $self->{'targetNamespace'} = shift;
    }
    return $self->{'targetNamespace'};
}

1;