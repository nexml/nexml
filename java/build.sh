#!/bin/sh
# this shell script is only needed for updating the nexml.org site - 
# if you downloaded this from sourceforge or somewhere else and you
# don't know what to do with it, don't worry: you probably don't need
# it.
ant jar
mv nexml.jar ../downloads
ant validator
mv validator.jar ../downloads
ant mesquite
mv mesquite-nexml.zip ../downloads
ant clean
mvn install:install-file -Dfile=../downloads/nexml.jar -DgroupId=org.nexml.model -DartifactId=nexml -Dversion=1.5-SNAPSHOT -Dpackaging=jar
