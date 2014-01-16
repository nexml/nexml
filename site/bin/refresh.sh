#!/bin/bash
# this script refreshes the nexml site. It is typically run
# by hand from within the directory where it resides, or
# by a cron job

############################################################
# ENVIRONMENT VARIABLES
# all site generation nagivation is relative to this directory. 
# On the server this is the home directory of the nexml account, 
# on a local machine this has to be set to point to the trunk 
# folder
if [ -z "$NEXML_HOME" ]; then
	export NEXML_HOME="$HOME"
fi

# this directory is for navigation *within* the code base, e.g.
# when a test suite for a processing library wants to run tests
# against files in the examples folder
if [ -z "$NEXML_ROOT" ]; then
	export NEXML_ROOT="$NEXML_HOME/nexml"
fi

# needed to do git pulls of the code base
export GIT=`which git`
# needed to do xsl transforms to generate RSS feeds
export XSLTPROC=`which xsltproc`
# needed to generate inheritance and inclusion graphs of the schema
export DOT=$NEXML_HOME/graphtools/graphviz/bin/dot
# used by static page generator object util::siteFactory
export SERVER_NAME=nexml-dev.nescent.org
export STATIC_ROOT=/nexml/html
export DOCUMENT_ROOT=$NEXML_HOME
# search paths for perl scripts
export PERL5LIB=$NEXML_HOME/perllib:$NEXML_HOME/perllib/arch:$NEXML_ROOT/perl/lib:$NEXML_ROOT/site/lib
# path to mesquite, to build mesquite io library against
if [ -z "$MESQUITE_ROOT" ]; then
	export MESQUITE_ROOT="$NEXML_HOME/Mesquite_Folder"
fi

###########################################################
# START THE CRON JOB
# go home
cd $NEXML_HOME
echo '' > cron.log

#######################################################
# CREATE TIME STAMP
timestamp=`date`
echo "Last updated: $timestamp" > $NEXML_ROOT/html/include/refreshDate.txt

##########################################################
# DO AN SVN UPDATE, BUILD RSS FEED FROM LOG
# go into nexml/ dir, do a git pull
cd $NEXML_ROOT
$GIT pull

# FIXME: it might be nice - but not necessary - for the
# website to display git commit messages as an RSS feed.

#########################################################
# BUILD RELEASES
dirs='java python perl xsd'
for dir in $dirs
do
	echo "building $dir"
	cd $NEXML_ROOT/$dir
	sh build.sh 2>>$NEXML_HOME/cron.log
done
# create cpp distribution
#export XSD_HOME="$HOME/xsd-3.1.0.b1-i686-macosx"
#export XERCES_HOME="$HOME/xerces-c_2_8_0-x86-macosx-gcc_4_0"
#cd $NEXML_ROOT/cpp
#sh build.sh 2>>$NEXML_HOME/cron.log
#mv *.gz ../downloads
#rm -rf out

#########################################################
# BUILD STATIC HTML
# this builds the schema documentation
cd $NEXML_ROOT/site/bin
./index.cgi $NEXML_ROOT/xsd/nexml.xsd 2>>$NEXML_HOME/cron.log
# create htmlified rss pages and static pages
./rss2html.cgi 2>>$NEXML_HOME/cron.log
./static.cgi 2>>$NEXML_HOME/cron.log
