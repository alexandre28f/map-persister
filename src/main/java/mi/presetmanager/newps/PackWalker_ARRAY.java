package mi.presetmanager.newps;

import java.util.Map;

public class PackWalker_ARRAY implements IPackWalker {
	private IPackWalker[] itsArray;
	
	@SuppressWarnings("rawtypes")
	public PackWalker_ARRAY(Object[] obj) {
		itsArray = new IPackWalker[obj.length];
		
		for (int i = 0; i < obj.length; i++) {
			Object x = obj[i];
			
			if (x instanceof Map) {
				itsArray[i] = new PackWalker((Map) x);
			} else if (x instanceof Object[]) {
				itsArray[i] = new PackWalker_ARRAY((Object[]) x);
			} else {
				itsArray[i] = new PackWalker_LEAF(x);
			}
		}
	}

	@Override
	public IPackWalker walk00(String key) {
		throw new IllegalArgumentException(String.format("%s/walk00(%s)", getClass(), key));
	}

	@Override
	public IPackWalker walk00(int index) {
		return itsArray[index];
	}

	@Override
	public Object look() {
		throw new IllegalArgumentException(String.format("%s/look()", getClass()));
	}
}
