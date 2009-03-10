package org.nexml.model.impl;

import org.nexml.model.Node;
import org.nexml.model.OTULinkable;

public class NodeImpl extends NexmlWritableImpl implements Node {

	@Override
	String getTagName() {
		return "node";
	}

}
