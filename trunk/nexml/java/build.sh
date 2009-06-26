#!/bin/sh
# this shell script is only needed for updating the nexml.org site - 
# if you downloaded this from sourceforge or somewhere else and you
# don't know what to do with it, don't worry: you probably don't need
# it.
mvn install -Dmaven.test.skip=true -DcreateChecksum=true
mvn clean
