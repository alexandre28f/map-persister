package org.alexandrehd.presetter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**	Saving machinery which spills out higher levels of the map it's saving into a folder structure,
 	for manual inspection and manipulation.
 	
 	@author nick
 */

public class SpilledSaver {
	private int itsDirectoryDepth;
	private File itsRoot;
	
	/**	The base extension for serialised leaves of the tree. (The directory names are
	 	left intact.)
	 */

	static private final String BASE_EXTENSION = ".ser";
	
	public SpilledSaver(File root, int directoryDepth) {
		itsDirectoryDepth = directoryDepth;
		itsRoot = root;
	}

	@SuppressWarnings("unchecked")
	public void persist(HashMap<String, ?> map) throws IOException {
		if (itsDirectoryDepth == 0) {
			SimpleSaver.persist(map, new File(itsRoot.getParent(), itsRoot.getName() + BASE_EXTENSION));
		} else {
			if (!itsRoot.mkdir()) {
				throw new IOException("cannot create directory: " + itsRoot);
			}
			
			for (String k: map.keySet()) {
				Object v = map.get(k);
				
				if (v instanceof HashMap) {
					new SpilledSaver(new File(itsRoot, k), itsDirectoryDepth - 1)
						.persist((HashMap<String, ?>) v);
				} else {
					SimpleSaver.persistAny(v, new File(itsRoot, k + ".ser"));
				}
			}
		}
	}
	
	public HashMap<String, ?> unpersist() throws ClassNotFoundException, IOException {
		File serialised = new File(itsRoot.getParent(), itsRoot.getName() + BASE_EXTENSION);

		// TODO: look for the .ser first, if that fails look for (and explore) the directory.
		if (serialised.exists()) {
			return SimpleSaver.unpersist(serialised);
		} else {
			throw new FileNotFoundException(serialised.toString());
		}
	}
}
