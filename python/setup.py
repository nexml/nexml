#! /usr/bin/env python
# Copyright (c) 2005-7 by Mark T. Holder,  University of Kansas
# (see bottom of file)
import ez_setup
import sys
ez_setup.use_setuptools()

from setuptools import setup, find_packages, Extension

setup(name = "pynexml", 
      version = "0.01",
      packages = find_packages(),
      maintainer = "Jeet Sukumaran and Mark Holder", 
      maintainer_email = "jeetsukumaran@frogweb.org mtholder@gmail.com", 
      description = "Phylogenetic stuff", 
      #test_suite = "dendropy.tests",
      #package_data = {
      #  "dendropy": ["tests/files/*", "tests/output/*", "*conf"],
      #  },
      classifiers = [
            "Development Status :: 2 - Pre-Alpha",
            "Environment :: Console",
            "Intended Audience :: Developers",
            "Intended Audience :: Science/Research",
            "License :: OSI Approved :: BSD License",
            "License :: OSI Approved :: GNU Library or  General Public License (GPL)",
            "Natural Language :: English",
            "Operating System :: OS Independent",
            "Programming Language :: Python",
            "Topic :: Scientific/Engineering :: Bio-Informatics",
            ]
    )

############################################################################
##  __init__.py
##
##  Part of the PhyloInfo phylogenetic data parsing library.
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
