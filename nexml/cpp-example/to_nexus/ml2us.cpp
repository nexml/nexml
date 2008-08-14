#include <memory>
#include <iostream>

#include "nexml.hxx"

using namespace std;
using namespace NeXML;

class NexmlCharactersAdaptor
{
	public:
		virtual unsigned getNumChars() const = 0;
};

class NexmlCharactersAdaptorSeqs : public NexmlCharactersAdaptor
{
	public:
		NexmlCharactersAdaptorSeqs(const AbstractSeqs *p):raw(p){}
		virtual unsigned getNumChars() const {
			if (!(this->raw))
				return 0;
			const AbstractSeqMatrix & mat = raw->matrix();
			const AbstractSeqMatrix::row_sequence & s = mat.row();
			if (s.empty())
				return 0;
			const AbstractSeqRow & fr = *(s.begin());
			const DNAMatrixSeqRow * dsr = dynamic_cast<const DNAMatrixSeqRow *> (&fr);
			if (dsr) {
				const AbstractSeq & fr_seq = dsr->seq();
				
			}
				
			const AbstractSeqRow::seq_type & fr_seq = fr.seq();
			cout << "fr_seq is " << fr_seq << '\n';
			return 1;
		}
	protected:
		const AbstractSeqs * raw;
};

class NexmlCharactersAdaptorCells : public NexmlCharactersAdaptor
{
	public:
		NexmlCharactersAdaptorCells(const AbstractCells *p):raw(p){}
		virtual unsigned getNumChars() const {return 2;}
	protected:
		const AbstractCells * raw;
};

const NexmlCharactersAdaptor * createCharactersAdaptor( const AbstractBlock *);


const NexmlCharactersAdaptor * createCharactersAdaptor( const AbstractBlock * ab)
{
	if (!ab)
		return NULL;
	const AbstractSeqs * seqPtr = dynamic_cast<const AbstractSeqs *> (ab);
	if (seqPtr)
		return new NexmlCharactersAdaptorSeqs(seqPtr);
	const AbstractCells * cellsPtr = dynamic_cast<const AbstractCells *> (ab);
	if (cellsPtr)
		return new NexmlCharactersAdaptorCells(cellsPtr);
	return NULL;			
}


int main (int argc, char* argv[])
{
	if (argc != 2) {
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
			const NexmlCharactersAdaptor * adaptor = createCharactersAdaptor(&ab);
			if (adaptor)
				cout << adaptor->getNumChars() << " characters\n";
			else
				cout <<  "unadaptable characters\n";
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
