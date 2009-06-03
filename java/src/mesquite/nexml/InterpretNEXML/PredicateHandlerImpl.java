package mesquite.nexml.InterpretNEXML;

public class PredicateHandlerImpl extends PredicateHandler {
	private String mPredicate;
	private String mPrefix = "msq";
	private boolean mPropertyIsRel;
	private Object mValue;
	private String mProperty;
	private Object mSubject;

	public PredicateHandlerImpl(Object subject, String predicate, Object value) {
		super(subject, predicate, value);
	}
	
	@Override
	void setPredicate(String predicate) {
		mPredicate = predicate;
	}	

	@Override
	String getPredicate() {
		return mPredicate;
	}

	@Override
	void setPrefix(String prefix) {
		mPrefix = prefix;
	}		
	
	@Override
	String getPrefix() {
		return mPrefix;
	}
	
	@Override
	void setPropertyIsRel(boolean propertyIsRel) {
		mPropertyIsRel = propertyIsRel;		
	}	


	@Override
	boolean getPropertyIsRel() {
		return mPropertyIsRel;
	}	
	

	@Override
	String getURIString() {
		return "http://mesquiteproject.org#";
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

	public Object getSubject() {
		return mSubject;
	}

	public void setSubject(Object subject) {
		mSubject = subject;
	}
	
}
