package org.nexml.model;

public interface Edge extends NetworkObject {
	Node getSource();

	void setSource(Node source);

	Node getTarget();

	void setTarget(Node target);
}
