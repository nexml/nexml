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
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCacheI#getObjectById(java.lang.String)
	 */
	public Object getObjectById (String id) {
		return this.objById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCacheI#getIdByObject(java.lang.Object)
	 */
	public String getIdByObject(Object obj) {
		return (String)this.idByObj.get(obj);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCacheI#getReferencedIdById(java.lang.String)
	 */
	public String getReferencedIdById (String id) {
		return (String)this.referencedIdById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.nexml.ObjectCacheI#setObject(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public void setObject(Object obj, String id, String idref) {
		this.idByObj.put(obj, id);
		this.objById.put(id, obj);
		if ( idref != null ) {
			this.referencedIdById.put(id, idref);
		}
	}
}
