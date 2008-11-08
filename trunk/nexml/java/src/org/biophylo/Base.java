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
	private String mName;
	private Map mGeneric;
	private int mId;	
	private static Logger logger = Logger.getInstance();
	private static IDPool pool = IDPool.getInstance();
	private static ObjectMediator objectMediator = ObjectMediator.getInstance();
	public static final double VERSION = 0.1;
	
	/**
	 * 
	 */
	public Base () {
		mId = pool.makeId();
		mGeneric = new HashMap();
		objectMediator.register(this);
	}
	
	/**
	 * @param name
	 */
	public void setName(String pName) {
		mName = pName;
	}
	
	/**
	 * @param generic
	 */
	public void setGeneric(Map pGeneric) {
		mGeneric = pGeneric;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void setGeneric(Object key, Object value) {
		if ( mGeneric == null ) {
			mGeneric = new HashMap();
		}
		mGeneric.put(key, value);
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return mName;
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
		return mGeneric;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Object getGeneric (Object key) {
		return mGeneric.get(key);
	}
	
	/**
	 * @return
	 */
	public int getId() {
		return mId;
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
