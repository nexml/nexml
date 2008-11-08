package org.biophylo.util;
import java.util.*;
public class Logger {
	private static Logger instance = null;
	private int level;
	private Vector listeners;
	
	/**
	 * 
	 */
	protected Logger() {
		this.level = 2;
		this.listeners = new Vector();
		this.listeners.add(new DefaultListener());
	}
	
	/**
	 * @return
	 */
	public static Logger getInstance() {
		if(instance == null) {
			instance = new Logger();
		}
	    return instance;
	}
	
	/**
	 * @param level
	 */
	public void VERBOSE(int level) {
		this.level = level;
	}
	
	/**
	 * @param msg
	 */
	public void fatal(String msg) {
		if ( this.level >= 0 ) {			
			this.broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void error(String msg) {
		if ( this.level >= 1 ) {
			this.broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void warn (String msg) {
		if ( this.level >= 2 ) {
			this.broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void info (String msg) {
		if ( this.level >= 3 ) {
			this.broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void debug (String msg) {
		if ( this.level >= 4 ) {
			this.broadcast(msg);
		}
	}
	
	/**
	 * @param listener
	 */
	public void addListener(LogListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeListener(LogListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * @param msg
	 */
	private void broadcast (String msg) {
		Throwable stack = new Throwable();
		StackTraceElement[] stes = stack.getStackTrace();
		StackTraceElement ste = stes[2];
		String[] fullMsg = new String[5];
		fullMsg[0] = msg;
		fullMsg[1] = stes[1].getMethodName().toUpperCase();
		fullMsg[2] = ste.getMethodName();
		fullMsg[3] = ste.getFileName();
		fullMsg[4] = ""+ ste.getLineNumber();
		
		for ( int i = 0; i < this.listeners.size(); i++ ) {
			((LogListener)this.listeners.get(i)).notify(fullMsg);
		}
	}
}
class DefaultListener implements LogListener {
	int msgI = 0;
	int levelI = 1;
	int methodI = 2;
	int fileI = 3;
	int lineI = 4;
	
	/* (non-Javadoc)
	 * @see org.biophylo.Util.LogListener#notify(java.lang.String[])
	 */
	public void notify (String[] msg) {
		System.err.println(
			msg[levelI]
			    + " " + msg[methodI]
			    + " [" + msg[fileI] + ":" + msg[lineI] + "] - "
			    + msg[msgI]
		);
	}
}
