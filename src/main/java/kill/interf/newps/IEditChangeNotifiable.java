package mi.interf.newps;

/**	New, simple notification of edit changes. (The new buffer will always be reachable from the principal
 	preset.)
 	
 	@author nick
 */

public interface IEditChangeNotifiable {
	void editBufferHasChanged();
}
