package org.alexandrehd.persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**	A class for writing a map to a location, either as a flat file
	(with extension added) or nested structure, according to a depth.

	<P>WARNING: Don't use this directly; there is (or will be) a difference-based
	saver which preserves existing files. {@link MapSaver} completely removes any existing
	file and/or structure at the root location.

	@author Nick Rothwell, nick@cassiel.com
*/

class MapSaver extends MapIO {
	MapSaver(File root) {
		super(root);
	}

	/*package*/ void saveNode(Object obj, int depth) throws IOException {
		if (depth == 0) {
			saveObjectToRoot(obj);
		} else if (obj.getClass() == HashMap.class) {
			File root = getRootPath();
			if (!root.mkdir()) { throw new IOException("could not create directory " + root); }

			@SuppressWarnings("unchecked")
			HashMap<String, ?> m = (HashMap<String, ?>) obj;

			for (Map.Entry<String, ?> e: m.entrySet()) {
				String key = e.getKey();
				MapSaver saver = new MapSaver(new File(getRootPath(), key));
				saver.saveNode(e.getValue(), depth - 1);
			}
		} else {
			saveObjectToRoot(obj);
		}
		
	}

    private static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
    
    void deleteRoot() throws FileNotFoundException {
		getFlatFile().delete();
		
		File r = getRootPath();
		if (r.exists()) {  deleteRecursive(r); }
	}

	@SuppressWarnings("unchecked")
	/*package*/ void saveObjectToRoot(Object obj) throws IOException {
		ObjectOutputStream stream = null;
		
		try {
			OutputStream out = new java.io.FileOutputStream(getFlatFile());
			stream = new ObjectOutputStream(out);
			stream.writeObject(obj);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
