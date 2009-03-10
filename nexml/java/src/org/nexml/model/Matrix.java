package org.nexml.model;

import java.util.List;

public interface Matrix extends Segmented {
	List<MatrixCell> getRow(OTU otu);
}
