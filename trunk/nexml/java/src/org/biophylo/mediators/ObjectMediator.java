package org.biophylo.mediators;
import org.biophylo.*;
import java.util.*;

public class ObjectMediator {
	private static ObjectMediator mInstance = null;
	private Vector mObject;
	
	/**
	 * 
	 */
	protected ObjectMediator() {
		mObject = new Vector();
	}
	
	/**
	 * @return
	 */
	public static ObjectMediator getInstance() {
		if( mInstance == null ) {
			mInstance = new ObjectMediator();
		}
	    return mInstance;
	}
	
	/**
	 * @param obj
	 */
	public void register(Base obj) {
		int id = obj.getId();
		mObject.add(id, obj);
	}
	
	/**
	 * @param id
	 * @return
	 */
	public Base getObjectById(int id) {
		return (Base)mObject.get(id);
	}
	
	/**
	 * @param obj
	 */
	public void unregister(Base obj) {
		mObject.remove(obj);
	}
}
