#!/usr/bin/perl
# $Id: index.cgi 225 2007-12-13 07:21:53Z rvos $
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib '../../../perllib';	
    use lib '../../../perllib/arch';
    unshift @INC, '../../perl/lib';
    unshift @INC, '../../site/lib';
}
use strict;
use warnings;
use util;
use util::siteFactory;
use XML::Twig;
use LWP::UserAgent;
use Template;
use Cwd;

# unfortunately this has to be hardcoded, we can't
# use Sys::Hostname::hostname() because we're 
# generating docs from a cron job (so no CGI vars)
# and we'd be running on a virtual host anyway
my $hostname = $ENV{'SERVER_NAME'} || 'eupoa.local';

# subtree for this part of the site structure, i.e.
# the rss feeds
my $subtree = $ENV{'STATIC_ROOT'} . '/index/';

# $prefix is the path to docroot, so on server-side
# includes we need it (hence it is part of $include),
# but on the client side (e.g. paths to images in an
# html page) it needs to be stripped
my $prefix;
if ( $ENV{'DOCUMENT_ROOT'} ) {
    $prefix = $ENV{'DOCUMENT_ROOT'};
}
elsif ( -d '/Users/rvosa/Documents/workspace' ) {
    $prefix = '/Users/rvosa/Documents/workspace';
}
else {
    $prefix = $ENV{'HOME'};
}

# $include is used to find server side includes, e.g.
# when we embed javascript or css directly into a page.
# on the client side we need to strip $prefix of it.
my $include = $prefix . '/nexml/html/include';

# the paths object is a utility object that translates between
# server side paths (i.e. relative to system root) and browser 
# side paths (i.e. relative to docroot)
my $paths = util::paths->new(
    '-prefix'   => $prefix,
    '-include'  => $include,
);

# instantiate T::T object for site html
my $template = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
    'PRE_PROCESS'  => 'header.tmpl', # prefix each template
    'POST_PROCESS' => 'footer.tmpl', # suffix each template
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

# variables to be interpolated in template
my $vars = {
    'title'       => 'nexml schema 1.0',
    'mainHeading' => 'RSS feed activity',
    'currentURL'  => 'http://' . $hostname . $subtree,
    'currentDate' => my $time = localtime,
    'paths'       => $paths,
    'hostName'    => $hostname,
    'encoder'     => util::encoder->new,
};

# feeds to work on
my ( $feed_prefix, $feed_suffix ) = ( 'http://pipes.yahoo.com/pipes/pipe.run?_id=', '&_render=rss' );
my $feed_id = {
    'wiki'    => '6d460b43b6f6ab43291486607cedf36f',
    'mail'    => '87da24b80f58d7e79d3e258b198b0590',
    'code'    => 'bKPzI8yX3BGsgnk6xAnzeQ',
    'tracker' => 'mFzHKL6X3BGhQ3_D2R2EvQ',
};

# feed downloader
my $ua = LWP::UserAgent->new;
$ua->timeout(30); # this is the yahoo pipes time out
$ua->env_proxy;

# rss feed parser
my $twig = XML::Twig->new;
my $baseURL = 'http://' . $hostname;
# now fetch and serialize feeds
for my $feed_name ( keys %{ $feed_id } ) {

    # concatenate to get the full yahoo pipes url
    my $feed_url = $feed_prefix . $feed_id->{$feed_name} . $feed_suffix;
    
    # try to fetch the url
    my $response = $ua->get($feed_url);
    
    # see if it worked
    if ( $response->is_success ) {
    
        # create the outfile path (this is relative to $template's OUTPUT_PATH constructor arg)
        my $outfile = $subtree . $feed_name . '/index.html';        
        
        # response may have been "success", but that doesn't necessarily mean this is well-formed
        eval {
            $twig->parse( $response->content );
        };
        
        # we have a more-or-less sane twig, serialize to html
        if ( not $@ ) {
            $vars->{'root'} = $twig->root->first_child('channel');
            $vars->{'currentFeed'} = $feed_url;
            $vars->{'currentURL'}  = $baseURL . '/' . $feed_name . '/';
            $vars->{'mainHeading'} = 'Rss feed activity for: ' . $feed_name;
            $vars->{'title'}       = 'nexml - ' . $feed_name;

            $template->process( 'rss2html.tmpl', $vars, $outfile ) 
            || warn $template->error(); # serialize failed
        }
        
        # parse failed
        else {
            warn $@;
        }
    }
    
    # fetch failed
    else {
        warn $response->status_line;
    }
}
