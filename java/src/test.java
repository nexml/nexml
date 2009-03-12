import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.CharacterState;
import org.nexml.model.Character;
import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;
import org.nexml.model.FloatEdge;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;
import org.nexml.model.UncertainCharacterState;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Document document = null;
		try {
			document = DocumentFactory.createDocument();
		} catch ( Exception e ) {
			e.printStackTrace();
		}			
		OTUs otus = document.createOTUs();
		otus.setLabel("bar");
		OTU otu = otus.createOTU();
		otu.setLabel("foo");
		TreeBlock treeBlock = document.createTreeBlock(otus);
		Tree<FloatEdge> tree = treeBlock.createFloatTree();
		tree.setLabel("baz");
		Node source = tree.createNode();
		source.setOTU(otu);
		Node target = tree.createNode();
		FloatEdge edge = (FloatEdge)tree.createEdge(source,target);
		edge.setLength(0.2342);
		CategoricalMatrix categoricalMatrix = document.createCategoricalMatrix(otus);
		CharacterStateSet characterStateSet = categoricalMatrix.createCharacterStateSet();
		CharacterState characterState = characterStateSet.createCharacterState(1);
		Set<CharacterState> members = new HashSet<CharacterState>();
		members.add(characterState);
		UncertainCharacterState uncertain = characterStateSet.createUncertainCharacterState(2, members);
		Character character1 = categoricalMatrix.createCharacter(characterStateSet);
		Character character2 = categoricalMatrix.createCharacter(characterStateSet);
		categoricalMatrix.getCell(otu, character1).setValue(characterState);
		categoricalMatrix.getCell(otu, character2).setValue(uncertain);
		System.out.println(document.getXmlString());
	}	
}
