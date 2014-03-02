package org.alexandrehd.presetter;

import java.io.File;
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
	
	@SuppressWarnings("unchecked")
	static public void persistAny(Object obj, File f) throws IOException, IllegalArgumentException {
		ObjectOutputStream stream = null;
		
		try {
			if (obj instanceof HashMap) {
				checkAllTypesOK((HashMap<String, ?>) obj);
			} else if (!allowedTypes.contains(obj.getClass())) {
				throw new IllegalArgumentException("cannot serialise type: " + obj.getClass());
			}
			
			OutputStream out = new java.io.FileOutputStream(f);
			stream = new ObjectOutputStream(out);
			stream.writeObject(obj);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	static public void persist(HashMap<String, ?> map, File f) throws IllegalArgumentException, IOException {
		persistAny(map, f);
	}
	
	static public Object unpersistAny(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream stream = null;
		try {
			InputStream in = new java.io.FileInputStream(f);
			stream = new ObjectInputStream(in);
			return stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	@SuppressWarnings("unchecked")
	static public HashMap<String, ?> unpersist(File f) throws IOException, ClassNotFoundException {
		return (HashMap<String, ?>) unpersistAny(f);
	}
}
