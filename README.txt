================================================================================
General layout
================================================================================
The repository has the following structure:
/examples     - nexml instance documents that should validate (i.e. testable)
/java         - xerces-j validator, generic class libs, mesquite code
/perl         - Bio::Phylo parser code
/soap         - experimental nexml schema inclusions in wsdl
/xsd          - schema files
/xslt         - experimental style sheets, including schematron
/experimental - experimental (invalid) instance documents
/html         - documents for the website

================================================================================
Third party tools
================================================================================
Java validation code (in java/validator subdirectory) relies on the Apache 
Xerces jars.  See java/validator/jars/LICENSE for the licensing information for
that code.  (That validation code is useful to developers, but does not have to
be distributed end-users, so that licensing information does not have to be 
bundled with end-user distributions)
