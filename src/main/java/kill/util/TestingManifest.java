package kill.util;

/**	Testing manifest constants. This class is here, and not in the test tree, because PRIVATE requires
 	it, and the Maven build doesn't add the test classes (nor should it).
 	
	@author nick
 */

public interface TestingManifest {
	static final float JUNIT_DELTA = 0.0001f;			//	Purely for jUnit.
}
