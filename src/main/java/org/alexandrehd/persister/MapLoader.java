package org.alexandrehd.persister;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class MapLoader extends MapIO implements IMapLoader {
	public MapLoader(File root) {
		super(root);
	}
	
	private Object loadNodeFromRoot() throws ClassNotFoundException, IOException {
		if (getFlatFile().exists()) {
			return loadObjectFromRoot();
		} else {
			File root = getRootFile();
			File[] contents = root.listFiles();
			if (contents == null) {
				throw new IOException("cannot read directory at: " + root);
			} else {
				HashMap<String, Object> result = new HashMap<String, Object>();
				
				for (File f: contents) {
					// This is a bit scrappy: we might hit both FOO and FOO.ser, in which case
					// we'll process FOO twice and get FOO.ser each time (as preference).
					//	TODO: pull out as strings, use a set.
					String n = f.getName();
					n = n.replaceAll("\\.ser$", "");
					//	TODO: sanitise names!
					result.put(n, new MapLoader(new File(f.getParentFile(), n)).loadNodeFromRoot());
				}
				
				return result;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, ?> loadFromRoot() throws ClassNotFoundException, IOException {
		return (HashMap<String, ?>) loadNodeFromRoot();
	}

	/*package*/ Object loadObjectFromRoot() throws IOException, ClassNotFoundException {
		ObjectInputStream stream = null;
		try {
			InputStream in = new java.io.FileInputStream(getFlatFile());
			stream = new ObjectInputStream(in);
			return stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
