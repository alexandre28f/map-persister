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
		
		new MapSaver(f).saveNode((HashMap<String, ?>) m, 0);
		assertEquals(m, new MapLoader(f).loadFromRoot());
	}
	
	@Test
	public void savesFilesToDepth1() throws Exception {
		HashMap<String, HashMap<String, Object>> m = testMap();
		File f = folder.newFolder();
		File root = new File(f, "ROOT");
		new MapSaver(root).saveNode(m, 1);
		
		assertTrue(root.isDirectory());
		assertTrue(new File(root, "TOP.ser").isFile());		
	}
	
    @Test
	public void persistMapToDepth() throws IOException, Exception {
		File f = folder.newFolder();
		File root = new File(f, "ROOT");
		
		HashMap<String, HashMap<String, Object>> m = testMap();

		new MapSaver(root).saveNode((HashMap<String, ?>) m, 99);
		assertEquals(m, new MapLoader(root).loadFromRoot());
	}
}
