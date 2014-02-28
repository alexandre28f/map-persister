package mi.presetmanager.newps;

import java.util.HashMap;
import java.util.Map;

public class PackMaker_MAP extends PackMaker {
	private Map<String, PackMaker> itsMap = new HashMap<String, PackMaker>();
	
	public PackMaker_MAP(Object... mapElements) {
		if (mapElements.length % 2 == 0) {
			for (int i = 0; i < mapElements.length - 1; i += 2) {
				Object object = mapElements[i + 1];
				if (!(object instanceof PackMaker)) { object = new PackMaker_LEAF(object); }
				put(mapElements[i], (PackMaker) object);
			}
		} else {
			throw new IllegalArgumentException(String.format("odd number of arguments while building %s", PackMaker.class));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PackMaker put(Object key, PackMaker item) {
		itsMap.put((String) key, item);
		return this;
	}

	@Override
	public IPackWalker walk00(String key) {
		Object item00 = itsMap.get(key);
		
		if (item00 == null) {
			return null;
		} else if (item00 instanceof IPackWalker) {
			return (IPackWalker) item00;
		} else {
			return new PackMaker_LEAF(item00);
		}
	}

	@Override
	public IPackWalker walk00(int index) {
		throw new IllegalArgumentException(String.format("walk00(int) called on %s", this));
	}

	@Override
	public Object look() {
		throw new IllegalArgumentException(String.format("look() called on %s", this));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object rawExpand() {
		Map m = new HashMap();
		
		for (Map.Entry<String, PackMaker> e: itsMap.entrySet()) {
			m.put(e.getKey(), e.getValue().rawExpand());
		}

		return m;
	}
}
