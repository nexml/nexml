#!/bin/sh
echo "The following files are currently invalid example files (according to the xerces-based validator in java/validator):"

echo "The following schema in the examples directory are not nexml instances:"
echo "  examples/mesquite.xml"

echo "They will be skipped. Other example files should validate..."
set -x
java/validator/validate_nexml.sh examples/TreeInfer.xml examples/characters.xml examples/nexml.xml examples/rest.xml examples/taxa.xml examples/trees.xml examples/verbose.xml 
python python/setup.py test