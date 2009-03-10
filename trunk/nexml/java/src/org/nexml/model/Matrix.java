package org.nexml.model;

import java.util.List;

public interface Matrix extends NexmlWritable {
	List<MatrixCell> getRow(OTU otu);
	List<MatrixCell> getColumn(Character character);
}
