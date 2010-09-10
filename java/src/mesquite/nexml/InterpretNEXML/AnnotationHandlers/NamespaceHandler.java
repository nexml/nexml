/**
 * 
 */
package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

/**
 * @author rvosa
 *
 */
public abstract class NamespaceHandler extends PredicateHandler {

	/**
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public NamespaceHandler(Annotatable annotatable,Annotation annotation) {
		super(annotatable, annotation);
	}

}
