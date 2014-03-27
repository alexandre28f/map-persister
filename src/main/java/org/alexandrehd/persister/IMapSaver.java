package org.alexandrehd.persister;

import java.io.IOException;
import java.util.HashMap;

/**	An interface for writing a map to a location, either as a flat file
    (with extension added) or nested structure, according to a depth.
    Completely removes any existing file and/or structure at this location.
    
 	@author Nick Rothwell, nick@cassiel.com
 */

public interface IMapSaver {
	/**	Save a map to a root. If depth is 0, create a flat file with extension
	 	".ser". If depth > 0, save into a directory, with entries matching the
	 	keys of the map. In both cases, any existing flat file or directory
	 	is deleted first.
	 	
	 	@param item the map to save
	 	@param depth the depth to "layer" this saved map into directories
	 	@throws IOException on error 
	 */
	
	void saveToRoot(HashMap<String, ?> item, int depth) throws IOException;
}
