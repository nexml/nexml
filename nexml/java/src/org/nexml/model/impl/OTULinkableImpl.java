package org.nexml.model.impl;

import org.nexml.model.OTU;
import org.nexml.model.OTULinkable;

abstract class OTULinkableImpl extends NexmlWritableImpl implements
		OTULinkable {
	
	private OTU mOTU;

	public OTU getOTU() {
		return mOTU;
	}

	public void setOTU(OTU otu) {
		mOTU = otu;
	}
}
