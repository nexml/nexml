package org.nexml.model;

public interface TreeBlock extends OTUsLinkable {
	Network createNetwork();

	Tree createTree();
}
