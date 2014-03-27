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

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, ?> loadFromRoot() throws ClassNotFoundException, IOException {
		// Base case for now:
		return (HashMap<String, ?>) loadAnyFromRoot();
	}

	/*package*/ Object loadAnyFromRoot() throws IOException, ClassNotFoundException {
		ObjectInputStream stream = null;
		try {
			InputStream in = new java.io.FileInputStream(getFlatName());
			stream = new ObjectInputStream(in);
			return stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
