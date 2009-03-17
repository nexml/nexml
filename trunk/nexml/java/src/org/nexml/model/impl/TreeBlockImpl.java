package org.nexml.model.impl;

import java.util.Iterator;
import org.nexml.model.FloatEdge;
import org.nexml.model.IntEdge;
import org.nexml.model.Network;
import org.nexml.model.OTUs;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TreeBlockImpl extends OTUsLinkableImpl<Network<?>> implements
		TreeBlock {
	static String getTagNameClass() {
		return "trees";
	}

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
	protected TreeBlockImpl(Document document) {
		super(document);
	}

	protected Network<IntEdge> createIntNetwork(Element element) {
		return new IntNetworkImpl(getDocument(),element,(OTUsImpl)getOTUs());
	}
	
    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected TreeBlockImpl(Document document, Element item, OTUs otus) {
		super(document, item);
		setOTUs(otus);
		for ( Element networkElement : getChildrenByTagName(item,NetworkImpl.getTagNameClass())) {
			if ( networkElement.getAttribute(XSI_PREFIX + ":type").indexOf("Int") > 0 ) {
				addThing(new IntNetworkImpl(document,networkElement,(OTUsImpl)otus));
			}
			else {
				addThing(new FloatNetworkImpl(document,networkElement,(OTUsImpl)otus));
			}
		}
		for ( Element treeElement : getChildrenByTagName(item,TreeImpl.getTagNameClass())) {
			if ( treeElement.getAttribute(XSI_PREFIX + ":type").indexOf("Int") > 0 ) {
				addThing(new IntTreeImpl(document,treeElement,(OTUsImpl)otus));
			}
			else {
				addThing(new FloatTreeImpl(document,treeElement,(OTUsImpl)otus));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.TreeBlock#createIntNetwork()
	 */
	public Network<IntEdge> createIntNetwork() {
		IntNetworkImpl network = new IntNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":IntNetwork");
		return network;
	}

	/**
	 * This method creates a network with edge lengths which are floats. Here we
	 * also create a tree element, and set its xsi:type attribute to
	 * nex:FloatNetwork
	 * 
	 * @author rvosa
	 */
	public Network<FloatEdge> createFloatNetwork() {
		FloatNetworkImpl network = new FloatNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":FloatNetwork");
		return network;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	public String getTagName() {
		return getTagNameClass();
	}

	/**
	 * This method creates a tree with edge lengths which are floats. Here we
	 * also create a tree element, and set its xsi:type attribute to
	 * nex:FloatTree
	 * 
	 * @author rvosa
	 */
	public Tree<FloatEdge> createFloatTree() {
		FloatTreeImpl tree = new FloatTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":FloatTree");
		return tree;
	}

	/**
	 * This method creates a tree with edge lengths which are ints. Here we also
	 * create a tree element, and set its xsi:type attribute to nex:IntTree
	 * 
	 * @author rvosa
	 */
	public Tree<IntEdge> createIntTree() {
		IntTreeImpl tree = new IntTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":IntTree");
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Network<?>> iterator() {
		return getThings().iterator();
	}
}
