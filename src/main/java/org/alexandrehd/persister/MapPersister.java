package org.alexandrehd.persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.alexandrehd.persister.util.Cloner;

public class MapPersister extends MapIO {
	private HashMap<String, ?> itsSnapshot;
	private int itsDepth;

	public MapPersister(File root, int depth) {
		super(root);
		itsDepth = depth;
		itsSnapshot = null;
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
	
	/**	Persist a map. This will be saved incrementally by reference to the
	 	map we originally read (if any; if not, we'll attempt a read before
	 	saving).
	 	
	 	@param map the map to persist
	 	
	 	@throws IllegalArgumentException if the map contains values which we
	 		don't allow to be serialised
	 		
	 	@throws IOException
	 	@throws ClassNotFoundException 
	 */
	
	public void persist(HashMap<String, ?> map)
		throws IllegalArgumentException, IOException, ClassNotFoundException
	{
		checkAllTypesOK(map);
		
		if (itsSnapshot == null) {
			try { /*ignore*/ unpersist(); } catch (IOException _) { }
		}
		
		persistNode(itsSnapshot, map, getRootPath(), itsDepth);
		itsSnapshot = new Cloner<HashMap<String, ?>>().deepCopy(map);
	}
	
	private void persistMap(HashMap<String, ?> oldMap,
						    HashMap<String, ?> newMap,
						    File location,
						    int depth) throws IOException {
		Set<String> oldKeys = oldMap.keySet();
		Set<String> newKeys = newMap.keySet();
		
		//	Remove old entries. (This might protect us against case-indifference.)		
		for (String k: oldKeys) {
			if (!newKeys.contains(k)) {
				new MapSaver(new File(location, k)).deleteRoot();
			}
		}
		
		//	Add new entries:
		for (String k: newKeys) {
			if (!oldKeys.contains(k)) {
				new MapSaver(new File(location, k)).saveNode(newMap.get(k), depth - 1);
			}
		}
		
		//	Recurse for keys in both maps:
		for (String k: oldKeys) {
			if (newKeys.contains(k)) {
				persistNode(oldMap.get(k),
							newMap.get(k),
							new File(location, k),
							depth - 1);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void persistNode(Object oldObj,
							 Object newObj,
							 File location,
							 int depth) throws IOException {
		//	Do we need to do anything at this point?
		if (!newObj.equals(oldObj)) {
			//	Do we need to do a recursive persistence pass?
			if (   location.isDirectory()		//	FIXME also recurse if directory not there?
				&& newObj.getClass() == HashMap.class
				&& depth > 0) {
				persistMap((HashMap<String, ?>) oldObj,
						   (HashMap<String, ?>) newObj,
						   location,
						   depth);
			} else {
				//	Not recursing: just wipe and overwrite existing:
				MapSaver saver = new MapSaver(location);
				saver.deleteRoot();
				saver.saveNode(newObj, depth);
			}
			
		}
	}
	
	/**	Unpersist a map from disk. Having done so, we keep a copy of the
	 	unpersisted map since this will be our template when doing difference
	 	calculations for any subsequent {@link #persist()} calls.
	 	
	 	@return the unpersisted map
	 	@throws IOException
	 	@throws ClassNotFoundException
	 */
	
	public HashMap<String, ?> unpersist()
		throws IOException, ClassNotFoundException
	{
		HashMap<String, ?> m = new MapLoader(getRootPath()).loadFromRoot();
		itsSnapshot = new Cloner<HashMap<String, ?>>().deepCopy(m);
		return m;
	}
}
