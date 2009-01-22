package org.biophylo.mediators;
import java.util.*;
import org.biophylo.util.*;
import org.biophylo.*;

public class TaxaMediator {
	private static TaxaMediator mInstance = null;
	private static Logger logger = Logger.getInstance();
	private ObjectMediator mObject;
	private HashMap mObjectsForTaxon;
	private HashMap mTaxonForObject;
	
	/**
	 * 
	 */
	protected TaxaMediator() {
		mObject = ObjectMediator.getInstance();
		mObjectsForTaxon = new HashMap(); // key is taxon ID
		mTaxonForObject = new HashMap(); // key is taxonlinker id
	}
	
	/**
	 * @return
	 */
	public static TaxaMediator getInstance() {
		if( mInstance == null ) {
			mInstance = new TaxaMediator();
		}
	    return mInstance;
	}
	
	/**
	 * @param taxonId
	 * @param linkerId
	 */
	public void setLink(int taxonId,int linkerId) {
		Integer lId = new Integer(linkerId);
		Integer tId = new Integer(taxonId);
		Containable obj = (Containable)mObject.getObjectById(linkerId);
		Integer type = new Integer(obj.type());
		mTaxonForObject.put(lId, tId);
		if ( ! mObjectsForTaxon.containsKey(tId) ) {
			mObjectsForTaxon.put(tId, new HashMap());
		}
		((HashMap)mObjectsForTaxon.get(tId)).put(lId, type);
	}
	
	/**
	 * @param linkerId
	 * @return
	 */
	public Containable getLink(int linkerId) {
		Integer lId = new Integer(linkerId);
		if ( mTaxonForObject.containsKey(lId) ) {
			Integer taxonId = (Integer)mTaxonForObject.get(lId);
			return (Containable)mObject.getObjectById(taxonId.intValue());
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
		if ( mObjectsForTaxon.containsKey(tId) ) {
			logger.debug("contains taxon id " + taxonId + " (" + mObject.getObjectById(taxonId) + ")");
			HashMap objects = (HashMap)mObjectsForTaxon.get(tId);
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
						result.add(mObject.getObjectById(key.intValue()));
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
		if ( mObjectsForTaxon.containsKey(tId) ) {
			if ( ((HashMap)mObjectsForTaxon.get(tId)).containsKey(lId) ) {
				((HashMap)mObjectsForTaxon.get(tId)).remove(lId);
			}
			else {
				mObjectsForTaxon.remove(tId);
			}
		}
		if ( mTaxonForObject.containsKey(lId) ) {
			mTaxonForObject.remove(lId);
		}
	}
}
