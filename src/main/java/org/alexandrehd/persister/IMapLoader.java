package org.alexandrehd.persister;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**	An interface for reading an entire map from a location, which is either
 	a flat file (we add the extension) or a directory. For hysterical reasons,
 	We instantiate a MapLoader on each root location.
 	
 	@author Nick Rothwell, nick@cassiel.com
 */

public interface IMapLoader {
	HashMap<String, ?> loadFromRoot() throws ClassNotFoundException, IOException;
}
