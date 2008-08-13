#include <memory>
#include <iostream>

#include "nexml.hxx"

using namespace std;
using namespace NeXML;

int
main (int argc, char* argv[]) {
	if (argc != 2)
		{
		cerr << "Expecting one command line argument (the path to a nexml file)" << endl;
		return 1;
		}
	try {
		// get the root element of the file in argv[1], the root is compexType Nexml
		auto_ptr<Nexml> root (nexml (argv[1]));
		
		// get the characters elements
		Nexml::characters_sequence& cs (root->characters ());
	
		for ( Nexml::characters_iterator i (cs.begin ()); i != cs.end (); ++i ) {
		  // characters elements all inherit from complexType AbstractBlock
		  AbstractBlock& ab (*i);
		
		  // now try to cast to one of the concrete subclasses
		  if (RestrictionSeqs* rs = dynamic_cast<RestrictionSeqs*> (&ab)) {
			cout << *rs << endl;
		  }
		  else if (ContinuousSeqs* cs = dynamic_cast<ContinuousSeqs*> (&ab)) {
			cout << *cs << endl;
		  }
		  else if (StandardSeqs* ss = dynamic_cast<StandardSeqs*> (&ab)) {
			cout << *ss << endl;
		  }
		  else if (StandardCells* sc = dynamic_cast<StandardCells*> (&ab)) {
			cout << *sc << endl;
		  }
		  else if (ContinuousCells* cc = dynamic_cast<ContinuousCells*> (&ab)) {
			cout << *cc << endl;
		  }
		  else if (DnaSeqs* ds = dynamic_cast<DnaSeqs*> (&ab)) {
			cout << *ds << endl;
		  }
		  else if (RnaSeqs* rs = dynamic_cast<RnaSeqs*> (&ab)) {
			cout << *rs << endl;
		  }
		  else {
			// Print the base type.
			//
			cout << ab << endl;
		  }
		}
	}
	catch (const xml_schema::exception& e) {
		cerr << e << endl;
		return 1;
	}
	return 0;
}
