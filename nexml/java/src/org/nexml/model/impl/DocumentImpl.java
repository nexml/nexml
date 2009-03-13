package org.nexml.model.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<Matrix<?>> mMatrices = new ArrayList<Matrix<?>>();

	private List<TreeBlock> mTreeBlockList = new ArrayList<TreeBlock>();

	public DocumentImpl(org.w3c.dom.Document document) {
		super(document);
		setRootAttributes();
	}

	public DocumentImpl(org.w3c.dom.Document document, Element element) {
		super(document, element);

		List<Element> elements = getChildrenByTagName(document
				.getDocumentElement(), OTUsImpl.getTagNameClass());

		Map<String, OTUsImpl> originalOTUsIds = new HashMap<String, OTUsImpl>();

		for (Element thisElement : elements) {
			String originalOTUsId = thisElement.getAttribute("id");
			OTUsImpl otus = new OTUsImpl(document, thisElement);
			originalOTUsIds.put(originalOTUsId, otus);
			mOtusList.add(otus);
		}

		List<Element> treeBlockElements = getChildrenByTagName(document
				.getDocumentElement(), TreeBlockImpl.getTagNameClass());
		for (Element treeBlockElement : treeBlockElements) {
			TreeBlockImpl treeBlock = new TreeBlockImpl(document,
					treeBlockElement, originalOTUsIds.get(
							treeBlockElement.getAttribute("otus"))
							.getOriginalOTUIds());
			treeBlock.setOTUs(originalOTUsIds.get(treeBlock.getElement()
					.getAttribute("otus")));
			mTreeBlockList.add(treeBlock);
		}

		List<Element> charsBlockElements = getChildrenByTagName(document
				.getDocumentElement(), MatrixImpl.getTagNameClass());
		for (Element charsBlock : charsBlockElements) {
			Matrix matrix = null;
			String xsiType = charsBlock.getAttribute("xsi:type");
			xsiType = xsiType.replaceAll("Seqs", "Cells");
			charsBlock.setAttribute("xsi:type", xsiType);
			if (xsiType.indexOf("Continuous") > 0) {
				matrix = new ContinuousMatrixImpl(getDocument(), charsBlock, 
						originalOTUsIds.get(charsBlock.getAttribute("otus")));
			} else {
				matrix = new CategoricalMatrixImpl(getDocument(), charsBlock,
						originalOTUsIds.get(charsBlock.getAttribute("otus")));
			}
			mMatrices.add(matrix);

		}
	}

	private void setRootAttributes() {
		getElement().setAttribute("version", "0.8");
		getElement().setAttribute("generator", getClass().getName());
		getElement().setPrefix("nex");
		getElement().removeAttribute("id");
	}

	protected DocumentImpl() {
	}

	/**
	 * THis method creates an otus element and appends it to the document root.
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
		mMatrices.add(categoricalMatrix);
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
		mMatrices.add(continuousMatrix);
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
	 *            concrete subclass they implement (the xsi:type business). XXX
	 *            Here, this subclass is set to a molecular sequence type as
	 *            specified in param type. Hopefully we come up with a better
	 *            way to do this.
	 * 
	 * @author pmidford
	 */
	public MolecularMatrix createMolecularMatrix(OTUs otus, String type) {
		MolecularMatrixImpl molecularMatrix = new MolecularMatrixImpl(
				getDocument());
		getElement().appendChild(molecularMatrix.getElement());
		molecularMatrix.setOTUs(otus);
		molecularMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":" + type);
		return molecularMatrix;
	}

	public String getXmlString() {
		StringWriter stringWriter = new StringWriter();
		try {
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

	public List<OTUs> getOTUsList() {
		return mOtusList;
	}

	public List<Matrix<?>> getMatrices() {
		return mMatrices;
	}

	public List<TreeBlock> getTreeBlockList() {
		return mTreeBlockList;
	}

	public String getId() {
		return null;
	}

	public void setLabel(String label) {

	}

	public String getLabel() {
		return null;
	}
	
	private Map<String, OTUs> mOriginalOTUsIds = new HashMap<String, OTUs>();
	
	Map<String, OTUs> getOriginalOTUsIds() {
		return mOriginalOTUsIds;
	}

}
