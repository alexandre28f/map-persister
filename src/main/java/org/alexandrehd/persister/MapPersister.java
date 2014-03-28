package org.alexandrehd.persister;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.alexandrehd.persister.util.Cloner;

public class MapPersister extends MapIO {
	private HashMap<String, ?> itsSnapshot;

	public MapPersister(File root, int depth) {
		super(root);
		itsSnapshot = null;
	}
	
	/**	Persist a map. This will be saved incrementally by reference to the
	 	map we originally read (if any; if not, we'll attempt a read before
	 	saving).
	 	
	 	@param map the map to persist
	 	
	 	@throws IllegalArgumentException if the map contains values which we
	 		don't allow to be serialised
	 		
	 	@throws IOException
	 * @throws ClassNotFoundException 
	 */
	
	public void persist(HashMap<String, ?> map)
		throws IllegalArgumentException, IOException, ClassNotFoundException
	{
		if (itsSnapshot == null) {
			try { /*ignore*/ unpersist(); } catch (IOException _) { }
		}
		
		persistNode(map, itsSnapshot, getRootPath());
	}
	
	private void persistNode(HashMap<String, ?> map,
							 HashMap<String, ?> snapshot,
							 File location) {
		
	}
	
	/**	Unpersist a map from disk. Having done so, we keep a copy of the
	 	unpersisted map since this will be our template when doing difference
	 	calculations for any subsequent {@link #persist()} calls.
	 	
	 	@return the unpersisted map
	 	@throws IOException
	 	@throws ClassNotFoundException
	 */
	
	@SuppressWarnings("unchecked")
	public HashMap<String, ?> unpersist()
		throws IOException, ClassNotFoundException
	{
		HashMap<String, ?> m = new MapLoader(getRootPath()).loadFromRoot();
		itsSnapshot = new Cloner<HashMap<String, ?>>().deepCopy(m);
		return m;
	}
}
