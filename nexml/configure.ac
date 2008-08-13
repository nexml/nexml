#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_INIT(nexmlparse, 0.1.01, nexml-discuss@lists.sourceforge.net)
AC_CONFIG_HEADERS([config.h])

AC_PREREQ(2.57)

#	Directory that contains install-sh and other auxiliary files
AC_CONFIG_AUX_DIR([config])

################################################################################
#	According to (http://www.mail-archive.com/autoconf@gnu.org/msg14232.html)
#		this macro should be after AC_INIT but before AM_INIT_AUTOMAKE
################################################################################
AC_CONFIG_MACRO_DIR(config)

AM_INIT_AUTOMAKE([1.6 foreign dist-zip tar-ustar filename-length-max=299])

AC_CONFIG_SRCDIR([ChangeLog])

# Checks for programs.
AC_PROG_LN_S
AC_PROG_INSTALL
AC_PROG_LIBTOOL
AC_CHECK_PROG([EXSED], sed, sed, no)
if test "$EXSED" = no; then
	AC_MSG_ERROR([sed is not found])
else
	if `echo "bogus/TEST.ext" | $EXSED -r -e 's:.*/([a-zA-Z]*)S\..*:\1:' >> /dev/null 2>&1` ; then
		EXSED="$EXSED -r"
	else
		if `echo "bogus/TEST.ext" | $EXSED -E -e 's:.*/([a-zA-Z]*)S\..*:\1:' >> /dev/null 2>&1` ; then
			EXSED="$EXSED -E"
		else
			AC_MSG_ERROR([sed is broken])
		fi
	fi
fi
AC_SUBST([EXSED])

################################################################################
# calling AC_PROG_CXX resets CXXFLAGS, we use our own flags set in the 
#	the AX_CXXFLAGS macro below.   
# So we cache the argument to configure
#	here in ARG_CXX_FLAGS (so that our macro does not override them)
ARG_CXX_FLAGS="$CXXFLAGS"
################################################################################
ARG_CXX_FLAGS="$CXXFLAGS"
AC_PROG_CXX
AC_PROG_RANLIB
CXXFLAGS="$ARG_CXX_FLAGS"
AX_CXXFLAGS
AC_SUBST(CXXFLAGS)
AC_CXX_RTTI



CPPFLAGS="-I\$(top_srcdir) $CPPFLAGS"
# Checks for libraries.

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([float.h stdlib.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_C_INLINE
AC_STRUCT_TM

# Checks for library functions.
AC_FUNC_MKTIME
AC_FUNC_STRTOD
AC_CHECK_FUNCS([memmove strchr strtol])

################################################################################
# the install-check requires python with subprocess to actually perform a check
# these tests should not cause failure of configure if python is not found.
################################################################################
#AC_PATH_PROG([PYTHON], [python], [], [$PATH])
#AC_PYTHON_MODULE([subprocess], [], [])  
#AC_SUBST(PYTHON)

#if test -z "$PYTHON"
#then
#	MAYBE_TEST_DIR=
#else
#	MAYBE_TEST_DIR="test"
#fi

XERCES_HOME="/usr/local"
AC_ARG_WITH(
	[xerces],
	AC_HELP_STRING(
		[--with-xerces=DIR],
		[Specify the XERCES_HOME directory for xerces library (parent of the include/xercesc and lib directories)]
		),
	[
	if ! test "$withval" = "yes" -o "$withval" = "no" ; then
		XERCES_HOME="$withval"
	fi
	])

if ! test -d "$XERCES_HOME/include/xercesc" ; then
	AC_MSG_ERROR([Xerces is a prerequisite for building nexmlparse. Expecting a directory called $XERCES_HOME/include/xercesc, verify that the argument to --with-xercesc is correct])
fi

XSD_HOME="/usr/local"
AC_ARG_WITH(
	[code-syn-xsd],
	AC_HELP_STRING(
		[--with-code-syn-xsd=DIR],
		[Specify the XSD_HOME directory for the xsd tool by CodeSynthesis]
		),
	[
	if ! test "$withval" = "yes" -o "$withval" = "no" ; then
		XSD_HOME="$withval"
		if ! test -f "$XSD_HOME/bin/xsd" ; then
			AC_MSG_ERROR([The xsd tool by CodeSynthesis was not found at $XSD_HOME/bin/xsd])
		fi
	fi
	])

XSDBIN="$XSD_HOME/bin/xsd"
if ! test -f "$XSDBIN" ; 
then
	AC_MSG_ERROR([The xsd tool by CodeSynthesis is a prerequisite for rebuilding nexmlparse code. Use the --with-code-syn-xsd argument to configure to specify the location of the xsd if you do wish to use it. Expecting an executable at "$XSD_HOME/bin/xsd"])
fi
if ! test -d "$XSD_HOME/libxsd" ; 
then
	AC_MSG_ERROR([The xsd tool by CodeSynthesis is a prerequisite for rebuilding nexmlparse code. Use the --with-code-syn-xsd argument to configure to specify the location of the xsd if you do wish to use it. Expecting a directory at "$XSD_HOME/libxsd"])
fi

CPPFLAGS="$CPPFLAGS -I$XERCES_HOME/include -I$XSD_HOME/libxsd"
LDFLAGS="$LDFLAGS -L$XERCES_HOME/lib"
LDADD="$LDADD -lxerces-c"







CPPFLAGS="$CPPFLAGS"

AC_SUBST(XSDBIN)
AC_SUBST(MAYBE_TEST_DIR)

AC_PROG_MAKE_SET

AC_CONFIG_FILES([					\
	Makefile 						\
	cpp/Makefile					\
	])
AC_OUTPUT
