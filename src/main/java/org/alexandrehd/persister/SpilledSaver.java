package org.alexandrehd.persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**	Saving machinery which spills out higher levels of the map it's saving
 	into a folder structure, for manual inspection and manipulation.
 	
 	@author nick
 */

@Deprecated
public class SpilledSaver {
	private int itsDirectoryDepth;
	private File itsRoot;
	private File itsDirectoryMarker;
	
	/**	The base extension for serialised leaves of the tree. (The directory names are
	 	left intact.)
	 */

	static private final String BASE_EXTENSION = ".ser";
	
	static private final String DIR_MARKER = "PRESET_MARKER";
	
	public SpilledSaver(File root, int directoryDepth) {
		itsDirectoryDepth = directoryDepth;
		itsRoot = root;
		itsDirectoryMarker = new File(itsRoot, DIR_MARKER);
	}
	
	private void makeRootValid() throws IllegalStateException, IOException {
		if (itsRoot.isDirectory()) {
			if (!itsDirectoryMarker.exists()) {
				throw new IllegalStateException("cannot find marker file: " + itsDirectoryMarker);
			}
		} else if (itsRoot.isFile()) {
			// TODO - write test first!
		} else {			// Create:
			if (!itsRoot.mkdir()) {
				throw new IOException("cannot create directory: " + itsRoot);
			}
			
			if (!itsDirectoryMarker.createNewFile()) {
				throw new IOException("cannot create marker file: " + itsDirectoryMarker);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void persist(HashMap<String, ?> map) throws IOException {
		if (itsDirectoryDepth == 0) {
			SimpleSaver.persist(map, new File(itsRoot.getParent(), itsRoot.getName() + BASE_EXTENSION));
		} else {
			// TODO: check for (and remove) flat serialised file if present?
			makeRootValid();

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
		File serialisedBase = new File(itsRoot.getParent(), itsRoot.getName() + BASE_EXTENSION);

		// Look for the .ser first, if that fails look for (and explore) the directory.
		if (serialisedBase.exists()) {
			return SimpleSaver.unpersist(serialisedBase);
		} else if (itsRoot.exists()) {
			if (itsDirectoryMarker.exists()) {
				return unpersistFromTree(itsRoot);
			} else {		// No marker: punt!
				throw new IllegalStateException("directory marker not found: " + itsDirectoryMarker);
			}
		} else {
			throw new FileNotFoundException(itsRoot.toString());
		}
	}

	private HashMap<String, ?> unpersistFromTree(File dir) {
		// TODO Auto-generated method stub
		return null;
	}
}
