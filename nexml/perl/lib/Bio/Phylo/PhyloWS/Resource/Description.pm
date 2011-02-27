package Bio::Phylo::PhyloWS::Resource::Description;
use strict;
use Bio::Phylo::Listable ();
use Bio::Phylo::PhyloWS::Resource ();
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(_DESCRIPTION_ _NONE_);
use vars '@ISA';
@ISA=qw(Bio::Phylo::PhyloWS::Resource Bio::Phylo::Listable);

{

=head1 NAME

Bio::Phylo::PhyloWS::Resource::Description - Represents a PhyloWS resource description

=head1 SYNOPSIS

 # no direct usage

=head1 DESCRIPTION

This class represents a resource description for a web resource that implements the PhyloWS
recommendations.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : my $desc = Bio::Phylo::PhyloWS::Resource::Description->new( -guid => $guid );
 Function: Instantiates Bio::Phylo::PhyloWS::Resource::Description object
 Returns : a Bio::Phylo::PhyloWS::Resource::Description object 
 Args    : Required: -guid => $guid
           Optional: any number of setters.

=cut

    sub new { 
        my $self = shift->SUPER::new( 
            @_,        
            '-tag'        => 'rdf:RDF',
            '-attributes' => {
                  'xmlns:rdf'     => 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
                  'xmlns:dcterms' => 'http://purl.org/dc/terms/',
                  'xmlns:dc'      => 'http://purl.org/dc/elements/1.1/',
                  'xmlns'         => 'http://purl.org/rss/1.0/',
            }
        );
    }

=back

=head2 TESTS

=over

=item is_identifiable()

Tests if invocant has an xml id attribute

 Type    : Test
 Title   : is_identifiable
 Usage   : if ( $obj->is_identifiable ) {
              # do something
           }
 Function: Tests if invocant has an xml id attribute
 Returns : FALSE
 Args    : NONE

=cut

    sub is_identifiable { 0 }

=back

=head2 SERIALIZERS

=over

=item to_xml()

Serializes resource to RSS1.0 XML representation

 Type    : Serializer
 Title   : to_xml()
 Usage   : print $obj->to_xml();
 Function: Serializes object to RSS1.0 XML string
 Returns : String 
 Args    : None
 Comments:

=cut

    sub to_xml {
        my $self = shift;

        my $xml = '<?xml version="1.0" encoding="UTF-8"?>' . "\n";
        # <rdf:RDF
        #   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        #   xmlns:dcterms="http://purl.org/dc/terms/"
        #   xmlns:dc="http://purl.org/dc/elements/1.1/"
        #   xmlns="http://purl.org/rss/1.0/">
        $xml .= $self->get_xml_tag(0);
        
        #   <channel rdf:about="${baseURL}/${phyloWSPath}">
        $xml .= '<channel rdf:about="' . $self->get_url . $self->get_guid . '">';
        
        #     <title>${phyloWSPath}</title>
        $xml .= '<title>' . ( $self->get_name || $self->get_guid ) . '</title>';
        
        #     <link>${baseURL}</link>
        $xml .= '<link>' . $self->get_url . '</link>';
        
        #     <description>Serializations for ${phyloWSPath}</description>
        $xml .= '<description>' . $self->get_desc . '</description>' if $self->get_desc;
        
        #     <items>
        #       <rdf:Seq>
        $xml .= '<items><rdf:Seq>';
        
        for my $resource ( @{ $self->get_entities } ) {
            # 		<rdf:li rdf:resource="${baseURL}/${phyloWSPath}?format=html"/>
            $xml .= '<rdf:li rdf:resource="' . $resource->get_full_url . '"/>';
        }

        #       </rdf:Seq>
        #     </items>
        #   </channel>
        $xml .= '</rdf:Seq></items></channel>';
        
        for my $resource ( @{ $self->get_entities } ) {
            $xml .= $resource->to_xml;
        }   
        
        $xml .= '</' . $self->get_tag . '>';
        return $xml;
    }
    sub _container { _NONE_ }
    sub _type { _DESCRIPTION_ }

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=head1 CITATION

If you use Bio::Phylo in published research, please cite it:

B<Rutger A Vos>, B<Jason Caravas>, B<Klaas Hartmann>, B<Mark A Jensen>
and B<Chase Miller>, 2011. Bio::Phylo - phyloinformatic analysis using Perl.
I<BMC Bioinformatics> B<12>:63.
L<http://dx.doi.org/10.1186/1471-2105-12-63>

=head1 REVISION

 $Id$

=cut

}

1;