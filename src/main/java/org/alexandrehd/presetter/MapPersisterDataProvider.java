/*
 * ISerializeableMapWrapper.java
 *
 * Feb 27, 2014 
 */
package org.alexandrehd.presetter;

import java.util.HashMap;
import java.util.Map;

/**
 * An interface for classes that provide and consume 
 * persistence safe maps as allowed by {@link org.alexandrehd.presetter.SimpleSaver}. 
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public interface MapPersisterDataProvider {
	
	/**
	 * Provide a persistence safe map.
	 * 
	 * By convention, this data should be provided via a deep clone.
	 * 
	 * @return A deeply cloned map to persist.
	 */
	public HashMap<String,?> provideMap();
	
	/**
	 * Consume a map provided by the implementing class 
	 * or another that is using a compatible map layout. 
	 * 
	 * @param map Unpersisted map to consume.
	 */
	public void consumeMap(Map<String,?> map);
}