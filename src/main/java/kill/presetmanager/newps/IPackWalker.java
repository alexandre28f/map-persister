package mi.presetmanager.newps;

public interface IPackWalker {
	/**	Walk through a map: */
	public IPackWalker walk00(String string);
	
	/** Walk through an array: */
	public IPackWalker walk00(int index);
	
	/** Examine this node: */
	public Object look();
}
