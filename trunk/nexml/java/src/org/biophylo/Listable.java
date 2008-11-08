package org.biophylo;
import org.biophylo.*;
import org.biophylo.util.exceptions.*;
import java.util.*;
import org.w3c.dom.*;

public abstract class Listable extends Containable {

	private Vector entities;
	
	/**
	 * 
	 */
	public Listable() {
		super();
		this.entities = new Vector();
	}
	
	/**
	 * @param obj
	 * @throws ObjectMismatch
	 */
	public void insert (Object[] obj) throws ObjectMismatch {
		for ( int i = 0; i < obj.length; i++ ) {
			if ( canContain(obj[i])) {
				this.entities.add(obj[i]);
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
		if ( this.canContain(obj) ) {
			this.entities.add(obj);
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
			if ( ! this.canContain(obj[i]) ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	public void clear () {
		this.entities.clear();
	}
	
	/**
	 * @return
	 */
	public Containable first () {
		return (Containable)this.entities.firstElement();
	}
	
	/**
	 * @return
	 */
	public Containable last () {
		return (Containable)this.entities.lastElement();
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean canContain (Object obj) {
		return this.type() == ((Containable)obj).container();
	}	
	
	/**
	 * @param visitor
	 */
	public void visit (Visitor visitor) {	
		for ( int i = 0; i < this.entities.size(); i++ ) {
			visitor.visit((Containable)this.entities.get(i));
		}		
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean contains (Containable obj) {
		return this.entities.contains(obj);
	}
	
	/**
	 * @param obj
	 */
	public void delete (Containable obj) {
		this.entities.remove(obj);
	}
	
	/**
	 * @return
	 */
	public Containable[] getEntities () {
		Containable[] ents = new Containable[this.entities.size()];
		this.entities.copyInto(ents);
		return ents;
	}
	
	/**
	 * @return
	 */
	public String[] getStringEntities() {
		String[] ents = new String[this.entities.size()];
		this.entities.copyInto(ents);
		return ents;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public Containable getByIndex (int index) {
		return (Containable)this.entities.get(index);
	}
	
}
