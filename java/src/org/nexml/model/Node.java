package org.nexml.model;

public interface Node extends NetworkObject, OTULinkable { 
	boolean isRoot();
	void setRoot(boolean isRoot);
}
