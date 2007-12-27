#! /usr/bin/env python

############################################################################
##  testconf.py
##
##  Part of the DendroPy phylogenetic tree manipulation library.
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
This module provides configuration utilities for the testing suite.
"""

import unittest
import os
from pynexml import get_logger
_LOG = get_logger("dendro.tests.test_conf")

import os

def test_source_path(filename):
    return os.path.join('sources', filename)
    
def test_target_path(filename):
    return os.path.join('output', filename)
        
class TestConf(unittest.TestCase):
    def test_log(self):
        _LOG.debug("This is test of logging at the debug level")
        _LOG.info("This is test of logging at the info level")
        _LOG.warning("This is test of logging at the warning level")
        _LOG.error("This is test of logging at the error level")
        _LOG.critical("This is test of logging at the critical level")

class DummyLogger(object):
    def debug(self, msg):
        print msg

class TestConf(unittest.TestCase):
    def test_log(self):
        _LOG.debug("This is test of logging at the debug level")
        _LOG.info("This is test of logging at the info level")
        _LOG.warning("This is test of logging at the warning level")
        _LOG.error("This is test of logging at the error level")
        _LOG.critical("This is test of logging at the critical level")

class DummyLogger(object):
    def debug(self, msg):
        print msg

def additional_tests():
    return unittest.TestLoader().loadTestsFromTestCase(TestConf)

if __name__ == "__main__":
    unittest.main()
