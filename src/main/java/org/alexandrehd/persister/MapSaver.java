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

public class MapSaver extends MapIO implements IMapSaver {
	public MapSaver(File root) {
		super(root);
	}

	static final Set<Class<?>> allowedTypes;
	
	//	Set up the kinds of object we allow in our maps (we also allow nested maps):
	static {
		allowedTypes = new HashSet<Class<?>>();
		allowedTypes.add(Double.class);
		allowedTypes.add(double[].class);
		allowedTypes.add(double[][].class);
		allowedTypes.add(String.class);
	}
	
	@SuppressWarnings("unchecked")
	static private void checkAllTypesOK(HashMap<String, ?> map) throws IllegalArgumentException {
		if (map.getClass() != HashMap.class) {
			throw new IllegalArgumentException("cannot serialise type " + map.getClass());
		}
		
		for (Object o: map.values()) {
			if (o.getClass() == HashMap.class) {
				checkAllTypesOK((HashMap<String, ?>) o);
			} else if (!allowedTypes.contains(o.getClass())) {
				throw new IllegalArgumentException("cannot serialise type " + o.getClass());
			}
		}
	}
	
	private void saveNode(Object obj, int depth) throws IOException {
		if (depth == 0) {
			saveObjectToRoot(obj);
		} else if (obj.getClass() == HashMap.class) {
			File root = getRootFile();
			if (!root.mkdir()) { throw new IOException("could not create directory " + root); }

			@SuppressWarnings("unchecked")
			HashMap<String, ?> m = (HashMap<String, ?>) obj;

			for (Map.Entry<String, ?> e: m.entrySet()) {
				String key = e.getKey();
				MapSaver saver = new MapSaver(new File(getRootFile(), key));
				saver.saveNode(e.getValue(), depth - 1);
			}
		} else {
			saveObjectToRoot(obj);
		}
		
	}

	/** Save an item after sanity check (and after removing anything currently in
	 	the save location). NB: this method *deletes the old root* before writing
	 	the data.
	 	
	 	@see org.alexandrehd.persister.IMapSaver#saveToRoot(java.util.HashMap, int)
	 */

	@Override
	public void saveToRoot(HashMap<String, ?> item, int depth) throws IOException {
		//	TODO this check should be lifted into the main delta-based persister
		//	(once we've written it).
		checkAllTypesOK(item);
		deleteRoot();
		saveNode(item, depth);
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
    
    private void deleteRoot() throws FileNotFoundException {
		getFlatFile().delete();
		
		File r = getRootFile();
		if (r.exists()) {  deleteRecursive(r); }
	}

	@SuppressWarnings("unchecked")
	/*package*/ void saveObjectToRoot(Object obj) throws IOException {
		ObjectOutputStream stream = null;
		
		try {
			if (obj instanceof HashMap) {
				checkAllTypesOK((HashMap<String, ?>) obj);
			} else if (!allowedTypes.contains(obj.getClass())) {
				throw new IllegalArgumentException("cannot serialise type: " + obj.getClass());
			}
			
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
