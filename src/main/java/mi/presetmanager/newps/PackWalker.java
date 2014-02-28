package mi.presetmanager.newps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessagePack;

import com.fasterxml.jackson.databind.ObjectMapper;


/**	Examine a packed object.

	@author nick
 */

public class PackWalker implements IPackWalker {
	@SuppressWarnings("rawtypes")
	private Map itsMap;
	/**	Create an examiner over message-packed object b.
		@param f the message-packed object

		@throws IOException 
	 */

	@SuppressWarnings("rawtypes")
	public PackWalker(File f) throws IOException {
		this(new ObjectMapper().readValue(f, Map.class));
	}
	
	@SuppressWarnings("rawtypes")
	public PackWalker(Map m) {
		itsMap = m;
	}

	/**	Walk down a string key in a map.

		@param key the key value
		@return the next {@link IPackWalker} down, or null if key not found
	 */

	@Override
	public IPackWalker walk00(String key) {
		Object obj00 = itsMap.get(key);
		
		if (obj00 == null) {
			return null;
		} else if (obj00 instanceof Map) {
			return new PackWalker((Map) obj00);
		} else if (obj00 instanceof Object[]) {
			return new PackWalker_ARRAY((Object[]) obj00);
		} else {
			return new PackWalker_LEAF(obj00);
		}
	}

	@Override
	public IPackWalker walk00(int index) {
		throw new IllegalArgumentException(String.format("%s/walk00(%d)", getClass(), index));
	}

	@Override
	public Object look() {
		throw new IllegalArgumentException(String.format("%s/look()", getClass()));
	}
}
