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

xsd="${NEXML_ROOT}/xsd/nexml.xsd"
ns="http://www.nexml.org/1.0"

if test -f "${script_directory}/target/classes/validator/XmlValidator.class"
then
	for xml in $@
	do
		if ! test -e "$xml"
		then
			echo "$script_name: file $xml does not exist"
			exit 1
		else
			echo "checking $xml"
			java -classpath "${script_directory}/target/classes:${script_directory}/jars/xercesImpl.jar" -Dxml="$xml" -Dxsd="$xsd" -Dns="$ns" validator.XmlValidator || exit 2
		fi
	done
else
	echo "${script_directory}/target/classes/validator/XmlValidator.class does not exist"
	echo "\"ant compile\" must be run in ${script_directory} before invoking this script"
	exit 3
fi
