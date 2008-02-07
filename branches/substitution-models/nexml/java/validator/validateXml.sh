#!/bin/sh

# This is all to determine what directory this script is located
# in.
program="$0"
while [ -h "$program" ]; do
	link=`ls -ld "$program"`
	link=`expr "$link" : '.*-> \(.*\)'`
	if [ "`expr "$link" : '/.*'`" = 0 ]; then
		dir=`dirname "$program"`
		program="$dir/$link"
	else
		program="$link"
	fi
done
script_name=`basename $program`
script_directory=`dirname $program`
script_directory=`cd $script_directory && /bin/pwd`

xml="$1"
xsd="$2"
ns="$3"

if [ "$xml" = "" -o "$xsd" = "" ]; then
	echo "Usage: validateXml xml_filename xsd_filename"
	exit 1
fi

if ! test -e "$xml"
then
	echo "$script_name: file $xml does not exist"
	exit 1
fi

if ! test -e "$xsd"
then
	echo "$script_name: file $xsd does not exist"
	exit 1
fi

if test "$ns" = ""
then
	java -classpath "${script_directory}/build:${script_directory}/jars/xercesImpl.jar" -Dxml="$xml" -Dxsd="$xsd" validator.XmlValidator
	exit $?
else
	java -classpath "${script_directory}/build:${script_directory}/jars/xercesImpl.jar" -Dxml="$xml" -Dxsd="$xsd" -Dns="$ns" validator.XmlValidator
	exit $?
fi
