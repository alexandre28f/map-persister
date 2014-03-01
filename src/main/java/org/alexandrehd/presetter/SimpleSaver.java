package org.alexandrehd.presetter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SimpleSaver {
	static final Set<Class<?>> allowedTypes;
	
	static {
		allowedTypes = new HashSet<Class<?>>();
		allowedTypes.add(Double.class);
		allowedTypes.add(double[].class);
		allowedTypes.add(double[][].class);
		allowedTypes.add(String.class);
	}
	
	@SuppressWarnings("unchecked")
	static private void checkAllTypesOK(HashMap<String, ?> map) throws IllegalArgumentException {
		for (Object o: map.values()) {
			if (o instanceof HashMap) {
				checkAllTypesOK((HashMap<String, ?>) o);
			} else if (!allowedTypes.contains(o.getClass())) {
				throw new IllegalArgumentException("cannot serialise type " + o.getClass());
			}
		}
	}
	
	static public void persist(HashMap<String, ?> map, File f) throws IOException {
		ObjectOutputStream stream = null;
		
		try {
			checkAllTypesOK(map);
			OutputStream out = new java.io.FileOutputStream(f);
			stream = new ObjectOutputStream(out);
			stream.writeObject(map);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	static public HashMap<String, ?> unpersist(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream stream = null;
		try {
			InputStream in = new java.io.FileInputStream(f);
			stream = new ObjectInputStream(in);
			return (HashMap<String, ?>) stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
