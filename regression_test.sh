#!/bin/sh
echo "The following files are currently invalid example files (according to the xerces-based validator in java/validator):"

# [Error] :18:53: cvc-elt.4.2: Cannot resolve 'nex:DNA' to a type definition for element 'characters'.
# [Error] :18:53: cvc-type.2: The type definition cannot be abstract for element characters.
# [Error] :18:53: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'characters'.
# [Error] :18:53: cvc-complex-type.4: Attribute 'otus' must appear on element 'characters'.
# [Error] :19:23: cvc-complex-type.2.4.a: Invalid content was found starting with element 'matrix'. One of '{dict, format}' is expected.
# [Error] :37:58: cvc-elt.4.2: Cannot resolve 'nex:STANDARD' to a type definition for element 'characters'.
# [Error] :37:58: cvc-type.2: The type definition cannot be abstract for element characters.
# [Error] :37:58: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'characters'.
# [Error] :37:58: cvc-complex-type.4: Attribute 'otus' must appear on element 'characters'.
# [Error] :38:11: cvc-complex-type.2.4.a: Invalid content was found starting with element 'matrix'. One of '{dict, format}' is expected.
# [Error] :70:60: cvc-elt.4.2: Cannot resolve 'nex:CONTINUOUS' to a type definition for element 'characters'.
# [Error] :70:60: cvc-type.2: The type definition cannot be abstract for element characters.
# [Error] :70:60: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'characters'.
# [Error] :70:60: cvc-complex-type.4: Attribute 'otus' must appear on element 'characters'.
# [Error] :71:11: cvc-complex-type.2.4.a: Invalid content was found starting with element 'matrix'. One of '{dict, format}' is expected.
# [Error] :94:61: cvc-elt.4.2: Cannot resolve 'nex:RESTRICTION' to a type definition for element 'characters'.
# [Error] :94:61: cvc-type.2: The type definition cannot be abstract for element characters.
# [Error] :94:61: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'characters'.
# [Error] :94:61: cvc-complex-type.4: Attribute 'otus' must appear on element 'characters'.
# [Error] :95:23: cvc-complex-type.2.4.a: Invalid content was found starting with element 'matrix'. One of '{dict, format}' is expected.
# [Error] :113:57: cvc-elt.4.2: Cannot resolve 'nex:PROTEIN' to a type definition for element 'characters'.
# [Error] :113:57: cvc-type.2: The type definition cannot be abstract for element characters.
# [Error] :113:57: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'characters'.
# [Error] :113:57: cvc-complex-type.4: Attribute 'otus' must appear on element 'characters'.
# [Error] :114:23: cvc-complex-type.2.4.a: Invalid content was found starting with element 'matrix'. One of '{dict, format}' is expected.
# [Error] :132:33: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'trees'.
# [Error] :132:33: cvc-complex-type.4: Attribute 'otus' must appear on element 'trees'.
# [Error] :133:61: cvc-elt.4.2: Cannot resolve 'nex:CompactTree' to a type definition for element 'tree'.
# [Error] :133:61: cvc-type.2: The type definition cannot be abstract for element tree.
# [Error] :133:61: cvc-complex-type.3.2.2: Attribute 'rooted' is not allowed to appear in element 'tree'.
# [Error] :152:11: cvc-complex-type.2.4.a: Invalid content was found starting with element 'nodes'. One of '{dict, node}' is expected.
# [Error] :155:13: cvc-identity-constraint.4.3: Key 'rowTaxonRef' with value 'null' not found for identity constraint of element 'nexml'.
echo "  examples/compact.xml"

# [Error] :2:20: cvc-elt.1: Cannot find the declaration of element 'models'.
# [Fatal Error] :8:55: The prefix "xsi" for attribute "xsi:type" associated with an element type "states" is not bound.
echo "  examples/models.xml"


# [Error] :12:20: cvc-complex-type.2.4.a: Invalid content was found starting with element 'taxa'. One of '{dict, otus}' is expected.
# [Error] :50648:35: cvc-complex-type.3.2.2: Attribute 'taxa' is not allowed to appear in element 'trees'.
# [Error] :50648:35: cvc-complex-type.4: Attribute 'otus' must appear on element 'trees'.
# [Error] :50649:57: cvc-elt.4.2: Cannot resolve 'nex:ListTree' to a type definition for element 'tree'.
# [Error] :50649:57: cvc-type.2: The type definition cannot be abstract for element tree.
# [Error] :50649:57: cvc-complex-type.3.2.2: Attribute 'rooted' is not allowed to appear in element 'tree'.
# [Error] :50650:77: cvc-complex-type.2.4.a: Invalid content was found starting with element 'root'. One of '{dict, node}' is expected.
echo "  examples/tolsmall_nexml.xml"


echo "The following schema in the examples directory are not nexml instances:"
echo "  examples/mesquite.xml"

echo "They will be skipped. Other example files should validate..."
java/validator/validate_nexml.sh examples/TreeInfer.xml examples/characters.xml examples/nexml.xml examples/rest.xml examples/taxa.xml examples/trees.xml examples/verbose.xml 
