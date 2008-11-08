package org.biophylo.mediators;
import org.biophylo.*;
import java.util.*;

public class ObjectMediator {
	private static ObjectMediator instance = null;
	private Vector object;
	
	/**
	 * 
	 */
	protected ObjectMediator() {
	     this.object = new Vector();
	}
	
	/**
	 * @return
	 */
	public static ObjectMediator getInstance() {
		if(instance == null) {
			instance = new ObjectMediator();
		}
	    return instance;
	}
	
	/**
	 * @param obj
	 */
	public void register(Base obj) {
		int id = obj.getId();
		this.object.add(id, obj);
	}
	
	/**
	 * @param id
	 * @return
	 */
	public Base getObjectById(int id) {
		return (Base)this.object.get(id);
	}
	
	/**
	 * @param obj
	 */
	public void unregister(Base obj) {
		this.object.remove(obj);
	}
}
