/**
 * 
 */
package org.biophylo;
import org.biophylo.Util.*;
import org.biophylo.Mediators.*;
import java.util.*;
import java.lang.reflect.*;
/**
 * @author rvosa
 *
 */
public abstract class Base {
	private String name;
	private String desc;
	private Double score;
	private HashMap generic;
	private int id;	
	private static Logger logger = Logger.getInstance();
	private IDPool pool = IDPool.getInstance();
	private static ObjectMediator objectMediator = ObjectMediator.getInstance();
	public static final double VERSION = 0.1;
	
	public Base () {
		this.id = pool.makeId();
		this.generic = new HashMap();
		objectMediator.register(this);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}
	
	public void setGeneric(HashMap generic) {
		this.generic = generic;
	}
	
	public void setGeneric(Object key, Object value) {
		if ( this.generic == null ) {
			this.generic = new HashMap();
		}
		this.generic.put(key, value);
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInternalName() {
		return this.getClass().getSimpleName() + this.getId();
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public Double getScore() {
		return this.score;
	}
	
	public HashMap getGeneric () {
		return this.generic;
	}
	
	public Object getGeneric (Object key) {
		return this.generic.get(key);
	}
	
	public int getId() {
		return this.id;
	}
	
	public static Logger getLogger() {
		return logger;
	}
	
	protected void finalize() throws Throwable {
	  //do finalization here
		objectMediator.unregister(this);
	  //super.finalize(); //not necessary if extending Object.
	} 	

}
