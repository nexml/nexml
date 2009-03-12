package org.nexml.model.impl;

import org.nexml.model.OTUs;
import org.nexml.model.OTUsLinkable;

abstract class OTUsLinkableImpl<T> extends SetManager<T> implements
		OTUsLinkable {

	private OTUs mOTUs;
	
	public OTUs getOTUs() {
		return mOTUs;
	}

	public void setOTUs(OTUs otus) {
		mOTUs = otus;
	}

}
