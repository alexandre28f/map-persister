package org.alexandrehd.persister;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

public class MapLoaderTest {
	@Test(expected=FileNotFoundException.class)
	public void testThrowsFNF() throws Exception {
		/*ignore*/ new MapLoader(new File("/foo/bar/baz/bonk")).loadFromRoot();
	}
}
