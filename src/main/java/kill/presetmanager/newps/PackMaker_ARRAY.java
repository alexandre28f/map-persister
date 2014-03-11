package mi.presetmanager.newps;

import java.util.HashMap;
import java.util.Map;

public class PackMaker_ARRAY extends PackMaker {
	@SuppressWarnings("rawtypes")
	private PackMaker[] itsArray;
	
	public PackMaker_ARRAY(Object... items) {
		PackMaker[] a = new PackMaker[items.length];
		for (int i = 0; i < items.length; i++) {
			Object obj = items[i];
			if (obj instanceof PackMaker) {
				a[i] = (PackMaker) obj;
			} else {
				a[i] = new PackMaker_LEAF(obj);
			}
		}
		
		itsArray = a;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PackMaker put(Object key, PackMaker item) {
		if (key instanceof Integer) {
			itsArray[((Integer) key)] = item;
			return this;
		} else {
			throw new IllegalArgumentException(String.format("bad indexing: %s/put(%s)", getClass(), key));
		}
	}

	@Override
	public IPackWalker walk00(String key) {
		throw new IllegalArgumentException(String.format("walk00(int) called on %s", this));
	}

	@Override
	public IPackWalker walk00(int index) {
		Object item = itsArray[index];
		
		if (item instanceof IPackWalker) {
			return (IPackWalker) item;
		} else {
			return new PackMaker_LEAF(item);
		}
	}

	@Override
	public Object look() {
		throw new IllegalArgumentException(String.format("look() called on %s", this));
	}

	@Override
	public Object rawExpand() {
		Object[] a = new Object[itsArray.length];
		
		for (int i = 0; i < a.length; i++) {
			a[i] = itsArray[i].rawExpand();
		}
		
		return a;
	}
}
