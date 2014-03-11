package kill.presetmanager.newps;
import java.io.IOException;

import org.msgpack.MessagePack;

/**	Make generic maps/arrays/etc, suitable for packing, out of arbitrary objects.

 	@author nick
 */

public abstract class PackMaker implements IPackWalker {
	/**	Add an item to an existing pack.

		@param key the key - string for maps, integer for arrays
		@param item the item to add against the key
		@return this {@code PackMaker}
	 */

	abstract public PackMaker put(Object key, PackMaker item);
	
	/**	Turn this {@code PackMaker} into s raw structure of
	 	maps and arrays.

		@return the raw structure
	 */

	abstract public Object rawExpand();

	/**	Pack into MessagePack format.

		@return the packed data object in this PackMaker.
	 	@throws IOException 
	 */

	public byte[] pack() throws IOException {
		return new MessagePack().write(rawExpand());
	}
}
