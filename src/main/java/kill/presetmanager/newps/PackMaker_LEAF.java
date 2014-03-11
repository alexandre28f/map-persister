package kill.presetmanager.newps;

public class PackMaker_LEAF extends PackMaker {
	private Object itsItem;

	public PackMaker_LEAF(Object item) {
		itsItem = item;
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
		return itsItem;
	}

	@Override
	public PackMaker put(Object key, PackMaker item) {
		throw new IllegalArgumentException(String.format("%s/put()", getClass()));
	}

	@Override
	public Object rawExpand() {
		return itsItem;
	}
}
