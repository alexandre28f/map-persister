package org.alexandrehd.persister;

import java.io.File;
import java.util.HashMap;

/**	An interface for reading an entire map from a location, which is either
 	a flat file (we add the extension) or a directory.
 	
 	@author Nick Rothwell, nick@cassiel.com
 */

public interface IMapLoader {
	HashMap<String, ?> loadFromLocation(File location);
}
