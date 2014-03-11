package mi.presetmanager.newps;

public class PackWalker_LEAF implements IPackWalker {
	private Object itsObject;

	public PackWalker_LEAF(Object x) {
		itsObject = x;
	}

	@Override
	public IPackWalker walk00(String key) {
		throw new IllegalArgumentException(String.format("%s/walk00(%s)", getClass(), key));
	}

	@Override
	public IPackWalker walk00(int index) {
		throw new IllegalArgumentException(String.format("%s/walk00(%d)", getClass(), index));
	}

	@Override
	public Object look() {
		return itsObject;
	}
}
