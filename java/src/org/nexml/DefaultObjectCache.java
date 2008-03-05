package org.nexml;

// $Id$

import java.util.*;

/**
 * The ObjectCache stores objects instantiated by the factories and passed
 * to it by the ElementHandler. It indexes them in various ways, so that 
 * references between objects can be resolved as the stream is being 
 * processed. In particular, because the specification is designed such that
 * referenced objects always precede the objects that refer to them, once a
 * reference comes along, the object that it refers to can be retrieved from
 * this cache.
 * @author rvosa
 */
public class DefaultObjectCache implements ObjectCache {
	private Hashtable objById;
	private Hashtable idByObj;
	private Hashtable referencedIdById;
	private Vector objects;
	
	/**
	 * Default constructor
	 */
	public DefaultObjectCache() {
		this.idByObj = new Hashtable();
		this.objById = new Hashtable();
		this.referencedIdById = new Hashtable();
		this.objects = new Vector();
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCache#getObjectById(java.lang.String)
	 */
	public NexmlWritable getObjectById (String id) {
		return (NexmlWritable)this.objById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCache#getIdByObject(java.lang.Object)
	 */
	public String getIdByObject(NexmlWritable obj) {
		return (String)this.idByObj.get(obj);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCache#getReferencedIdById(java.lang.String)
	 */
	public String getReferencedIdById (String id) {
		return (String)this.referencedIdById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCache#setObject(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public void setObject(NexmlWritable obj, String id, String idref) {
		this.objects.add(obj);
		if ( id != null ) {
			this.idByObj.put(obj,id);
			this.objById.put(id, obj);
		}
		if ( idref != null ) {
			this.referencedIdById.put(id, idref);
		}
	}
	
	public NexmlWritable getObject(int i) {
		if ( this.objects.size() > i ) {
			return (NexmlWritable)this.objects.get(i);
		}
		else {
			return null;
		}
	}
}
