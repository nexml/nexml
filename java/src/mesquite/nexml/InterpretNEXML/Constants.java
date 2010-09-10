package mesquite.nexml.InterpretNEXML;

import java.net.URI;

public class Constants {
	public static final String MESQUITE_NS_PREFIX = "msq";
	public static final String MESQUITE_NS_BASE = "http://mesquiteproject.org#";
	public static final String NRURIString = "http://mesquiteproject.org/namereference#";
	public static final String BeanURIString = "http://mesquiteproject.org/bean#";
	public static final String BaseURIString = "http://mesquiteproject.org#";
	
	public static final String BasePrefix = "msq";
	public static final String NRPrefix = "nr";
	public static final String BeanPrefix = "bean";
	
	public static final URI BaseURI = URI.create(BaseURIString);
	public static final URI BeanURI = URI.create(BeanURIString);
	
	public static final String TaxaUID  = BasePrefix + ":taxaUID";
	public static final String TaxonUID = BasePrefix + ":taxonUID";	
	
	public static final String PREDICATES_PROPERTIES = "predicateHandlerMapping.properties";
	public static final String NAMESPACE_PROPERTIES = "namespaceHandlerMapping.properties";
}
