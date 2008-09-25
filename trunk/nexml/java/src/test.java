import org.biophylo.*;
import org.biophylo.Util.*;
import java.io.*;
public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {						
		Logger logger = Logger.getInstance();
		logger.VERBOSE(2);
		try {
			FileInputStream fs = new FileInputStream(args[0]);
			Object[] blocks = IO.parse("Nexml", fs);
			if ( blocks[0] != null ) {
				logger.warn(((XMLWritable)blocks[0]).getRootOpenTag());
				for ( int i = 0; i < blocks.length; i++ ) {
					logger.warn(((XMLWritable)blocks[i]).toXml());
				}
				logger.warn(((XMLWritable)blocks[0]).getRootCloseTag());
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	
	
}
