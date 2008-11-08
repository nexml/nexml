package org.biophylo.mediators;
import java.util.*;
import org.biophylo.taxa.*;
import org.biophylo.util.*;
import org.biophylo.*;

public class TaxaMediator {
	private static TaxaMediator instance = null;
	private static Logger logger = Logger.getInstance();
	private ObjectMediator object;
	private HashMap objectsForTaxon;
	private HashMap taxonForObject;
	
	/**
	 * 
	 */
	protected TaxaMediator() {
	     this.object = ObjectMediator.getInstance();
	     this.objectsForTaxon = new HashMap(); // key is taxon ID
	     this.taxonForObject = new HashMap(); // key is taxonlinker id
	}
	
	/**
	 * @return
	 */
	public static TaxaMediator getInstance() {
		if(instance == null) {
			instance = new TaxaMediator();
		}
	    return instance;
	}
	
	/**
	 * @param taxonId
	 * @param linkerId
	 */
	public void setLink(int taxonId,int linkerId) {
		Integer lId = new Integer(linkerId);
		Integer tId = new Integer(taxonId);
		Containable obj = (Containable)this.object.getObjectById(linkerId);
		Integer type = new Integer(obj.type());
		this.taxonForObject.put(lId, tId);
		if ( ! this.objectsForTaxon.containsKey(tId) ) {
			this.objectsForTaxon.put(tId, new HashMap());
		}
		((HashMap)this.objectsForTaxon.get(tId)).put(lId, type);
	}
	
	/**
	 * @param linkerId
	 * @return
	 */
	public Containable getLink(int linkerId) {
		Integer lId = new Integer(linkerId);
		if ( this.taxonForObject.containsKey(lId) ) {
			Integer taxonId = (Integer)this.taxonForObject.get(lId);
			return (Containable)this.object.getObjectById(taxonId.intValue());
		}
		return null;
	}
	
	/**
	 * @param taxonId
	 * @param type
	 * @return
	 */
	public Vector getLink(int taxonId, int type) {
		Integer tId = new Integer(taxonId);
		if ( this.objectsForTaxon.containsKey(tId) ) {
			logger.debug("contains taxon id " + taxonId + " (" + this.object.getObjectById(taxonId) + ")");
			HashMap objects = (HashMap)this.objectsForTaxon.get(tId);
			logger.debug("HashMap: " + objects);
			Integer objType = new Integer(type);
			logger.debug("Requested object type " + type);
			if ( objects.containsValue(objType) ) {
				logger.debug("Have link to that object type");
				Object keys[] = objects.keySet().toArray();
				Vector result = new Vector();
				for ( int i = 0; i < keys.length; i++ ) {					
					Integer key = (Integer)keys[i];
					logger.debug("Inspecting obj "+keys[i]+ " => type "+((Integer)objects.get(key)).intValue());
					if ( ((Integer)objects.get(key)).intValue() == type ) {
						logger.debug("Found object id "+key.toString());
						result.add(this.object.getObjectById(key.intValue()));
					}
				}
				return result;
			}

		}
		return null;
	}
	
	/**
	 * @param taxonId
	 * @param linkerId
	 */
	public void removeLink(int taxonId, int linkerId) {
		logger.info("removing link " + taxonId + " => " + linkerId);
		Integer tId = new Integer(taxonId);
		Integer lId = new Integer(linkerId);
		if ( this.objectsForTaxon.containsKey(tId) ) {
			if ( ((HashMap)this.objectsForTaxon.get(tId)).containsKey(lId) ) {
				((HashMap)this.objectsForTaxon.get(tId)).remove(lId);
			}
			else {
				this.objectsForTaxon.remove(tId);
			}
		}
		if ( this.taxonForObject.containsKey(lId) ) {
			this.taxonForObject.remove(lId);
		}
	}
}
