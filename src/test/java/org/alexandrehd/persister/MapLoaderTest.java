package org.alexandrehd.persister;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MapLoaderTest {
	//	Sweet: http://junit.org/javadoc/4.9/org/junit/rules/TemporaryFolder.html
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected=IOException.class)
	public void throwsIOException() throws Exception {
		/*ignore*/ new MapLoader(folder.newFile()).loadFromRoot();
	}
    
    @Test
    public void canLoadEmptyMap() throws ClassNotFoundException, IOException {
    	HashMap<String, ?> m = new MapLoader(folder.newFolder()).loadFromRoot();
    	assertEquals(0, m.size());
    }
}
