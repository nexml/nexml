<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <?

        function curPageURL() {
            $pageURL = 'http';
            if ( $_SERVER["HTTPS"] == "on" ) {
                $pageURL .= "s";
            }
            $pageURL .= "://";
            if ( $_SERVER["SERVER_PORT"] != "80" ) {
                $pageURL .= $_SERVER["SERVER_NAME"].":".$_SERVER["SERVER_PORT"].$_SERVER["REQUEST_URI"];
            } 
            else {
                $pageURL .= $_SERVER["SERVER_NAME"].$_SERVER["REQUEST_URI"];
            }
            return $pageURL;
        }    
    
        /*
        possible sections:
            wiki
            mail
            svn
            tracker
        */
        // url argument comes from $PATH_INFO
        $page = explode("/",$_SERVER['PATH_INFO']);
        $requested_feed = $page[1];
//        $requested_feed = 'wiki';
        
        // define hooks to rss_parser class as xml functions do not allow object methods as handlers.
        function rss_start_element( $parser, $name, $attributes ) {
            global $rss;
            $rss->start_element( $parser, $name, $attributes );
        }
        
        function rss_end_element( $parser, $name ) {
            global $rss;
            $rss->end_element( $parser, $name );
        }
        
        function rss_character_data( $parser, $data ) {
            global $rss;
            $rss->character_data( $parser, $data );
        }
        
        
        class rss_parser {
        
            // constructor. setup parser options and handlers.
            function rss_parser() {
                $this->error = '';
                $this->file = '';
                
                $this->channel = array();
                $this->data = '';
                $this->stack = array();
                $this->num_items = 0;
                
                $this->xml_parser = xml_parser_create();
                xml_set_element_handler($this->xml_parser, "rss_start_element", "rss_end_element");
                xml_set_character_data_handler($this->xml_parser, "rss_character_data");
            }
        
            function character_data( $parser, $data ) {
            
                // concatenate non-parsed data...
                if ( empty( $this->data ) ) { 
                    $this->data = $data;
                }
                
                // and get rid of white space.
                else {
                    $this->data .= $data;
                }
            }
        
            function start_element( $parser, $name, $attrs ) {
                switch( $name ) {
                    case 'RSS':
                    break;
                    
                    case 'CHANNEL':
                    break;
                    
                    case 'IMAGE':
                    array_push($this->stack, $name);
                    break;
                    
                    case 'ITEM':
                    array_push($this->stack, $name);
                    array_push($this->stack, $this->num_items); // push item index.
                    $this->item[$this->num_items] = array();
                    $this->num_items++;
                    break;
                    
                    case 'TEXTINPUT':
                    array_push($this->stack, $name);
                    break;
                    
                    default:
                    array_push($this->stack, $name);
                    break;
                
                }
            }
        
            function end_element( $parser, $name ) {
                switch ( $name ) {
                    case 'RSS':
                    break;
                    
                    case 'CHANNEL':
                    break;
                    
                    case 'IMAGE':
                    array_pop($this->stack);
                    break;
                    
                    case 'ITEM':
                    array_pop($this->stack);
                    array_pop($this->stack);
                    break;
                    
                    case 'TEXTINPUT':
                    array_pop($this->stack);
                    break;
                    
                    default: // child element.
                    $element = (implode("']['",$this->stack));
                    eval("\$this->channel['$element']=\$this->data;"); // this does all the hard work.
                    array_pop($this->stack);
                    $this->data = '';
                    break;
                }
            }
            
            function parse() {
                if ( !( $fp = @fopen( $this->file, "r" ) ) ) {
                    $this->error = "Could not open RSS source \"$this->file\".";
                    return false;
                }
                while ( $data = fread( $fp, 4096 ) ) {
                    if ( !xml_parse( $this->xml_parser, $data, feof( $fp ) ) ) {
                        $this->error = sprintf(
                            "XML error: %s at line %d.",
                            xml_error_string( xml_get_error_code( $this->xml_parser ) ),
                            xml_get_current_line_number( $this->xml_parser ) 
                        );
                        return false;
                    }
                }
                xml_parser_free( $this->xml_parser );
                return true;
            }   
        
        } // class rss_parser.

    ?>
  <head>

    <!-- site meta data -->  
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta name="description" content="Nexml is a file format for encoding phylogenetic data using xml." />
    <meta name="keywords" content="nexml, xml, nexus, phylogenetics, phylogenies, trees, dna, rna, nucleotide, standard, continuous, amino, acid, matrix, tree, network, data" />
    <meta name="author" content="Rutger Vos" />
    <meta name="robots" content="index,follow" />
    <meta name="verify-v1" content="rtW0xb5pOfIPc63Qi8LvHmqYqUSq+/aynzeNXo2CIls=" />
    <title>nexml - encoding phylogenetic data in xml - <?= $requested_feed ?></title>
    
    <!-- RSS feeds -->
    <link
      rel="alternate"
      type="application/rss+xml"
      title="nexml-discuss mailing list - RSS 2.0"
      href="http://mailbucket.org/nexml_discuss.xml"/>
    <link 
      rel="alternate" 
      type="application/rss+xml" 
      title="SourceForge project summary - RSS 2.0" 
      href="http://sourceforge.net/export/rss2_projsummary.php?group_id=209571"/>
    <link
      rel="alternate"
      type="application/rss+xml" 
      title="Wiki changes - RSS 2.0"
      href="https://www.nescent.org/wg/evoinfo/index.php?title=Future_Data_Exchange_Standard&amp;action=history&amp;feed=rss"/>

    <link
      rel="alternate"
      type="application/rss+xml"
      title="SVN revision history - RSS 2.0"
      href="/nexml.rss"/>
    
    <!-- external inclusions -->
    <link rel="stylesheet" type="text/css" href="/nexml/html/style.css" />

    <!-- don't use shortcut, expand closing tag!
    Thanks, policywatcher! http://carroll.org.uk/archives/2005/01/17/xhtml-script-tag-in-internet-explorer -->
    <script type="text/javascript" src="/nexml/html/script.js" ></script>
    
  </head>
  
  <body>
    <h1 class="hide">nexml: phylogenetic data in xml - <?= $requested_feed ?></h1>
    <div id="thetop">
      <a id="top" name="top"></a>
      <p class="hide">
        Skip to: 
        <a href="#sitemenu" accesskey="2">Site menu</a> |
        <a href="#maincontent" accesskey="3">Main content</a>
      </p>
    </div>
    <div id="container">
      <div id="main">

        <!-- main banner in the top-left -->
        <div id="logo">       
          <h1>
            [<a href="/" accesskey="4">nexml</a>]
          </h1><span id="tagline">phylogenetic data in xml</span>

        </div>
        
        <!-- site blurb in the top-middle -->
        <div id="intro">
          <h2>
            <a id="maincontent" name="maincontent"></a>
            The future data exchange standard is here!
          </h2>
          <p>
            nexml is an exchange standard for representing 
            phylogenetic data &mdash; inspired by the commonly used 
            NEXUS format, but more robust and easier to process.
          </p>
        </div>
        <div class="clear"></div>

        <!-- validate form -->   
        <div id="validateDiv">
        <form 
            action="http://nexml-dev.nescent.org/nexml/validator" 
            enctype="multipart/form-data" 
            method="post" 
            id="validateForm">
            <fieldset id="validateFieldset">
                <legend id="validateLegend">Validate nexml data</legend>
                <label for="validateUpload">Select file to validate</label>
                <input type="file" name="file" id="validateUpload"/>
                <input type="submit" id="validateSubmit" value="Validate"/>
            </fieldset>
        </form>          
        </div>
        <?
            // feed urls
            $feed_url = array(
                'wiki'    => '6d460b43b6f6ab43291486607cedf36f',
                'mail'    => 'PLIrzi2Y3BGLOzPq8TxBKg',
                'svn'     => 'bKPzI8yX3BGsgnk6xAnzeQ',
                'tracker' => 'mFzHKL6X3BGhQ3_D2R2EvQ',
            );            
            
            if ( isset( $feed_url[$requested_feed] ) ) {
                $base = 'http://pipes.yahoo.com/pipes/pipe.run?_id=';
                $feed = $base . $feed_url[$requested_feed] . '&_render=rss';
                $rss = new rss_parser();       
                $rss->file = $feed;
                $rss->parse() or die( $rss->error );     
                if ( $rss->error ) {
                    print $rss->error;
                }

        ?>
        <h3 class="headerstyle"><? echo($rss->channel['TITLE']) ?></h3>
        <div class="linkshare">
            <? $url = curPageURL(); ?>
            <a class="sharelink" rel="nofollow" href="http://digg.com/submit?phase=2&amp;url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/digg.gif" alt="digg"/>
            </a>
            <a class="sharelink" rel="nofollow" href="http://reddit.com/submit?url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/reddit.gif" alt="reddit"/>
            </a>
            <a class="sharelink" rel="nofollow" href="http://del.icio.us/post?url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/delicious.gif" alt="del.icio.us"/>
            </a>    
            <a class="sharelink" rel="nofollow" href="http://www.facebook.com/share.php?u=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/facebook.gif" alt="facebook"/>
            </a>              
            <div class="credit date">  &mdash; 
                <?= date("r") ?> &nbsp;
                <a class="sharelink" href="<?= htmlspecialchars($feed) ?>">
                    <img class="rsslink" src="/nexml/html/img/feed.png" alt="rss"/>
                </a>                 
            </div>          
        </div>        
        <p><?= $rss->channel['DESCRIPTION'] ?>
        </p><ul>
        <?
                foreach( $rss->channel['ITEM'] as $item ) {
                    ?>
                    <li class="rss"><strong><?= htmlspecialchars($item['TITLE']) ?></strong>       
                    <small class="credit"> by <?= htmlspecialchars($item['AUTHOR']) ?> on 
                    <?= htmlspecialchars($item['PUBDATE']) ?>                                                         
                    [<a class="rss" href="<?= htmlspecialchars(trim($item['LINK'])) ?>">link</a>]
                    </small></li>
                    <?
                }
                ?></ul><?
            }
            else {    
        ?>

        <!-- regular text items -->
        <h3 class="headerstyle">
          The idea behind the project
        </h3>
        <div class="linkshare">
            <? $url = curPageURL(); ?>
            <a class="sharelink" rel="nofollow" href="http://digg.com/submit?phase=2&amp;url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/digg.gif" alt="digg"/>
            </a>
            <a class="sharelink" rel="nofollow" href="http://reddit.com/submit?url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/reddit.gif" alt="reddit"/>
            </a>
            <a class="sharelink" rel="nofollow" href="http://del.icio.us/post?url=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/delicious.gif" alt="del.icio.us"/>
            </a>    
            <a class="sharelink" rel="nofollow" href="http://www.facebook.com/share.php?u=<?= $url ?>">
                <img class="sharelink" src="/nexml/html/img/facebook.gif" alt="facebook"/>
            </a>  
            <div class="credit date"> &mdash;
                <?= date("r") ?> 
            </div>
        </div>
        <p>
          The <a rel="nofollow" href="http://www.citeulike.org/user/rvosa/article/2011773">
          NEXUS file format</a> is a commonly used format for 
          phylogenetic data. Unfortunately, over time, the format
          has become overloaded - which has caused various
          <a rel="nofollow" href="https://www.nescent.org/wg_evoinfo/NEXUS_Problems">
          problems</a>. Meanwhile, new technologies around the
          <a rel="nofollow" href="http://www.w3.org/XML/">XML</a> standard have
          emerged. These technologies have the potential to greatly
          simplify, and improve robustness, in the processing of phylogenetic data:
          </p>
          <ul>
            <li class="rss">
                <strong>Validation</strong> &mdash; some of the issues hampering
                interoperability are caused by the fact that no formal
                specification exists for NEXUS files, and no objective way
                to validate files. Thanks to <a rel="nofollow" href="http://www.w3.org/XML/Schema">
                XML Schema</a> we can now define a grammar against which 
                data files can be validated.
            </li>
            <li class="rss">
                <strong>Web services</strong> &mdash; a number of different 
                technologies (such as <a rel="nofollow" href="http://www.xmlrpc.com/">XML-RPC</a>, 
                <a rel="nofollow" href="http://en.wikipedia.org/wiki/Representational_State_Transfer">REST</a> 
                and <a rel="nofollow" href="http://www.w3.org/TR/soap/">SOAP</a>) have 
                emerged allowing disparate services to be glued together 
                over the internet. Using such services, researchers can 
                "farm out" their calculations to dedicated servers, such
                as those of the <a rel="nofollow" href="http://www.phylo.org">CIPRES
                project</a>.
            </li>
            <li class="rss">
                <strong>Native XML databases</strong> &mdash; relational
                databases are based on a fixed schema. For phylogenetic
                data this poses problems, because the field of phyloinformatics
                moves very rapidly: new metrics and analysis types are
                published constantly. 
                <a rel="nofollow" href="http://xmldb-org.sourceforge.net/">XML databases</a>
                are freed from this constraint, simplifying storage of
                unusual data types.
            </li>          
          </ul>
        <p>
          Therefore, a group of developers of phylogenetic software have 
          come together as part of the <a href="http://www.nescent.org">
          NESCent</a> working group for <a href="https://www.nescent.org/wg_evoinfo">
          evolutionary informatics</a> to develop a 
          <a href="https://www.nescent.org/wg_evoinfo/Future_Data_Exchange_Standard">
          new data exchange standard</a> based on these technologies.
        </p>
        <p class="internallink">

          [ <a href="#top">Back to top</a> ]
        </p>
        <h3 class="headerstyle">
          What are we doing about it?
        </h3>
        <p>
          Nexml development is being undertaken in a number of
          subprojects:
        </p>
        <ul>
          <li class="rss">
          In the first place, we're designing an XML schema. This
          schema (designated as namespace http://www.nexml.org/1.0)
          is documented on our 
          <a href="https://www.nescent.org/wg_evoinfo/Future_Data_Exchange_Standard">wiki</a>;
          the bleeding edge version is available from <a href="/nexml/html/index/svn">svn</a>;
          the <a href="/nexml/xsd">source code can be browsed</a> on our
          site (it's a check out from our repository which is updated
          every five minutes); for bug reports and feature requests please
          visit our <a href="/nexml/html/index/tracker">issue tracker</a>
          page.
          </li>
          <li class="rss">
          Secondly, we're developing java class libraries to aid
          software developers wanting to use nexml in their applications
          These class libraries will support reading of nexml data
          through a SAX API (to facilitate large data sets or data
          streams) and writing through a set of simple interfaces objects
          need to implement in order to become xml writable. The code
          for this subproject is in the java subfolder on the svn
          repository.
          </li>
          <li class="rss">
          Third, we're developing perl modules that plug into the 
          IO backend of the Bio::Phylo package on CPAN. Much of the
          validator code on this website runs on these modules. As
          Bio::Phylo is (developing to become) compatible with BioPerl
          and Bio::NEXUS, this will make nexml IO available to a large
          number of perl programmers.
          </li>
        </ul>
        <p>
        In addition, the developers of the phycas project are working
        on parsing and serializing their python objects in nexml, and
        Sergei Kosakovsky Pond of hyphy is interested in nexml IO for
        that project. We've also had (very tentative) conversations with
        Paul Lewis of NCL (the nexus class libraries in c++) about nexml
        integration. In short, a lot of active but alpha stage development
        is going on.
        </p>
        <p class="internallink">
          [ <a href="#top">Back to top</a> ]
        </p>
        <h3 class="headerstyle">
          Get involved!
        </h3>
        <p>
          If you are interested in being involved in the nexml project
          in any way, please do! 
          <!-- more goes here -->
        </p>
        <p class="internallink">
          [ <a href="#top">Back to top</a> ]
        </p>
        
        <? } ?>
        
      </div>
      
      <div id="sidebar">
      
        <!-- top-right site navigation menu -->
        <h2 class="quicklinks">
          <a id="quicklinks" name="quicklinks"></a>Quick links:
        </h2>
          <a class="quicklink" href="/nexml/html/doc/schema-1/">Schema 1.0</a>
          <a class="quicklink" href="/nexml/examples">Example files</a>          
        <br />
        <h2 class="sidelink menuheader">
          <a id="sitemenu" name="sitemenu"></a>Bindings:
        </h2>
        
        <a class="sidelink" href="/nexml/java/">Java</a> 
        <span class="hide">|</span>
        
        <a class="sidelink" href="/nexml/python/">Python</a>
        <span class="hide">|</span> 
        
        <a class="sidelink" href="/nexml/perl/">Perl</a> 
        <span class="hide">|</span>
        
        <h2 class="sidelink menuheader">
          <a id="sitemenu" name="sitemenu"></a>Goings on:
        </h2>
        
        <a class="sidelink" href="/nexml/html/index/wiki/">Wiki</a> 
        <span class="hide">|</span>
        
        <a class="sidelink" href="/nexml/html/index/mail/">Mailing list</a>
        <span class="hide">|</span> 
        
        <a class="sidelink" href="/nexml/html/index/svn/">SVN repository</a> 
        <span class="hide">|</span>

        <a class="sidelink" href="/nexml/html/index/tracker/">Issue tracker</a>
        <span class="hide">|</span>
           
        
        <a class="hide" href="#top" accesskey="1">Top of page</a>
        
        <!-- links at the bottom right -->
        <h3>
          External links
        </h3>
        <p>
          - <a href="http://sourceforge.net/projects/nexml/">
          SourceForge project page
          </a>
          <br />

          - <a href="http://www.nescent.org/wg_evoinfo/Future_Data_Exchange_Standard">
          EvoInfo wiki
          </a><br />
          
          - <a href="http://www.nescent.org">
          NESCent
          </a><br />

          - <a href="http://mesquiteproject.org">
          Mesquite
          </a><br />
        </p>
      </div>
      <div class="clear">
        &nbsp;
      </div>
    </div>
    <div id="footer">
      <p>
        &copy; <?= date("Y") ?> <a href="http://rutgervos.blogspot.com" class="credit">Rutger Vos</a>. 
        Design by <a href="http://andreasviklund.com/" class="credit">Andreas Viklund</a> 
        of <a href="http://jokkmokk.biz/" title="ITUS Jokkmokk">Jokkmokk</a>.
      </p>
    </div>
    <script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
    </script>
    <script type="text/javascript">
      _uacct = "UA-3174308-1";
      urchinTracker();
    </script>
  </body>
</html>
