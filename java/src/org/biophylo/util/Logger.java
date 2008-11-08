package org.biophylo.util;
import java.util.*;
public class Logger {
	private static Logger mInstance = null;
	private int mLevel;
	private Vector mListeners;
	
	/**
	 * 
	 */
	protected Logger() {
		mLevel = 2;
		mListeners = new Vector();
		mListeners.add(new DefaultListener());
	}
	
	/**
	 * @return
	 */
	public static Logger getInstance() {
		if(mInstance == null) {
			mInstance = new Logger();
		}
	    return mInstance;
	}
	
	/**
	 * @param level
	 */
	public void VERBOSE(int pLevel) {
		mLevel = pLevel;
	}
	
	/**
	 * @param msg
	 */
	public void fatal(String msg) {
		if ( mLevel >= 0 ) {			
			broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void error(String msg) {
		if ( mLevel >= 1 ) {
			broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void warn (String msg) {
		if ( mLevel >= 2 ) {
			broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void info (String msg) {
		if ( mLevel >= 3 ) {
			broadcast(msg);
		}
	}
	
	/**
	 * @param msg
	 */
	public void debug (String msg) {
		if ( mLevel >= 4 ) {
			broadcast(msg);
		}
	}
	
	/**
	 * @param listener
	 */
	public void addListener(LogListener listener) {
		mListeners.add(listener);
	}
	
	/**
	 * @param listener
	 */
	public void removeListener(LogListener listener) {
		mListeners.remove(listener);
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
		
		for ( int i = 0; i < mListeners.size(); i++ ) {
			((LogListener)mListeners.get(i)).notify(fullMsg);
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
