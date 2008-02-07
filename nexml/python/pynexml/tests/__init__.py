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
pynexml testing suite
"""

__all__ = [
          "test_conf",
          "test_io",
          ]
import unittest
from pynexml import get_logger
_LOG = get_logger("tests.__init__")
# pylint: disable-msg=C0111,W0401,W0611

def even_more_tests(all_suites):
    "finds test from module introspection"
    if __name__ != "__main__":
         return 
    #commented out
    for i in __all__:
        module = __import__("pynexml.tests.%s" % i)
        _LOG.debug(i)
        tests_mod = getattr(module, "tests")
        sub_test_mod = getattr(tests_mod, i)
        suite = sub_test_mod.additional_tests()
        if suite:
            all_suites.append(suite)

# def additional_tests():
#     """Creates a unittest.TestSuite from all of the modules in `pynexml.tests`
# 
#     \todo uncommenting even_more_tests line results in test from "setup.py test"
#         being run 3 times each.  I don't know why. (even with it commented out
#         they are being run twice
#     """
#     all_suites = []
#     #even_more_tests(all_suites)
#     return unittest.TestSuite(all_suites)

# def test_all():
#     "Runs all of the unittests in `pynexml.tests`"
#     runner = unittest.TextTestRunner()
#     runner.run(additional_tests())

if __name__ == "__main__":
    test_all()

