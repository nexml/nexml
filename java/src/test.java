import org.biophylo.*;
import org.biophylo.Util.*;
import org.biophylo.Taxa.*;
import org.biophylo.Forest.*;
import org.biophylo.Matrices.*;
import java.io.*;
import java.util.*;
public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {						
		Logger logger = Logger.getInstance();
		logger.VERBOSE(2);
		try {
			Project proj = new Project();
			Taxa taxa = new Taxa();
			Forest forest = new Forest();
			Tree tre = new Tree();
			Node root = new Node();
			HashMap dict = new HashMap();
			Vector value = new Vector();
			value.add("string");
			value.add("This is a tree");
			dict.put("description", value);
			tre.setGeneric("dict", dict);
			tre.insert(root);
			forest.insert(tre);
			forest.setTaxa(taxa);
			proj.insert(taxa);
			proj.insert(forest);
			Matrix matrix = new Matrix("Standard");
			matrix.setTaxa(taxa);
			proj.insert(matrix);
			int max = 100;
			StringBuffer sb = new StringBuffer();
			for ( int i = 0; i < max; i++ ) {
				sb.append(1);
			}
			for ( int i = 0; i < max; i++ ) {								
				Taxon taxon = new Taxon();
				Datum datum = new Datum("Standard");
				datum.setTaxon(taxon);
				datum.insert(sb.toString());
				matrix.insert(datum);
				Node node = new Node();
				node.setTaxon(taxon);
				root.setChild(node);
				node.setParent(root);
				node.setName("n"+i);
				node.setBranchLength(1);
				tre.insert(node);
				taxa.insert(taxon);
			}
			System.out.println(proj.toXml());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	
	
}
