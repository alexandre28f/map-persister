package org.alexandrehd.persister;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
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

	@Override
	public void saveToRoot(HashMap<String, ?> item, int depth) throws IOException {
		// Flat save only (for now):
		saveAnyToRoot(item);
	}

	@SuppressWarnings("unchecked")
	/*package*/ void saveAnyToRoot(Object obj) throws IOException {
		ObjectOutputStream stream = null;
		
		try {
			if (obj instanceof HashMap) {
				checkAllTypesOK((HashMap<String, ?>) obj);
			} else if (!allowedTypes.contains(obj.getClass())) {
				throw new IllegalArgumentException("cannot serialise type: " + obj.getClass());
			}
			
			OutputStream out = new java.io.FileOutputStream(getFlatName());
			stream = new ObjectOutputStream(out);
			stream.writeObject(obj);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
