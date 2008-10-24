package org.biophylo;
import org.biophylo.*;
import org.biophylo.Util.Exceptions.*;
import java.util.*;
import org.w3c.dom.*;

public abstract class Listable extends Containable {

	private Vector entities;
	
	public Listable() {
		super();
		this.entities = new Vector();
	}
	
	public void insert (Object[] obj) throws ObjectMismatch {
		for ( int i = 0; i < obj.length; i++ ) {
			if ( this.canContain(obj[i])) {
				if ( obj[i] instanceof Containable ) {
					Document doc = getDocument();
					if ( doc == null ) {
						doc = createDocument();
						setDocument(doc);
					}
					((Containable)obj[i]).setDocument(doc);
				}
				this.entities.add(obj[i]);
			}
			else {
				throw new ObjectMismatch();
			}
		}
	}
	
	public void insert (Object obj) throws ObjectMismatch {
		if ( this.canContain(obj) ) {
			this.entities.add(obj);
		}
		else {
			throw new ObjectMismatch();
		}		
	}
	
	public boolean canContain (Object[] obj) {
		for ( int i = 0; i < obj.length; i++ ) {
			if ( ! this.canContain(obj[i]) ) {
				return false;
			}
		}
		return true;
	}
	
	public void clear () {
		this.entities.clear();
	}
	
	public Containable first () {
		return (Containable)this.entities.firstElement();
	}
	
	public Containable last () {
		return (Containable)this.entities.lastElement();
	}
	
	public boolean canContain (Object obj) {
		return this.type() == ((Containable)obj).container();
	}	
	
	public void visit (Visitor visitor) {	
		for ( int i = 0; i < this.entities.size(); i++ ) {
			visitor.visit((Containable)this.entities.get(i));
		}		
	}
	
	public boolean contains (Containable obj) {
		return this.entities.contains(obj);
	}
	
	public void delete (Containable obj) {
		this.entities.remove(obj);
	}
	
	public Containable[] getEntities () {
		Containable[] ents = new Containable[this.entities.size()];
		this.entities.copyInto(ents);
		return ents;
	}
	
	public String[] getStringEntities() {
		String[] ents = new String[this.entities.size()];
		this.entities.copyInto(ents);
		return ents;
	}
	
	public Containable getByIndex (int index) {
		return (Containable)this.entities.get(index);
	}
	
}
