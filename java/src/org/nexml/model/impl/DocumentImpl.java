package org.nexml.model.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.Document;
import org.nexml.model.Matrix;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Element;

public class DocumentImpl extends AnnotatableImpl implements Document {
	private List<OTUs> mOtusList = new ArrayList<OTUs>();
	private List<Matrix<?>> mMatrixList = new ArrayList<Matrix<?>>();
	private List<TreeBlock> mTreeBlockList = new ArrayList<TreeBlock>();
	public static Collection<String> characterNames = new ArrayList<String>();

    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */
	public DocumentImpl(org.w3c.dom.Document document) {
		super(document);
		setRootAttributes();
	}

    /**
     * This is the only public constructor that takes an element as
     * its argument. This is so that we can start the recursive
     * element traversal from outside this packages, e.g. in the
     * DocumentFactory.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the <nex:nexml/> root element
     * @author rvosa
     */
	public DocumentImpl(org.w3c.dom.Document document, Element element) {
		super(document, element);
		if ( ! element.getOwnerDocument().equals(document) ) {
			throw new RuntimeException("This'll never work");
		}		

		List<Element> oTUsElements = getChildrenByTagName(element, OTUsImpl.getTagNameClass());

		for (Element thisElement : oTUsElements) {
			OTUsImpl otus = new OTUsImpl(document, thisElement);
			mOtusList.add(otus);
		}

		List<Element> treeBlockElements = getChildrenByTagName(element, TreeBlockImpl.getTagNameClass());
		for (Element treeBlockElement : treeBlockElements) {
			String oTUsId = treeBlockElement.getAttribute("otus");			
			TreeBlockImpl treeBlock = new TreeBlockImpl(document,treeBlockElement,getOTUsById(oTUsId));
			mTreeBlockList.add(treeBlock);
		}

		characterNames.clear();
		List<Element> charsBlockElements = getChildrenByTagName(document
				.getDocumentElement(), MatrixImpl.getTagNameClass());
		for (Element charsBlock : charsBlockElements) {
			
			List<Element> formatElements = getChildrenByTagName(charsBlock, "format");
			for (Element thisE : formatElements) {
				List<Element> charElements = getChildrenByTagName(thisE, "char");
				for (Element charElement : charElements){
					String charLabel = charElement.getAttribute("label").trim();
					characterNames.add(charLabel.trim());
				}
			}
			
			
			String xsiType = charsBlock.getAttribute(XSI_PREFIX+":type");
			Matrix<?> matrix = null;
			xsiType = xsiType.replaceAll("Seqs", "Cells");
			charsBlock.setAttribute(XSI_PREFIX+":type", xsiType);
			if (xsiType.indexOf("Continuous") > 0) {
				matrix = new ContinuousMatrixImpl(getDocument(), charsBlock, 
					(OTUsImpl)getOTUsById(charsBlock.getAttribute("otus")));
			} 
			else if ( xsiType.indexOf(MolecularMatrixImpl.DNA) > 0 ) {
				matrix = new MolecularMatrixImpl(getDocument(), charsBlock,
					(OTUsImpl)getOTUsById(charsBlock.getAttribute("otus")),
					MolecularMatrixImpl.DNA
					);
			}
			else if ( xsiType.indexOf(MolecularMatrixImpl.RNA) > 0 ) {
				matrix = new MolecularMatrixImpl(getDocument(), charsBlock,
					(OTUsImpl)getOTUsById(charsBlock.getAttribute("otus")),
					MolecularMatrixImpl.RNA
					);
			}		
			else if ( xsiType.indexOf(MolecularMatrixImpl.Protein) > 0 ) {
				matrix = new MolecularMatrixImpl(getDocument(), charsBlock,
					(OTUsImpl)getOTUsById(charsBlock.getAttribute("otus")),
					MolecularMatrixImpl.Protein
					);
			}			
			else {
				matrix = new CategoricalMatrixImpl(getDocument(), charsBlock,
					(OTUsImpl)getOTUsById(charsBlock.getAttribute("otus")));				
			}
			mMatrixList.add(matrix);

		}
	}

	private void setRootAttributes() {
		getElement().setAttribute("version", "0.8");
		getElement().setAttribute("generator", getClass().getName());
		getElement().setPrefix(NEX_PREFIX);
		getElement().removeAttribute("id");
		getElement().setAttributeNS(
			"http://www.w3.org/2000/xmlns/", 
			"xmlns:" + NEX_PREFIX, 
			DEFAULT_NAMESPACE
		);
		getElement().setAttribute("xmlns", DEFAULT_NAMESPACE);
		getElement().setAttributeNS(
			"http://www.w3.org/2000/xmlns/",
			"xmlns:xsi",
			"http://www.w3.org/1999/XMLSchema-instance"
		);
		getElement().setAttributeNS(
			"http://www.w3.org/2000/xmlns/",
			"xmlns:xsd",			
			"http://www.w3.org/2001/XMLSchema#"
		);
		getElement().setAttributeNS(
			"http://www.w3.org/2000/xmlns/",
			"xmlns:rdf",			
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		);
	}

	protected DocumentImpl() {
	}

	/**
	 * This method creates an otus element and appends it to the document root.
	 * 
	 * @author rvosa
	 */
	public OTUs createOTUs() {
		OTUsImpl otus = new OTUsImpl(getDocument());
		mOtusList.add(otus);
		getElement().appendChild(otus.getElement());
		return otus;
	}

	/**
	 * This method creates a trees element and appends it to the document root.
	 * Because NeXML requires that trees elements have an id reference attribute
	 * to specify the otus element it refers to, the equivalent OTUs object
	 * needs to be passed in here.
	 * 
	 * @author rvosa
	 */
	public TreeBlock createTreeBlock(OTUs otus) {
		TreeBlockImpl treeBlock = new TreeBlockImpl(getDocument());
		mTreeBlockList.add(treeBlock);
		getElement().appendChild(treeBlock.getElement());
		treeBlock.setOTUs(otus);
		return treeBlock;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return "nexml";
	}

	/**
	 * This method creates the characters element and appends it to the document
	 * root. Because NeXML requires that characters elements have an id
	 * reference attribute to specify the otus element it refers to, the
	 * equivalent OTUs object needs to be passed in here. In addition,
	 * characters elements need to specify the concrete subclass they implement
	 * (the xsi:type business). XXX Here, this subclass is set to StandardCells.
	 * Hopefully we come up with a better way to do this.
	 * 
	 * @author rvosa
	 */
	public CategoricalMatrix createCategoricalMatrix(OTUs otus) {
		CategoricalMatrixImpl categoricalMatrix = new CategoricalMatrixImpl(
				getDocument());
		mMatrixList.add(categoricalMatrix);
		getElement().appendChild(categoricalMatrix.getElement());
		categoricalMatrix.setOTUs(otus);
		categoricalMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":StandardCells");
		return categoricalMatrix;
	}

	/**
	 * This method creates the characters element and appends it to the document
	 * root. Because NeXML requires that characters elements have an id
	 * reference attribute to specify the otus element it refers to, the
	 * equivalent OTUs object needs to be passed in here. In addition,
	 * characters elements need to specify the concrete subclass they implement
	 * (the xsi:type business). XXX Here, this subclass is set to
	 * ContinuousCells. Hopefully we come up with a better way to do this.
	 * 
	 * @author rvosa
	 */
	public ContinuousMatrix createContinuousMatrix(OTUs otus) {
		ContinuousMatrixImpl continuousMatrix = new ContinuousMatrixImpl(
				getDocument());
		mMatrixList.add(continuousMatrix);
		getElement().appendChild(continuousMatrix.getElement());
		continuousMatrix.setOTUs(otus);
		continuousMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":ContinuousCells");
		return continuousMatrix;
	}

	/**
	 * @param otus
	 * @param type specifies the molecular sequence type (Dna,Rna,Protein) This
	 *            method creates the characters element and appends it to the
	 *            document root. Because NeXML requires that characters elements
	 *            have an id reference attribute to specify the otus element it
	 *            refers to, the equivalent OTUs object needs to be passed in
	 *            here. In addition, characters elements need to specify the
	 *            concrete subclass they implement (the xsi:type business). 
	 *            XXX Here, this subclass is set to a molecular sequence type as
	 *            specified in param type. Hopefully we come up with a better
	 *            way to do this.
	 * 
	 * @author pmidford
	 */
	public MolecularMatrix createMolecularMatrix(OTUs otus, String type) {
		MolecularMatrixImpl molecularMatrix = new MolecularMatrixImpl(
				getDocument());
		mMatrixList.add(molecularMatrix);
		getElement().appendChild(molecularMatrix.getElement());
		molecularMatrix.setOTUs(otus);
		molecularMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":" + type + "Cells");
		return molecularMatrix;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Document#getXmlString()
	 */
	public String getXmlString() {
		StringWriter stringWriter = new StringWriter();
		try {
			getDocument().normalizeDocument();
			Source source = new DOMSource(getElement());
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return stringWriter.getBuffer().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Document#getOTUsList()
	 */
	public List<OTUs> getOTUsList() {
		return mOtusList;
	}
	
	protected OTUs getOTUsById(String id) {
		if ( null == id ) {
			return null;
		}
		for ( OTUs otus : getOTUsList() ) {
			if ( ((OTUsImpl)otus).getId().equals(id) ) {
				return otus;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Document#getMatrices()
	 */
	public List<Matrix<?>> getMatrices() {
		return mMatrixList;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Document#getTreeBlockList()
	 */
	public List<TreeBlock> getTreeBlockList() {
		return mTreeBlockList;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getId()
	 */
	public String getId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {

	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getLabel()
	 */
	public String getLabel() {
		return null;
	}

}
