package org.nexml.model;

public interface Edge extends NexmlWritable {
	Node getSource();

	void setSource(Node source);

	Node getTarget();

	void setTarget(Node target);
}
