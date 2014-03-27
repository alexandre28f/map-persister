package org.alexandrehd.persister;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kill.sys.XManifest;

import org.alexandrehd.persister.MapSaver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MapSaverTest {
	//	Sweet: http://junit.org/javadoc/4.9/org/junit/rules/TemporaryFolder.html
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	public void canPersistValidBaseType() throws Exception {
		double d = 4.5;
		File f = folder.newFile();
		new MapSaver(f).saveAnyToRoot(d);
		
		Double d2 = (Double) new MapLoader(f).loadAnyFromRoot();
		assertEquals(d, d2.doubleValue(), XManifest.EPSILON);
	}

    @Test
	public void persistSimpleMapNoDepth() throws IOException, Exception {
		Map<String, Double> m = new HashMap<String, Double>();
		m.put("FOOBLE", 4.0);
		
		File f = folder.newFile();
		
		new MapSaver(f).saveToRoot((HashMap<String, ?>) m, 0);
		assertEquals(m, new MapLoader(f).loadFromRoot());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bouncesBadTypes() throws IOException {
		Map<String, Date> m = new HashMap<String, Date>();
		m.put("FOO", new Date());
		
		File f = folder.newFile();
		new MapSaver(f).saveToRoot((HashMap<String, ?>) m, 0);
	}
	
	static class Fooble<T> extends HashMap<String, T> {
		private static final long serialVersionUID = 1L;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void cannotSaveHashMapSubclass() throws IOException {
		new MapSaver(folder.newFile()).saveToRoot(new Fooble<Double>(), 0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void cannotSaveInnerHashMapSubclass() throws IOException {
		Fooble<Double> fd = new Fooble<Double>();
		HashMap<String, HashMap<String, Double>> m = new HashMap<String, HashMap<String, Double>>();
		m.put("FOOBLE", fd);
		
		new MapSaver(folder.newFile()).saveToRoot(m, 0);
	}
}
