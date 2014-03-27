package org.alexandrehd.persister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kill.sys.XManifest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MapSaverTest {
	//	Sweet: http://junit.org/javadoc/4.9/org/junit/rules/TemporaryFolder.html
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	private HashMap<String, HashMap<String, Object>> testMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("A", 1.0);
		m.put("B", 2.0);
		m.put("C", 3.0);
		//m.put("D", new double[] { 10, 11, 12 });
		
		HashMap<String, HashMap<String, Object>> m2 = new HashMap<String, HashMap<String, Object>>();
		
		m2.put("TOP", m);
		
		return m2;
	}
	
	private HashMap<String, HashMap<String, Object>> testMap2() {
		HashMap<String, HashMap<String, Object>> m = testMap();
		m.get("TOP").remove("B");
		m.get("TOP").put("X", 419);
		return m;
	}
	
	public void canPersistValidBaseType() throws Exception {
		double d = 4.5;
		File f = folder.newFile();
		new MapSaver(f).saveObjectToRoot(d);
		
		Double d2 = (Double) new MapLoader(f).loadObjectFromRoot();
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
	
	@Test
	public void savesFilesToDepth1() throws Exception {
		HashMap<String, HashMap<String, Object>> m = testMap();
		File f = folder.newFolder();
		File root = new File(f, "ROOT");
		assertTrue(root.mkdir());
		new MapSaver(root).saveToRoot(m, 1);
		
		assertTrue(root.isDirectory());
		assertTrue(new File(root, "TOP.ser").isFile());		
	}
	
    @Test
	public void persistMapToDepth() throws IOException, Exception {
		File f = folder.newFolder();
		File root = new File(f, "ROOT");
		
		HashMap<String, HashMap<String, Object>> m = testMap();

		new MapSaver(root).saveToRoot((HashMap<String, ?>) m, 99);
		assertEquals(m, new MapLoader(root).loadFromRoot());
	}
	
	@Test
	public void savingTreeDeletesFlatFile() throws Exception {
		HashMap<String, HashMap<String, Object>> m = testMap();
		File f = folder.newFolder();
		File root = new File(f, "ROOT");
		File ser = new File(f, "ROOT.ser");
		assertTrue(ser.createNewFile());
		
		new MapSaver(root).saveToRoot(m, 1);
		assertFalse(ser.exists());
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
