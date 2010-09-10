package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import mesquite.lib.Associable;
import mesquite.lib.Listable;
import mesquite.lib.NameReference;
import mesquite.nexml.InterpretNEXML.Constants;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

public class PredicateHandlerImpl extends PredicateHandler {
	private String mPredicate;
	private boolean mPropertyIsRel;
	private Object mValue;
	private String mProperty;
	private Annotatable mSubject;

	public PredicateHandlerImpl(Annotatable annotatable, Annotation annotation) {
		super(annotatable, annotation);
	}
	
	@Override
	public
	void setPredicate(String predicate) {
		mPredicate = predicate;
	}	

	@Override
	public
	String getPredicate() {
		return mPredicate;
	}

	@Override
	public
	void setPrefix(String prefix) {
	}		
	
	@Override
	public
	String getPrefix() {
		return Constants.MESQUITE_NS_PREFIX;
	}
	
	@Override
	public
	void setPropertyIsRel(boolean propertyIsRel) {
		mPropertyIsRel = propertyIsRel;		
	}	


	@Override
	public
	boolean getPropertyIsRel() {
		return mPropertyIsRel;
	}	
	

	@Override
	public
	String getURIString() {
		return Constants.MESQUITE_NS_BASE;
	}

	@Override
	public
	Object getValue() {
		return mValue;
	}
	
	@Override
	public
	void setValue(Object value) {
		mValue = value;
	}

	public String getRel() {
		return getProperty();
	}

	public void setRel(String rel) {
		setProperty(rel); 
		setPropertyIsRel(true);		
	}

	public String getProperty() {
		if ( null != mProperty ) {
			return mProperty;
		}
		else {
			return getPrefix() + ":" + getPredicate();
		}
	}

	public void setProperty(String property) {
		mProperty = property;
	}

	public Annotatable getSubject() {
		return mSubject;
	}

	public void setSubject(Annotatable subject) {
		mSubject = subject;
	}

	@Override
	public
	void setURIString(String uri) {		
	}

	@Override
	public
	void read(Associable mesAssociable, Listable listable, int index) {
		Object convertedValue = getValue();			
		if ( convertedValue instanceof Boolean ) {
			NameReference mesNr = mesAssociable.makeAssociatedBits(getPredicate());
			mesNr.setNamespace(getURI());
			mesAssociable.setAssociatedBit(mesNr,index,(Boolean)convertedValue);
		}
		else if ( convertedValue instanceof Double ) {
			NameReference mesNr = mesAssociable.makeAssociatedDoubles(getPredicate());
			mesNr.setNamespace(getURI());
			mesAssociable.setAssociatedDouble(mesNr,index,(Double)convertedValue);
		}
		else if ( convertedValue instanceof Long ) {
			NameReference mesNr = mesAssociable.makeAssociatedLongs(getPredicate());
			mesNr.setNamespace(getURI());
			mesAssociable.setAssociatedLong(mesNr,index,(Long)convertedValue);					
		}	
		else if ( convertedValue instanceof Object ) {
			NameReference mesNr = mesAssociable.makeAssociatedObjects(getPredicate());
			mesNr.setNamespace(getURI());
			mesAssociable.setAssociatedObject(mesNr,index,convertedValue);
		}		
	}

	@Override
	public
	void write() {
		// TODO Auto-generated method stub
		
	}
	
}
