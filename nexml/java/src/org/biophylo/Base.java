/**
 * 
 */
package org.biophylo;
import org.biophylo.util.*;
import org.biophylo.mediators.*;
import java.util.*;
import java.lang.reflect.*;
/**
 * @author rvosa
 *
 */
public abstract class Base {
	private String name;
	private Map generic;
	private int id;	
	private static Logger logger = Logger.getInstance();
	private IDPool pool = IDPool.getInstance();
	private static ObjectMediator objectMediator = ObjectMediator.getInstance();
	public static final double VERSION = 0.1;
	
	/**
	 * 
	 */
	public Base () {
		this.id = pool.makeId();
		this.generic = new HashMap();
		objectMediator.register(this);
	}
	
	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param generic
	 */
	public void setGeneric(Map generic) {
		this.generic = generic;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void setGeneric(Object key, Object value) {
		if ( this.generic == null ) {
			this.generic = new HashMap();
		}
		this.generic.put(key, value);
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return
	 */
	public String getInternalName() {
		return this.getClass().getSimpleName() + this.getId();
	}
	
	/**
	 * @return
	 */
	public Map getGeneric () {
		return this.generic;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Object getGeneric (Object key) {
		return this.generic.get(key);
	}
	
	/**
	 * @return
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * @return
	 */
	public static Logger getLogger() {
		return logger;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
	  //do finalization here
		objectMediator.unregister(this);
	  //super.finalize(); //not necessary if extending Object.
	} 	

}
