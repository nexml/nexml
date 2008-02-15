# $Id: Nexus.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Parsers::Nexus;
use strict;
use Bio::Phylo::Factory;
use Bio::Phylo::IO qw(parse);
use Bio::Phylo::Util::CONSTANT qw(:objecttypes);
use Bio::Phylo::Util::Exceptions qw(throw);
use UNIVERSAL qw(isa);
use IO::String;
use vars qw(@ISA);

# TODO: handle mixed? distances, splits, bipartitions

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::IO);

# create logger
my $logger = Bio::Phylo->get_logger;

# create factory
my $factory = Bio::Phylo::Factory->new;

my $TAXA = _TAXA_;

=head1 NAME

Bio::Phylo::Parsers::Nexus - Parses nexus files. No serviceable parts inside.

=head1 DESCRIPTION

This module parses nexus files. It is called by the L<Bio::Phylo::IO> module,
there is no direct usage. The parser can handle files and strings with multiple
tree, taxon, and characters blocks whose links are defined using Mesquite's
"TITLE = 'some_name'" and "LINK TAXA = 'some_name'" tokens.

The parser returns a reference to an array containing one or more taxa, trees
and matrices objects. Nexus comments are stripped, private nexus blocks (and the
'assumptions' block) are skipped. It currently doesn't handle 'mixed' data.

=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $nexus = Bio::Phylo::Parsers::Nexus->_new;
 Function: Initializes a Bio::Phylo::Parsers::Nexus object.
 Returns : A Bio::Phylo::Parsers::Nexus object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;

    # this is a dispatch table whose sub references are invoked
    # during parsing. the keys match the tokens upon which the
    # respective subs are called. Underscored (private) fields are for parsing
    # context.
    my $self = {
        '_current'         => undef,
        '_previous'        => undef,
        '_begin'           => undef,
        '_ntax'            => undef,
        '_nchar'           => undef,
        '_gap'             => undef,
        '_missing'         => undef,
        '_i'               => undef,
        '_tree'            => undef,
        '_trees'           => undef,
        '_treename'        => undef,
        '_treestart'       => undef,
        '_row'             => undef,
        '_matrixtype'      => undef,
        '_found'           => 0,
        '_linemode'        => 0,
        '_tokens'          => [],
        '_context'         => [],
        '_translate'       => [],
        '_symbols'         => [],
        '_charlabels'      => [],
        '_comments'        => [],
        '_treenames'       => [],
        '_matrix'          => {},
        'begin'            => \&_begin,
        'taxa'             => \&_taxa,
        'title'            => \&_title,
        'dimensions'       => \&_dimensions,
        'ntax'             => \&_ntax,
        'taxlabels'        => \&_taxlabels,
        'data'             => \&_data,
        'characters'       => \&_characters,
        'nchar'            => \&_nchar,
        'format'           => \&_format,
        'datatype'         => \&_datatype,
        'gap'              => \&_gap,
        'missing'          => \&_missing,
        'charlabels'       => \&_charlabels,
        'symbols'          => \&_symbols,
        'items'            => \&_items,
        'matrix'           => \&_matrix,
        'trees'            => \&_trees,
        'translate'        => \&_translate,
        'tree'             => \&_tree,
        'utree'            => \&_tree,
        'end'              => \&_end,
        'endblock'         => \&_end,        
        '#nexus'           => \&_nexus,
        'link'             => \&_link,
        ';'                => \&_semicolon,
    };
    if ( ref $class ) {
        %$class = %$self;
    }
    bless $self, __PACKAGE__;
    return $self;
}

=begin comment

 Type    : Wrapper
 Title   : _from_handle(\*FH)
 Usage   : $nexus->_from_handle(\*FH);
 Function: Does all the parser magic, from a file handle
 Returns : ARRAY
 Args    : \*FH = file handle

=end comment

=cut

# trickery to get it to parse strings as well, uses IO::String
*_from_string = \&_from_handle;

sub _from_handle {
    my $self = shift;
    $logger->info( "going to parse nexus data" );
    $self->{'_lines' } = $self->_stringify( @_ );
    $self->{'_tokens'} = $self->_tokenize( $self->{'_lines'} );

    # iterate over tokens, dispatch methods from %{ $self } table
    # This is the meat of the parsing, from here everything else is called.
    $logger->info( "tokenized and split data, going to parse blocks" );
    my $i = 0;
    my $private_block;
    my $token_queue = [ undef, undef, undef ];
    no strict 'refs';
    TOKEN_LINE: for my $token_line ( @{ $self->{'_tokens'} } ) {
        if ( not $self->{'_linemode'} ) {
            RAW_TOKEN: for my $raw_token ( @{ $token_line } ) {
                if ( $raw_token =~ qr/^\[/ ) {
                    push @{ $self->{'_comments'} }, $raw_token;
                    next RAW_TOKEN;
                }
                my $lower_case_token = lc( $raw_token );
                push @$token_queue, $lower_case_token;
                shift @$token_queue;
                if ( exists $self->{$lower_case_token} and not $private_block ) {
                    if ( ref $self->{$lower_case_token} eq 'CODE' ) {
                        $self->{'_previous'} = $self->{'_current'};
                        $self->{'_current'}  = $lower_case_token;

                        # pull code ref from dispatch table
                        my $c = $self->{$lower_case_token};

                        # invoke as object method
                        $self->$c($raw_token);
                        next RAW_TOKEN;
                    }
                }
                elsif ( $self->{'_current'} and not $private_block ) {
                    my $c = $self->{ $self->{'_current'} };
                    $self->$c($raw_token);
                    next RAW_TOKEN;
                }

                # $self->{'_begin'} is switched 'on' by &_begin(), and 'off'
                # again by any one of the appropriate subsequent tokens, i.e.
                # taxa, data, characters and trees
                if ( $self->{'_begin'} and not exists $self->{$lower_case_token} and not $private_block ) {
                    $private_block = $raw_token;
                    next RAW_TOKEN;
                }

                # jump over private block content
                if ( $private_block and $token_queue->[-2] eq 'end' and $token_queue->[-1] eq ';' ) {
                    $private_block = 0;
                    $logger->info( "Skipped private $private_block block" );
                    next RAW_TOKEN;
                }
                else {
                    next RAW_TOKEN;
                }
            }
        }
        elsif ( $self->{'_linemode'} ) {
            my $c = $self->{ $self->{'_current'} };
            push @{ $token_queue }, $token_line;
            shift @$token_queue;
            $self->$c($token_line);
            next TOKEN_LINE;
        }
    }

    return $self->_post_process;
}

# makes array reference of strings, one string per line, from input
# file handle or string;
sub _stringify {
    my $self = shift;
    $logger->info( "going to split nexus data on lines" );
    my %opts = @_;
    my @lines;
    if ( $opts{'-string'} ) {
        $opts{'-handle'} = IO::String->new( $opts{'-string'} );
        $logger->info( "nexus data was a string, faking handle access" );
    }
    while ( my $line = readline( $opts{'-handle'} ) ) {
        push @lines, $line;
        chomp( $line );
        $logger->debug( "read line: $line" );
    }
    return \@lines;
}

=begin comment

 Type    : Method
 Title   : _tokenize()
 Usage   : $nexus->_tokenize($lines);
 Function: Tokenizes lines in $lines array ref
 Returns : Two dimensional ARRAY
 Args    : An array ref of lines (e.g. read from an input file);
 Comments: This method accepts an array ref holding lines that may contain
           single quotes, double quotes or square brackets. Line breaks and
           spaces inside these quoted/bracketed fragments are ignored, otherwise
           it is split, e.g.:

           [
               [ '#NEXUS' ],
               [ 'BEGIN TAXA; [taxablock comment]' ],
               [ 'DIMENSIONS NTAX=3;' ],
               [ 'TAXLABELS "Taxon \' A" \'Taxon B\' TAXON[comment]C' ],
               ...etc...
           ]

           becomes:
           [
               [ '#NEXUS' ],
               [
                   'BEGIN',
                   'TAXA',
                   ';',
                   '[taxablock comment]'
               ],
               [
                   'DIMENSIONS',
                   'NTAX',
                   '=',
                   '3',
                   ';'
               ],
               [
                   'TAXLABELS',
                   '"Taxon \' A"',
                   '\'Taxon B\'',
                   'TAXON',
                   '[comment]',
                   'C'
               ],
               ...etc...
           ]


=end comment

=cut

sub _tokenize {
    my ( $self, $lines ) = @_;
    $logger->info( "going to split lines on tokens" );
    my ( $extract, $INSIDE_QUOTE, $continue ) = ( '', 0, 0 );
    my ( @tokens, @split );

    my $QUOTES_OR_BRACKETS       = qr/[\[\]'"]/mox;
    my $OPENING_QUOTE_OR_BRACKET = qr/^(.*?)([\['"].*)$/mox;
    my $CLOSING_BRACKET_MIDLINE  = qr/^.*?(\])(.*)$/mox;
    my $CONTEXT_QB_AT_START      = qr/^([\['"])(.*)$/mox;

    my $CONTEXT_CLOSER;
    my $QuoteContext; # either " ' or [
    my $QuoteStartLine;
    my $LineCount = 0;
    my %CLOSE_CHAR = (
        '"' => '"',
        "'" => "'",
        '[' => ']',
    );
    my %INVERSE_CLOSE_CHAR = (
        '"' => '"',
        "'" => "'",
        ']' => '[',
        ')' => '(',
    );

    # tokenize
    LINE: for my $line ( @{ $lines } ) {
        $LineCount++;
        TOKEN: while ( $line =~ /\S/ ) {

            # line in file has no quoting/bracketing characters, and
            # is no extension of a quoted/bracketed fragment starting
            # on a previous line
            if ( $line !~ $QUOTES_OR_BRACKETS && ! $INSIDE_QUOTE ) {
                if ( $continue ) {
                    push @{ $tokens[-1] }, $line;
                    $continue = 0;
                }
                else {
                    push @tokens, [$line];
                }
                my $logline = join( ' ', @{ $tokens[-1] } );
                chomp( $logline );
                $logger->debug( "Tokenized line $LineCount: $logline" );
                next LINE;
            }

            # line in file has opening quoting/bracketing characters, and
            # is no extension of a quoted/bracketed fragment starting
            # on a previous line
            elsif ( $line =~ $OPENING_QUOTE_OR_BRACKET && ! $INSIDE_QUOTE ) {
                my ( $start, $quoted ) = ( $1, $2 );
                push @tokens, [$start];
                $line = $quoted;
                $extract = $quoted;
                $INSIDE_QUOTE++;
                $continue = 1;
                $QuoteContext = substr($quoted,0,1);
                $logger->debug( "Line $LineCount contains $QuoteContext" );
                $QuoteStartLine = $LineCount;
                $CONTEXT_QB_AT_START = qr/^(\Q$QuoteContext\E)(.*)$/;
                my $context_closer = $CLOSE_CHAR{$QuoteContext};
                $CONTEXT_CLOSER = qr/^(.*?)(\Q$context_closer\E)(.*)$/;
                next TOKEN;
            }

            # line in file has no quoting/bracketing characters, and
            # is an extension of a quoted/bracketed fragment starting
            # on a previous line
            elsif ( $line !~ $CONTEXT_CLOSER && $INSIDE_QUOTE ) {
                $logger->debug( "Line $LineCount extends quote or comment" );
                $extract .= $line;
                next LINE;
            }
            elsif ( $line =~ $CONTEXT_QB_AT_START && $INSIDE_QUOTE ) {
                my ( $q, $remainder ) = ( $1, $1 . $2 );              
                if ( $q eq '"' || $q eq "'" ) {
                    if ( $remainder =~ m/^($q[^$q]*?$q)(.*)$/ ) {
                        $logger->debug( "Line $LineCount closes $INVERSE_CLOSE_CHAR{$q} with $q" );
                        push @{ $tokens[-1] }, ( $1 );
                        $line = $2;
                        $INSIDE_QUOTE--;
                        next TOKEN;
                    }
                    elsif ( $remainder =~ m/^$q[^$q]*$/ ) {
                        $extract .= $line;
                        $continue = 1;
                        next LINE;
                    }
                }
                elsif ( $q eq '[' ) {
                    for my $i ( 1 .. length($line) ) {
                        $INSIDE_QUOTE++ if substr($line,$i,1) eq '[';
                        if ( $i and ! $INSIDE_QUOTE ) {
                            push @{ $tokens[-1] }, substr($line,0,$i);
                            my $logqc = substr($line,($i-1),1);
                            $logger->debug( "Line $LineCount closes $INVERSE_CLOSE_CHAR{$logqc} with $logqc" );
                            $line = substr($line,$i);														                            
                            next TOKEN;
                        }
                        $INSIDE_QUOTE-- if substr($line,$i,1) eq ']';
                    }
                    $extract = $line;
                    $continue = 1;
                    next LINE;
                }
            }
            elsif ( $line =~ $CONTEXT_CLOSER && $INSIDE_QUOTE ) {
                my ( $start, $q, $remainder ) = ( $1, $2, $3 );
                $logger->debug( "Line $LineCount closes $INVERSE_CLOSE_CHAR{$q} with $q" );
                $start = $extract . $start if $continue;
                if ( $q eq '"' or $q eq "'" ) {
                    push @{ $tokens[-1] }, $start;
                    $line = $remainder;
                    next TOKEN;
                }
                elsif ( $q eq ']' ) {
                    for my $i ( 0 .. length($line) ) {
                        $INSIDE_QUOTE++ if substr($line,$i,1) eq '[';
                        if ( $i and ! $INSIDE_QUOTE ) {
                            my $segment = substr($line,0,$i);
                            if ( $continue ) {
                                push @{ $tokens[-1] }, $extract . $segment;
                            }
                            else {
                                push @{ $tokens[-1] }, $segment;
                            }
                            $line = substr($line,$i);
                            next TOKEN;
                        }
                        $INSIDE_QUOTE-- if substr($line,$i,1) eq ']';
                    }
                    if ( $continue ) {
                        $extract .= $line;
                    }
                    else {
                        $extract = $line;
                    }
                    $continue = 1;
                    next LINE;
                }
            }
        }
    }

    # an exception here means that an opening quote symbol " ' [
    # ($QuoteContext) was encountered at input file/string line $QuoteStartLine.
    # This can happen if any of these symbols is used in an illegal
    # way, e.g. by using double quotes as gap symbols in matrices.
    if ( $INSIDE_QUOTE ) {
    	throw 'BadArgs' => "Unbalanced $QuoteContext starting at line $QuoteStartLine";
    }

    # final split: non-quoted/bracketed fragments are split on whitespace,
    # others are preserved verbatim
    $logger->info( "going to split non-quoted/commented fragments on whitespace" );
    foreach my $line (@tokens) {
        my @line;
        foreach my $word (@$line) {
            if ( $word !~ $QUOTES_OR_BRACKETS ) {
                $word =~ s/(=|;|,)/ $1 /g;
                push @line, grep { /\S/ } split /\s+/, $word;
            }
            else {
                push @line, $word;
            }
        }
        push @split, \@line;
    }
    return \@split;
}

# link matrices and forests to taxa
sub _post_process {
    my $self = shift;
    my $taxa = [];
    foreach my $block ( @{ $self->{'_context'} } ) {
        if ( $block->_type == $TAXA ) {
            push @{$taxa}, $block;
        }
        elsif ( $block->_type != $TAXA and $block->can('set_taxa') ) {
            if ( $taxa->[-1] and $taxa->[-1]->can('_type') == $TAXA and not $block->get_taxa ) {
                $block->set_taxa( $taxa->[-1] );    # XXX exception here?
            }
        }
    }
    my $blocks = $self->{'_context'};
    
    @{ $taxa } = ();
    $self->{'_current'}         = undef;
    $self->{'_previous'}        = undef;
    $self->{'_begin'}           = undef;
    $self->{'_ntax'}            = undef;
    $self->{'_nchar'}           = undef;
    $self->{'_gap'}             = undef;
    $self->{'_missing'}         = undef;
    $self->{'_i'}               = undef;
    $self->{'_tree'}            = undef;
    $self->{'_trees'}           = undef;
    $self->{'_treename'}        = undef;
    $self->{'_treestart'}       = undef;
    $self->{'_row'}             = undef;
    $self->{'_matrixtype'}      = undef;
    $self->{'_found'}           = 0;
    $self->{'_linemode'}        = 0;
    $self->{'_tokens'}          = [];
    $self->{'_context'}         = [];
    $self->{'_translate'}       = [];
    $self->{'_symbols'}         = [];
    $self->{'_charlabels'}      = [];
    $self->{'_comments'}        = [];
    $self->{'_treenames'}       = [];
    $self->{'_matrix'}          = {};
    $self->{'begin'}            = \&_begin;
    $self->{'taxa'}             = \&_taxa;
    $self->{'title'}            = \&_title;
    $self->{'dimensions'}       = \&_dimensions;
    $self->{'ntax'}             = \&_ntax;
    $self->{'taxlabels'}        = \&_taxlabels;
    $self->{'data'}             = \&_data;
    $self->{'characters'}       = \&_characters;
    $self->{'nchar'}            = \&_nchar;
    $self->{'format'}           = \&_format;
    $self->{'datatype'}         = \&_datatype;
    $self->{'gap'}              = \&_gap;
    $self->{'missing'}          = \&_missing;
    $self->{'charlabels'}       = \&_charlabels;
    $self->{'symbols'}          = \&_symbols;
    $self->{'items'}            = \&_items;
    $self->{'matrix'}           = \&_matrix;
    $self->{'trees'}            = \&_trees;
    $self->{'translate'}        = \&_translate;
    $self->{'tree'}             = \&_tree;
    $self->{'utree'}            = \&_tree;
    $self->{'end'}              = \&_end;
    $self->{'#nexus'}           = \&_nexus;
    $self->{'link'}             = \&_link;
    $self->{';'}                = \&_semicolon;
    
    return $blocks;
}

=begin comment

The following subs are called by the dispatch table stored in the object when
their respective tokens are encountered.

=end comment

=cut

sub _nexus {
    my $self = shift;
    if ( uc( $_[0] ) eq '#NEXUS' ) {
    	$logger->info( "found nexus token" );
    }
}

sub _begin {
    my $self = shift;
    $self->{'_begin'} = 1;
}

sub _taxa {
    my $self = shift;
    if ( $self->{'_begin'} ) {
        my $taxa = $factory->create_taxa;
        push @{ $self->{'_context'} }, $taxa;
        $logger->info( "starting taxa block" );
        $self->{'_begin'} = 0;
    }
    else {
        $self->{'_current'} = 'link';  # because of 'link taxa = blah' construct
    }
}

sub _title {
    my $self  = shift;
    my $token = shift;
    if ( defined $token and uc($token) ne 'TITLE' ) {
        my $title = $token;
        if ( not $self->_current->get_name ) {
            $self->_current->set_name($title);
            $logger->info( "block has title '$title'" );
        }
    }
}

sub _link {
    my $self  = shift;
    my $token = shift;
    if ( defined $token and $token !~ m/^(?:LINK|TAXA|=)$/i ) {
        my $link = $token;
        if ( not $self->_current->get_taxa ) {
            foreach my $block ( @{ $self->{'_context'} } ) {
                if ( $block->get_name and $block->get_name eq $link ) {
                    $self->_current->set_taxa($block);
                    last;
                }
            }
            $logger->info( "block links to taxa block with title '$link'" );
        }
    }
}

sub _dimensions {
    #my $self = shift;
}

sub _ntax {
    my $self = shift;
    if ( defined $_[0] and $_[0] =~ m/^\d+$/ ) {
        $self->{'_ntax'} = shift;
        my $ntax = $self->{'_ntax'};
        $logger->info( "number of taxa: $ntax" );
    }
}

sub _taxlabels {
    my $self = shift;
    if ( defined $_[0] and uc( $_[0] ) ne 'TAXLABELS' ) {
        my $taxon = shift;
        $logger->debug( "taxon: $taxon" );
        push @{ $self->{'_taxlabels'} }, $taxon;
    }
    elsif ( defined $_[0] and uc( $_[0] ) eq 'TAXLABELS' ) {
        $self->_current->set_generic(
            'nexus_comments' => $self->{'_comments'}
        );
        $self->{'_comments'} = [];
        $logger->info( "starting taxlabels" );
    }
}

sub _data {
    my $self = shift;
    if ( $self->{'_begin'} ) {
        $self->{'_begin'} = 0;
        $logger->info( "starting data block" );
    }
}

sub _characters {
    my $self = shift;
    if ( $self->{'_begin'} ) {
        $self->{'_begin'} = 0;
        $logger->info( "starting characters block" );
    }
}

sub _nchar {
    my $self = shift;
    if ( defined $_[0] and $_[0] =~ m/^\d+$/ ) {
        $self->{'_nchar'} = shift;
        my $nchar = $self->{'_nchar'};
        $logger->info( "number of characters: $nchar" );
    }
}

sub _format {
    #my $self = shift;
}

sub _datatype {
    my $self = shift;
    if ( defined $_[0] and $_[0] !~ m/^(?:DATATYPE|=)/i ) {
        my $datatype = shift;
        my $matrix = $factory->create_matrix( '-type' => $datatype ); 
        push @{ $self->{'_context'} }, $matrix;
        $logger->info( "datatype: $datatype" );
    }
}

sub _items {
    #my $self = shift;
}

sub _gap {
    my $self = shift;
    if ( $_[0] !~ m/^(?:GAP|=)/i and !$self->{'_gap'} ) {
        $self->{'_gap'} = shift;
        my $gap = $self->{'_gap'};
        $self->_current->set_gap( $gap );
        $logger->info( "gap character: $gap" );
        undef $self->{'_gap'};
    }
}

sub _missing {
    my $self = shift;
    if ( $_[0] !~ m/^(?:MISSING|=)/i and !$self->{'_missing'} ) {
        $self->{'_missing'} = shift;
        my $missing = $self->{'_missing'};
        $self->_current->set_missing( $missing );
        $logger->info( "missing character: $missing" );
        undef $self->{'_missing'};
    }
}

sub _symbols {
    my $self = shift;
    if ( $_[0] !~ m/^(?:SYMBOLS|=|")$/i and $_[0] =~ m/^"?(.)"?$/ ) {
        push @{ $self->{'_symbols'} }, $1;
    }
}

sub _charlabels {
    my $self = shift;
    if ( defined $_[0] and uc $_[0] ne 'CHARLABELS' ) {
        push @{ $self->{'_charlabels'} }, shift;
    }
}

sub _matrix {
    my $self  = shift;
    my $token = shift;
    if ( not defined $self->{'_matrixtype'} ) {
        $self->{'_matrixtype'} = $self->_current->get_type;
        if ( @{ $self->{'_charlabels'} } ) {
            $self->_current->set_charlabels(
                $self->{'_charlabels'}
            );
        }
    }

    # first token: 'MATRIX'
    if ( not isa($token, 'ARRAY') and uc($token) eq 'MATRIX' ) {
        $self->{'_linemode'} = 1;
        $logger->info( "starting matrix" );
        return;
    }
    elsif ( isa($token, 'ARRAY') and not grep { /^;$/ } @{ $token } ) {
        my $name;
        for my $i ( 0 .. $#{ $token } ) {
            if ( not $name and $token->[$i] !~ qr/^\[/ ) {
                $name = $token->[$i];
                if ( not exists $self->{'_matrix'}->{$name} ) {
                    $self->{'_matrix'}->{$name} = [];
                }
            }
            elsif ( $name and $token->[$i] !~ qr/^\[/ ) {
                if ( $self->{'_matrixtype'} =~ m/^continuous$/i ) {
                    push @{ $self->{'_matrix'}->{$name} }, map { split(/\s+/, $_) } $token->[$i];
                }
                else {
                    push @{ $self->{'_matrix'}->{$name} }, map { split(//, $_) } $token->[$i];
                }
            }
            else {
                next;
            }
        }
    }
    elsif ( isa($token, 'ARRAY') and grep { /^;$/ } @{ $token } ) {
        my $name;
        for my $i ( 0 .. $#{ $token } ) {
            last if $token->[$i] eq ';';
            if ( not $name and $token->[$i] !~ qr/^\[/ ) {
                $name = $token->[$i];
                $self->{'_matrix'}->{$name} = [] if not $self->{'_matrix'}->{$name};
                next;
            }
            elsif ( $name and $token->[$i] !~ qr/^\[/ ) {
                if ( $self->{'_matrixtype'} =~ m/^continuous$/i ) {
                    push @{ $self->{'_matrix'}->{$name} }, map { split(/\s+/, $_) } $token->[$i];
                }
                else {
                    push @{ $self->{'_matrix'}->{$name} }, map { split(//, $_) } $token->[$i];
                }
            }
        }

        # link to taxa
        for my $row ( keys %{ $self->{'_matrix'} } ) {
            my $taxon;

            # find / create matching taxon, matrix is linked
            if ( my $taxa = $self->_current->get_taxa ) {
                FINDTAXON: for ( @{ $taxa->get_entities } ) {
                    if ( $_->get_name eq $row ) {
                        $taxon = $_;
                        last FINDTAXON;
                    }
                }
                if ( not $taxon ) {
                    $taxon = $factory->create_taxon( '-name' => $row );
                    $taxa->insert($taxon);
                }
            }

            # find / create taxa, matrix is not linked
            else {
                my $taxa;
                FINDTAXA: for ( my $i = $#{ $self->{'_context'} } ; $i >= 0 ; $i-- ) {
                    if ( $self->{'_context'}->[$i]->_type == $TAXA ) {
                        $taxa = $self->{'_context'}->[$i];
                        last FINDTAXA;
                    }
                }

                # create new taxa block
                if ( not $taxa ) {
                    $taxa = $factory->create_taxa;
                    my $current = pop( @{ $self->{'_context'} } );
                    push @{ $self->{'_context'} }, $taxa, $current;
                    $taxon = $factory->create_taxon( '-name' => $row );
                    $taxa->insert($taxon);

                }
                else {
                    FINDINNEW: for ( @{ $taxa->get_entities } ) {
                        if ( $_->get_name eq $row ) {
                            $taxon = $_;
                            last FINDINNEW;
                        }
                    }
                }

                # link current block to taxa
                if ( not $self->_current->get_taxa ) {
                    $self->_current->set_taxa($taxa);
                }
            }
            
            # create new datum
            my @logarray = @{ $self->{'_matrix'}->{ $row } };
            my $logstring = join ' ', @logarray;
            $logger->info("Setting seq: $logstring");
            my $datum = $factory->create_datum(
            	'-type'  => $self->_current->get_type,
            	'-name'  => $row, 
            	'-taxon' => $taxon,            
            );
            $datum->set_char( \@logarray );

            # insert new datum in matrix
            $self->_current->insert( $datum );
			$logger->info( sprintf("parsed %s characters for taxon '$row'", $datum->get_length ) );
			if ( $self->{'_matrixtype'} =~ qr/^continuous$/i ) {
				my $logchars = join( ' ', @{ $self->{'_matrix'}->{$row} } );
				$logger->info( "characters: $logchars" );
			}
			else {
				my $logchars = join( '', @{ $self->{'_matrix'}->{$row} } );
				$logger->info( "characters: $logchars" );
			}
        }
        $self->{'_matrix'} = {};

        # Let's avoid these!
        if ( $self->_current->get_nchar != $self->{'_nchar'} ) {
            my ( $obs, $exp ) = ( $self->_current->get_nchar, $self->{'_nchar'} );
            _bad_format( "Observed and expected nchar mismatch: $obs vs. $exp" );
        }
        elsif ( $self->_current->get_ntax != $self->{'_ntax'} ) {
            my ( $obs, $exp ) = ( $self->_current->get_ntax, $self->{'_ntax'} );
            _bad_format( "Observed and expected ntax mismatch: $obs vs. $exp" );
        }

        $self->{'_linemode'} = 0;
    }
}

sub _bad_format {
	throw 'BadFormat' => shift;
}

sub _current { shift->{'_context'}->[-1] }

sub _trees {
    my $self = shift;
    if ( $self->{'_begin'} ) {
        $self->{'_begin'}     = 0;
        $self->{'_trees'}     = '';
        $self->{'_treenames'} = [];
        $logger->info( "starting trees block" );
    }
}

sub _translate {
    my $self = shift;
    if ( defined $_[0] and $_[0] =~ m/^\d+$/ ) {
        $self->{'_i'} = shift;
        if ( $self->{'_i'} == 1 ) {
            $logger->info( "starting translation table" );
        }
        elsif ( $self->{'_i'} > 1 ) {
        }
    }
    elsif ( defined $self->{'_i'} and defined $_[0] and $_[0] ne ';' ) {
        my $i = $self->{'_i'};
        $self->{'_translate'}->[$i] = $_[0];
        $logger->debug( "Translation: $i => $_[0]" );
        $self->{'_i'} = undef;
    }
}

sub _tree {
    my $self = shift;
    if ( not $self->{'_treename'} and $_[0] !~ m/^(U?TREE|\*)$/i ) {
        $self->{'_treename'} = $_[0];
    }
    if ( $_[0] eq '=' and not $self->{'_treestart'} ) {
        $self->{'_treestart'} = 1;
    }
    if ( $_[0] ne '=' and $self->{'_treestart'} ) {
        $self->{'_tree'} .= $_[0];
    }

    # tr/// returns # of replacements, hence can be used to check
    # tree description is balanced
    if (    $self->{'_treestart'}
        and $self->{'_tree'}
        and $self->{'_tree'} =~ tr/(/(/ == $self->{'_tree'} =~ tr/)/)/ )
    {
        my $translated = $self->{'_tree'};
        my $translate  = $self->{'_translate'};
        for my $i ( 1 .. $#{$translate} ) {
            $translated =~ s/(\(|,)$i(,|\)|:)/$1$translate->[$i]$2/;
        }
        my ( $logtreename, $logtree ) = ( $self->{'_treename'}, $self->{'_tree'} );
        $logger->info( "tree: $logtreename string: $logtree" );
        $self->{'_trees'} .= $translated . ';';
        push @{ $self->{'_treenames'} }, $self->{'_treename'};
        $self->{'_treestart'} = 0;
        $self->{'_tree'}      = undef;
        $self->{'_treename'}  = undef;
    }
}

sub _end {
    my $self = shift;
    $self->{'_translate'} = [];
    if ( uc $self->{'_previous'} eq ';' and $self->{'_trees'} ) {
        my $forest = parse( '-format' => 'newick', '-string' => $self->{'_trees'} );
        for my $i ( 0 .. $#{ $self->{'_treenames'} } ) {
            $forest->get_by_index($i)->set_name( $self->{'_treenames'}->[$i] );
        }
        $self->{'_trees'} = '';
        $self->{'_treenames'} = [];
        
        # get the most recently seen taxa block, link against that
        if ( not $forest->get_taxa ) {
        	for ( my $i = $#{ $self->{'_context'} }; $i >= 0; $i-- ) {
        		my $block = $self->{'_context'}->[$i];
        		if ( $block->_type == _TAXA_ ) {
        			$forest->set_taxa($block);
        		}
        	}
        }
        
        # still not found? create one
        if ( not $forest->get_taxa ) {
        	my $taxa = $forest->make_taxa;
        	push @{ $self->{'_context'} }, $taxa;
        	
        }
        
        push @{ $self->{'_context'} }, $forest;
    }
}

sub _semicolon {
    my $self = shift;
    if ( uc $self->{'_previous'} eq 'MATRIX' ) {
        $self->{'_matrixtype'} = undef;
        $self->{'_matrix'}     = {};
        $self->{'_charlabels'} = [];
        $self->{'_linemode'}   = 0;
        if ( not $self->_current->get_ntax ) {
            my $taxon = {};
            foreach my $row ( @{ $self->_current->get_entities } ) {
                $taxon->{ $row->get_taxon }++;
            }
            my $ntax = scalar keys %{$taxon};
        }
    }
    elsif ( uc $self->{'_previous'} eq 'TAXLABELS' ) {
        foreach my $name ( @{ $self->{'_taxlabels'} } ) {
            my $taxon = $factory->create_taxon( '-name' => $name );
            $self->_current->insert($taxon);
        }
        if ( $self->_current->get_ntax != $self->{'_ntax'} ) {
            _bad_format(
                sprintf(
                    'Mismatch between observed and expected ntax: %d vs %d',
                    $self->_current->get_ntax,
                    $self->{'_ntax'}
                )
            );
        }
        $self->{'_taxlabels'} = [];
    }
    elsif ( uc $self->{'_previous'} eq 'SYMBOLS' ) {
		my $logsymbols = join( ' ', @{ $self->{'_symbols'} } );
		$logger->info( "symbols: $logsymbols" );
        $self->{'_symbols'} = [];
    }
    elsif ( uc $self->{'_previous'} eq 'CHARLABELS' ) {
        if ( @{ $self->{'_charlabels'} } ) {
            my $logcharlabels = join( ' ', @{ $self->{'_charlabels'} } );
            $logger->info( "charlabels: $logcharlabels" );
        }
    }
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The nexus parser is called by the L<Bio::Phylo::IO> object. Look there for
examples of file parsing and manipulation.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Nexus.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

1;
