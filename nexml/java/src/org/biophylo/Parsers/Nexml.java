package org.biophylo.Parsers;
import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;
import java.util.*;
import org.biophylo.Util.*;
import org.biophylo.Taxa.*;
import org.biophylo.Forest.*;
import org.biophylo.Util.Exceptions.*;
import org.biophylo.*;
import org.biophylo.Matrices.*;
import org.biophylo.Matrices.Datatype.*;
import org.biophylo.Mediators.*;
public class Nexml implements Parsers {
	private static HashMap factory = null;
	private static ObjectMediator om = ObjectMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	
	public Nexml () {
		if ( factory == null ) {
			factory = new HashMap();
			factory.put("otus", org.biophylo.Taxa.Taxa.class);
			factory.put("otu", org.biophylo.Taxa.Taxon.class);
			factory.put("characters", org.biophylo.Matrices.Matrix.class);
			factory.put("row", org.biophylo.Matrices.Datum.class);
			factory.put("trees", org.biophylo.Forest.Forest.class);
			factory.put("tree", org.biophylo.Forest.Tree.class);
			factory.put("node", org.biophylo.Forest.Node.class);
		}
	}
	
	public Object[] parse(InputStream data) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document xmlDoc = null;
		Object[] theBlocks = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		try {			
			xmlDoc = db.parse(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try { 
			HashMap taxaBlocks = processOtus(xmlDoc.getElementsByTagName("otus"));
			Vector treeBlocks = processTrees(xmlDoc.getElementsByTagName("trees"));
			Vector charBlocks = processCharacters(xmlDoc.getElementsByTagName("characters"));
			resolveTreeTaxa(taxaBlocks,treeBlocks);
			resolveMatrixTaxa(taxaBlocks,charBlocks);
			int numBlocks = taxaBlocks.size();
			numBlocks += treeBlocks.size();
			numBlocks += charBlocks.size();
			Object[] blocks = new Object[numBlocks];
			Object[] taxa = taxaBlocks.values().toArray();
			for ( int i = 0; i < taxa.length; i++ ) {
				blocks[i] = taxa[i];
			}
			for ( int i = 0; i < treeBlocks.size(); i++ ) {
				blocks[i+taxa.length] = treeBlocks.get(i);
			}
			for ( int i = 0; i < charBlocks.size(); i++ ) {
				blocks[i+taxa.length+treeBlocks.size()] = charBlocks.get(i);
			}
			theBlocks = blocks;
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return theBlocks;
	}
	
	private static void resolveTreeTaxa(HashMap taxaBlocks,Vector treeBlocks){
		for ( int i = 0; i < treeBlocks.size(); i++ ) {
			String otusIdRef = (String)((Forest)treeBlocks.get(i)).getGeneric("otus");
			Taxa taxa = (Taxa)taxaBlocks.get(otusIdRef);
			((Forest)treeBlocks.get(i)).setTaxa(taxa);
			HashMap taxon = new HashMap();
			Containable[] taxonObj = taxa.getEntities();
			for ( int j = 0; j < taxonObj.length; j++ ) {
				taxon.put(taxonObj[j].getXmlId(), taxonObj[j]);
			}
			Containable[] trees = ((Forest)treeBlocks.get(i)).getEntities();
			for ( int j = 0; j < trees.length; j++ ) {
				Containable[] nodes = ((Listable)trees[j]).getEntities();
				for ( int k = 0; k < nodes.length; k++ ) {
					String otuIdRef = (String)nodes[k].getGeneric("otu");
					if ( otuIdRef != null ) {
						Taxon theTaxon = (Taxon)taxon.get(otuIdRef);
						if ( theTaxon != null ) {
							((TaxonLinker)nodes[k]).setTaxon(theTaxon);
						}
					}
				}
			}
		}
	}
	private static void resolveMatrixTaxa(HashMap taxaBlocks,Vector charBlocks){
		for ( int i = 0; i < charBlocks.size(); i++ ) {
			String otusIdRef = (String)((Matrix)charBlocks.get(i)).getGeneric("otus");
			Taxa taxa = (Taxa)taxaBlocks.get(otusIdRef);
			((Matrix)charBlocks.get(i)).setTaxa(taxa);
			HashMap taxon = new HashMap();
			Containable[] taxonObj = taxa.getEntities();
			for ( int j = 0; j < taxonObj.length; j++ ) {
				taxon.put(taxonObj[j].getXmlId(), taxonObj[j]);
			}
			Containable[] rows = ((Matrix)charBlocks.get(i)).getEntities();
			for ( int j = 0; j < rows.length; j++ ) {
				String otuIdRef = (String)rows[j].getGeneric("otu");
				if ( otuIdRef != null ) {
					Taxon theTaxon = (Taxon)taxon.get(otuIdRef);
					((TaxonLinker)rows[j]).setTaxon(theTaxon);
				}
			}
		}		
	}
	
	private static Vector processCharacters(NodeList charElts) throws ObjectMismatch {
		Vector charBlocks = new Vector();
		if ( charElts.getLength() == 0) return charBlocks;
		for ( int i = 0; i < charElts.getLength(); i++ ) {
			Matrix charObj = (Matrix)objFromElement((Element)charElts.item(i));
			String type = ((Element)charElts.item(i)).getAttribute("xsi:type");
			int start = type.indexOf(":");
			int finish = type.lastIndexOf("Seqs");
			if ( finish == -1 ) {
				finish = type.lastIndexOf("Cells");
			}
			type = type.substring(start + 1, finish);
			logger.info(type);
			Datatype to = Datatype.getInstance(type);
			charObj.setTypeObject(to);
			String otus = ((Element)charElts.item(i)).getAttribute("otus");
			charObj.setGeneric("otus", otus);
			HashMap stateSet = new HashMap();
			HashMap stateSetForChar = new HashMap();
			HashMap colIndices = new HashMap();
			HashMap symbolOfId = new HashMap();
			if ( ((Element)charElts.item(i)).getElementsByTagName("format").getLength() > 0 ) {
				Element formatElt = (Element)((Element)charElts.item(i)).getElementsByTagName("format").item(0);
				NodeList statesElts = formatElt.getElementsByTagName("states");
				for ( int j = 0; j < statesElts.getLength(); j++ ) {
					String id = ((Element)statesElts.item(j)).getAttribute("id");
					HashMap lookup = new HashMap();					
					HashMap innerSet = new HashMap();
					resolveMapping(((Element)statesElts.item(j)).getElementsByTagName("state"),symbolOfId,lookup);
					resolveMapping(((Element)statesElts.item(j)).getElementsByTagName("polymorphic_state_set"),symbolOfId,lookup);
					resolveMapping(((Element)statesElts.item(j)).getElementsByTagName("uncertain_state_set"),symbolOfId,lookup);
					innerSet.put("lookup", lookup);
					innerSet.put("symbols", symbolOfId);
					stateSet.put(id, innerSet);
					charObj.setLookup(lookup);
				}
				if ( ((Element)charElts.item(i)).getElementsByTagName("char").getLength() > 0 ) {
					NodeList colElts = ((Element)charElts.item(i)).getElementsByTagName("char");
					for ( int j = 0; j < colElts.getLength(); j++ ) {
						String id = ((Element)colElts.item(j)).getAttribute("id");
						String states = ((Element)colElts.item(j)).getAttribute("states");
						stateSetForChar.put(id, states);
						colIndices.put(id, new Integer(j));
					}
				}				
			}
			Element matrixElt = (Element)((Element)charElts.item(i)).getElementsByTagName("matrix").item(0);
			processMatrix(matrixElt,symbolOfId,colIndices,charObj);
			charBlocks.add(charObj);
		}
		return charBlocks;
	}
	
	private static void processMatrix(Element matrixElt,HashMap symbolOfId,HashMap colIndices,Matrix charObj) throws ObjectMismatch {
		NodeList row = matrixElt.getElementsByTagName("row");
		for ( int i = 0; i < row.getLength(); i++ ) {
			Datum datum = (Datum)objFromElement((Element)row.item(i));
			String otuId = ((Element)row.item(i)).getAttribute("otu");
			datum.setGeneric("otu", otuId);
			datum.setTypeObject(charObj.getTypeObject());
			String[] chars = new String[colIndices.size()];
			charObj.insert(datum);
			NodeList cell = ((Element)row.item(i)).getElementsByTagName("cell");
			if ( cell.getLength() != 0 ) {
				for ( int j = 0; j < cell.getLength(); j++ ) {
					String charId = ((Element)cell.item(j)).getAttribute("char");
					String stateId = ((Element)cell.item(j)).getAttribute("state");
					int charIndex;
					String state;
					if ( colIndices.containsKey(charId) ) {
						charIndex = ((Integer)colIndices.get(charId)).intValue();
					}
					else {
						charIndex = Integer.parseInt(charId);
					}
					if ( symbolOfId.containsKey(stateId) ) {
						state = (String)symbolOfId.get(stateId);
					}
					else {
						state = stateId;
					}
					chars[charIndex] = state;
				}
				String missing = ""+charObj.getMissing();
				for ( int j = 0; j < chars.length; j++ ) {
					if ( chars[j] == null ) {
						chars[j] = missing;
					}
				}
				datum.insert(chars);
			}
			else {
				Element seq = (Element)((Element)row.item(i)).getElementsByTagName("seq").item(0);
				datum.insert(seq.getTextContent());			
			}			
		}		
	}
	
	private static void resolveMapping(NodeList elts,HashMap symbolOfId,HashMap lookup) {
		for ( int i = 0; i < elts.getLength(); i++ ) {
			String symbol = ((Element)elts.item(i)).getAttribute("symbol");
			String id = ((Element)elts.item(i)).getAttribute("id");
			symbolOfId.put(id, symbol);
			NodeList mappings = ((Element)elts.item(i)).getElementsByTagName("mapping");
			Vector symlist = new Vector();
			if ( mappings.getLength() > 0 ) {
				for ( int j = 0; j < mappings.getLength(); j++ ) {
					String refId = ((Element)mappings.item(j)).getAttribute("state");
					String refSym = (String)symbolOfId.get(refId);
					symlist.add(refSym);
				}
			}
			else {
				symlist.add(symbol);				
			}
			lookup.put(symbol, symlist);
		}
	}
	
	private static Tree processTree(Element treeElt) throws ObjectMismatch {
		Tree treeObj = (Tree)objFromElement(treeElt);
		HashMap sourceOf = new HashMap();
		HashMap targetOf = new HashMap();
		HashMap lengthOf = new HashMap();
		HashMap otuOf = new HashMap();
		HashMap nodeById = new HashMap();
		NodeList nodeElts = treeElt.getElementsByTagName("node");
		for ( int i = 0; i < nodeElts.getLength(); i++ ) {
			org.biophylo.Forest.Node nodeObj = (org.biophylo.Forest.Node)objFromElement((Element)nodeElts.item(i));
			String id = nodeObj.getXmlId();
			treeObj.insert(nodeObj);
			nodeById.put(id, nodeObj);
			String otuId = ((Element)nodeElts.item(i)).getAttribute("otu");
			if ( otuId != null ) {
				nodeObj.setGeneric("otu",otuId);
			}
		}
		NodeList edgeElts = treeElt.getElementsByTagName("edge");
		for ( int i = 0; i < edgeElts.getLength(); i++ ) {
			String target = ((Element)edgeElts.item(i)).getAttribute("target");
			String source = ((Element)edgeElts.item(i)).getAttribute("source");
			String length = ((Element)edgeElts.item(i)).getAttribute("length");
			sourceOf.put(target, source);
			targetOf.put(source, target);
			if ( length != null ) {
				lengthOf.put(target, length);
			}
		}
		Containable[] entities = treeObj.getEntities();
		for ( int i = 0; i < entities.length; i++ ) {
			String id = entities[i].getXmlId();
			if ( sourceOf.containsKey(id) ) {
				org.biophylo.Forest.Node parentNode = (org.biophylo.Forest.Node)nodeById.get((String)sourceOf.get(id));
				((org.biophylo.Forest.Node)entities[i]).setParent(parentNode);
				parentNode.setChild((org.biophylo.Forest.Node)entities[i]);
			}
			if ( lengthOf.containsKey(id) ) {
				((org.biophylo.Forest.Node)entities[i]).setBranchLength(Double.parseDouble((String)lengthOf.get(id)));
			}
			if ( otuOf.containsKey(id) ) {
				((org.biophylo.Forest.Node)entities[i]).setGeneric("otu",(String)otuOf.get(id));
			}
		}
		if ( treeElt.getElementsByTagName("rootedge").getLength() > 0 ) {
			Element rootEdge = (Element)treeElt.getElementsByTagName("rootedge").item(0);
			String target = rootEdge.getAttribute("target");
			String bl = rootEdge.getAttribute("length");
			((org.biophylo.Forest.Node)nodeById.get(target)).setBranchLength(Double.parseDouble(bl));
		}
		return treeObj;
	}
	
	private static Vector processTrees(NodeList treesElts) throws ObjectMismatch {
		Vector result = new Vector();
		if(treesElts==null) return result;
		for ( int i = 0; i < treesElts.getLength(); i++ ) {
			Forest forestObj = (Forest)objFromElement((Element)treesElts.item(i));
			String otusId = ((Element)treesElts.item(i)).getAttribute("otus");
			forestObj.setGeneric("otus", otusId);
			NodeList treeElts = ((Element)treesElts.item(i)).getElementsByTagName("tree");
			for ( int j = 0; j < treeElts.getLength(); j++ ) {
				Tree tree = processTree((Element)treeElts.item(j));
				forestObj.insert(tree);
			}
			result.add(forestObj);
		}		
		return result;
	}
	
	private static HashMap processOtus(NodeList otusElts) {
		HashMap taxaBlocks = new HashMap();
		for ( int i = 0; i < otusElts.getLength(); i++ ) {
			Taxa taxa = (Taxa)objFromElement((Element)otusElts.item(i));
			NodeList otuElts = ((Element)otusElts.item(i)).getElementsByTagName("otu");
			for ( int j = 0; j < otuElts.getLength(); j ++ ) {
				Taxon taxon = (Taxon)objFromElement((Element)otuElts.item(j));
				try { 
					taxa.insert(taxon);
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			taxaBlocks.put(taxa.getXmlId(), taxa);
		}
		return taxaBlocks;
	}
	
	private static XMLWritable objFromElement(Element elt) {
		String tagName = elt.getTagName();
		XMLWritable obj = null;
		try {
			obj = (XMLWritable)((Class)factory.get(tagName)).newInstance();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		obj.setXmlId(elt.getAttribute("id"));
		obj.setName(elt.getAttribute("label"));
		NodeList children = elt.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ ) {
			if ( children.item(i).getNodeType() == 1 && children.item(i).getNodeName().equals("dict") ) {
				HashMap dict = parseDict((Element)children.item(i));
				obj.setGeneric("dict", dict);
			}
		}
		return obj;
	}
	
	private static HashMap parseDict(Element elt) {
		HashMap result = new HashMap();
		NodeList children = elt.getChildNodes();
		for ( int i = 0; i < children.getLength(); i++ ) {
			if ( children.item(i).getNodeType() == 1 && children.item(i).getNodeName().equals("key") ) {
				String key = children.item(i).getTextContent();
				Element valueElt = null;
				VALUE: for ( int j = i+1; j < children.getLength(); j++ ) {
					if ( children.item(j).getNodeType() == 1 ) {
						valueElt = (Element)children.item(j);
						break VALUE;
					}
				}
				if ( valueElt.getTagName().equals("dict") ) {
					result.put(key, parseDict(valueElt));
				}
				else if ( valueElt.getTagName().indexOf("vector") > 0 ) {
					Vector value = new Vector();
					value.add(valueElt.getTagName());
					String[] valueArray = valueElt.getTextContent().split(" ");
					for ( int k = 0; k < valueArray.length; k++ ) {
						value.add(valueArray[i]);
					}
					result.put(key, value);
				}
				else if ( valueElt.getTagName().equals("any") ) {
					Vector value = new Vector();
					value.add("any");
					value.add(valueElt);
					result.put(key, value);
				}
				else {
					Vector value = new Vector();
					value.add(valueElt.getTagName());
					value.add(valueElt.getTextContent());
					result.put(key, value);
				}
			}
		}		
		return result;
	}

}
