package org.nexml;
import java.util.*;

public class ObjectCache {
	private Hashtable objById;
	private Hashtable idByObj;
	private Hashtable referencedIdById;
	
	public Object getObjectById (String id) {
		return this.objById.get(id);
	}
	
	public String getIdByObject(Object obj) {
		return (String)this.idByObj.get(obj);
	}
	
	public String getReferencedIdById (String id) {
		return (String)this.referencedIdById.get(id);
	}
	
	public void setObject(Object obj, String id, String idref) {
		this.idByObj.put(obj, id);
		this.objById.put(id, obj);
		if ( idref != null ) {
			this.referencedIdById.put(id, idref);
		}
	}
}
