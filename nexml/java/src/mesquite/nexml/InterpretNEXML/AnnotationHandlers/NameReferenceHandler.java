/**
 * 
 */
package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import mesquite.lib.Associable;
import mesquite.lib.Listable;
import mesquite.lib.NameReference;
import mesquite.nexml.InterpretNEXML.Constants;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

/**
 * @author rvosa
 *
 */
public class NameReferenceHandler extends NamespaceHandler {
	private Annotatable mSubject;
	private Object mValue;
	private String mPredicate;
	
	/**
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public NameReferenceHandler(Annotatable annotatable,Annotation annotation) {
		super(annotatable, annotation);
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getSubject()
	 */
	@Override
	public
	Annotatable getSubject() {
		return mSubject;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setSubject(java.lang.Object)
	 */
	@Override
	public
	void setSubject(Annotatable subject) {
		mSubject = subject;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getValue()
	 */
	@Override
	public
	Object getValue() {
		return mValue;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setValue(java.lang.Object)
	 */
	@Override
	public
	void setValue(Object value) {
		mValue = value;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getPrefix()
	 */
	@Override
	public
	String getPrefix() {
		return Constants.NRPrefix;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setPrefix(java.lang.String)
	 */
	@Override
	public
	void setPrefix(String prefix) {
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getPredicate()
	 */
	@Override
	public
	String getPredicate() {
		return mPredicate;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setPredicate(java.lang.String)
	 */
	@Override
	public
	void setPredicate(String predicate) {
		mPredicate = predicate;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getPropertyIsRel()
	 */
	@Override
	public
	boolean getPropertyIsRel() {
		return false;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setPropertyIsRel(boolean)
	 */
	@Override
	public
	void setPropertyIsRel(boolean propertyIsRel) {

	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#getURIString()
	 */
	@Override
	public
	String getURIString() {
		return Constants.NRURIString;
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#setURIString(java.lang.String)
	 */
	@Override
	public
	void setURIString(String uri) {
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.PredicateHandler#handle(mesquite.lib.Associable, mesquite.lib.Listable, int)
	 */
	@Override
	public
	void read(Associable associable, Listable listable, int index) {
		Object value = getValue();
		String predicate = getPredicate();
		String[] parts = predicate.split(":");
		String local = parts[1];
		NameReference nRef = new NameReference(local);
		if ( value instanceof Boolean ) {
			associable.setAssociatedBit(nRef, index, (Boolean)value);
		}
		else if ( value instanceof Double ) {
			associable.setAssociatedDouble(nRef, index, (Double)value);
		}
		else if ( value instanceof Long ) {
			associable.setAssociatedLong(nRef, index, (Long)value);
		}
		else {
			associable.setAssociatedObject(nRef, index, value);
		}	
		System.out.println("NameReferenceHandler for predicate "+local+" with value "+value);
	}

	@Override
	public
	void write() {
		// TODO Auto-generated method stub
		
	}

}
