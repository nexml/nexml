package org.biophylo;
import org.biophylo.util.exceptions.*;
import java.util.*;

public abstract class Listable extends Containable {

	private Vector mEntities;
	
	/**
	 * 
	 */
	public Listable() {
		super();
		mEntities = new Vector();
	}
	
	/**
	 * @param obj
	 * @throws ObjectMismatch
	 */
	public void insert (Object[] obj) throws ObjectMismatch {
		for ( int i = 0; i < obj.length; i++ ) {
			if ( canContain(obj[i])) {
				mEntities.add(obj[i]);
			}
			else {
				throw new ObjectMismatch();
			}
		}
	}
	
	/**
	 * @param obj
	 * @throws ObjectMismatch
	 */
	public void insert (Object obj) throws ObjectMismatch {
		if ( canContain(obj) ) {
			mEntities.add(obj);
		}
		else {
			throw new ObjectMismatch();
		}		
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean canContain (Object[] obj) {
		for ( int i = 0; i < obj.length; i++ ) {
			if ( ! canContain(obj[i]) ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	public void clear () {
		mEntities.clear();
	}
	
	/**
	 * @return
	 */
	public Containable first () {
		return (Containable)mEntities.firstElement();
	}
	
	/**
	 * @return
	 */
	public Containable last () {
		return (Containable)mEntities.lastElement();
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean canContain (Object obj) {
		return type() == ((Containable)obj).container();
	}	
	
	/**
	 * @param visitor
	 */
	public void visit (Visitor visitor) {	
		for ( int i = 0; i < mEntities.size(); i++ ) {
			visitor.visit((Containable)mEntities.get(i));
		}		
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean contains (Containable obj) {
		return mEntities.contains(obj);
	}
	
	/**
	 * @param obj
	 */
	public void delete (Containable obj) {
		mEntities.remove(obj);
	}
	
	/**
	 * @return
	 */
	public Containable[] getEntities () {
		Containable[] ents = new Containable[mEntities.size()];
		mEntities.copyInto(ents);
		return ents;
	}
	
	/**
	 * @return
	 */
	public String[] getStringEntities() {
		String[] ents = new String[mEntities.size()];
		mEntities.copyInto(ents);
		return ents;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public Containable getByIndex (int index) {
		return (Containable)mEntities.get(index);
	}
	
}
