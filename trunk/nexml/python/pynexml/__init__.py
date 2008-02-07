#! /usr/bin/env python

############################################################################
##  __init__.py
##
##  Part of the PyNexml phylogenetic data parsing library.
##
##  Copyright 2007 Jeet Sukumaran and Mark T. Holder.
##
##  This program is free software; you can redistribute it and/or modify
##  it under the terms of the GNU General Public License as published by
##  the Free Software Foundation; either version 3 of the License, or
##  (at your option) any later version.
##
##  This program is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##  GNU General Public License for more details.
##
##  You should have received a copy of the GNU General Public License along
##  with this program. If not, see <http://www.gnu.org/licenses/>.
##
############################################################################
"""
Required for package semantics.
"""
import os
import sys
import random

__all__ = [
	'base',
    'characters',
    'datasets',
    'newick',
    'nexml',
    'taxa',
    'trees',
    'utils',
    'xmlparser'
          ]

_NAME = "PyNexml"
_VERSION = "0.1"
_AUTHOR = "Jeet Sukumaran and Mark T. Holder"
_COPYRIGHT = "Copyright 2007 Jeet Sukumaran and Mark T. Holder."
_LICENSE = """
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program. If not, see <http://www.gnu.org/licenses/>.
"""

def python_version():
    """
    Returns Python version as float.
    """
    major_ver = sys.version_info[0]
    minor_ver = sys.version_info[1]
    return major_ver + (float(minor_ver)/10)

def is_python_at_least(version):
    """
    Returns True if Python version is at least as high as the argument
    (a numeric value).
    """
    if python_version() >= version:
        return True
    else:
        return False

_user_cfg_dir = os.path.expanduser("~/.pynexml")

def get_pynexml_cfg_file(file_name):
    """Looks for file_name in _user_cfg_dir, if it is not found there then
    the package directory is checked.
    
    Returns `None` if the file is not found.
    """
    global _user_cfg_dir
    if _user_cfg_dir:
        fp = os.path.join(_user_cfg_dir, file_name)
        if os.path.exists(fp):
            return fp
    from pkg_resources import resource_filename
    fp = resource_filename(__name__, file_name)
    if os.path.exists(fp):
        return fp
    return None


# set to True by the first call to get_logger()
_logger_initialized = False 
def get_logger(s):
    """Wrapper around logging.getLogger that make sure that the pynexml
    logging configuration file is read (or a default is applied)
    """
    global _logger_initialized
    import logging
    if not _logger_initialized:
        import logging.config
        filename = "logging_pynexml.conf"
        full_path = get_pynexml_cfg_file(filename)
        specified_path_failed = False
        if full_path and os.path.exists(full_path):
            try:
                logging.config.fileConfig(full_path)
                _logger_initialized = True
                logger = logging.getLogger(s)
                return logger
            except:
                specified_path_failed = True
        print "logging config file not found"
        logger = logging.getLogger()
        logger.setLevel(logging.NOTSET)
        ch = logging.StreamHandler()
        ch.setLevel(logging.NOTSET)
        default_fmt_str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
        formatter = logging.Formatter(default_fmt_str)
        ch.setFormatter(formatter)
        logger.addHandler(ch)
        if specified_path_failed:
            warning = 'Could not parse %s, using default logging' % full_path
            sys.stderr.write(warning)
        _logger_initialized = True
    return logging.getLogger(s)


