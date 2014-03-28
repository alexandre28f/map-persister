package org.alexandrehd.persister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kill.sys.XManifest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MapPersisterTest {
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
	
	@Test(expected=FileNotFoundException.class)
	public void throwsFileNotFoundException() throws Exception {
		MapPersister saver = new MapPersister(new File(folder.getRoot(), "abcde"), 0);
		/*ignore*/ saver.unpersist();
	}
	
	@Test
	public void testWorksWithNoNesting() throws Exception {
		File f = folder.newFile("abcde");
		MapPersister saver = new MapPersister(f, 0);
		
		saver.persist(testMap());
		
		File fullF = new File(f.getParentFile(), f.getName() + ".ser");
		assertTrue("serialised file does not exist", fullF.isFile());
	}
	
	@Test
	public void persistsWithSingleLevelNesting() throws Exception {
		File f = new File(folder.getRoot(), "temp");
		
		MapPersister saver = new MapPersister(f, 1);
		
		saver.persist(testMap());
		
		assertTrue("top directory does not exist", f.isDirectory());
		
		File ser = new File(f, "TOP.ser");
		assertTrue("serialised file exists", ser.isFile());
		
		assertEquals(3.0,
					 ((Double) new MapPersister(f, 0).unpersist().get("C")).doubleValue(),
					 XManifest.EPSILON
					);
	}

	@Test
	public void persistsWithDoubleLevelNesting() throws Exception {
		File f = new File(folder.getRoot(), "temp2");
		
		MapPersister saver = new MapPersister(f, 2);
		
		saver.persist(testMap());
		
		assertTrue("top directory does not exist", f.isDirectory());
		
		File d = new File(f, "TOP");
		assertTrue("inner directory does not exist", d.isDirectory());

		File ser = new File(d, "C.ser");
		assertTrue("serialised file does not exist", ser.isFile());
	}
	
	@Test
	public void testCanUnpersistUnnested() throws Exception {
		File f = new File(folder.getRoot(), "temp2");

		MapPersister saver = new MapPersister(f, 0);
		saver.persist(testMap());
		assertEquals(testMap(), saver.unpersist());
	}
	
	@Ignore
	public void testCanUnpersistNested() throws Exception {
		File f = new File(folder.getRoot(), "a");
		MapPersister saver = new MapPersister(f, 2);
		saver.persist(testMap());
		assertEquals(testMap(), saver.unpersist());
	}
	
	@Ignore
	public void willRemoveFlatSaveOnNestedSave() throws Exception {
		File f = new File(folder.getRoot(), "a");
		MapPersister saver = new MapPersister(f, 0);
		saver.persist(testMap());

		saver = new MapPersister(f, 2);
		saver.persist(testMap2());
		
		assertEquals(testMap2(), saver.unpersist());
		
	}
	
	@Test
	public void canPersistIntoExistingPersistedDirectory() throws Exception {
		File f = new File(folder.getRoot(), "a");
		MapPersister saver = new MapPersister(f, 2);
		saver.persist(testMap());
		saver.persist(testMap());
	}

	@Test
	public void persistIntoExistingDirectoryWillNotOverwrite() throws Exception {
		File f = new File(folder.getRoot(), "a");
		MapPersister saver = new MapPersister(f, 1);
		saver.persist(testMap());
		
		File f2 = new File(f, "TOP.ser");
		long mod1 = f2.lastModified();
		assertTrue("expecting serialised file", mod1 != 0L);
		
		File f3 = new File(f, "_tmp_");
		f3.createNewFile();

		Thread.sleep(100);
		saver.persist(testMap());
		assertTrue("expecting same file (by last modified)", f2.lastModified() == mod1);
		assertTrue("expecting marker file to exist", f3.exists());
	}
	
	@Test
	public void entryIsRemoved() throws Exception {
		File f = new File(folder.getRoot(), "a");
		MapPersister saver = new MapPersister(f, 2);
		saver.persist(testMap());
		
		File B_ser = new File(new File(f, "TOP"), "B.ser");
		assertTrue("entry is present", B_ser.exists());

		HashMap<String, HashMap<String, Object>> m2 = testMap();
		m2.get("TOP").remove("B");
		saver.persist(m2);
		assertFalse("entry has been removed", B_ser.exists());
	}
	
	
	static class Fooble<T> extends HashMap<String, T> {
		private static final long serialVersionUID = 1L;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bouncesBadTypes() throws Exception {
		Map<String, Date> m = new HashMap<String, Date>();
		m.put("FOO", new Date());
		
		File f = folder.newFile();
		new MapPersister(f, 0).persist((HashMap<String, ?>) m);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void cannotSaveHashMapSubclass() throws Exception {
		new MapPersister(folder.newFile(), 0).persist(new Fooble<Double>());
	}

	@Test(expected=IllegalArgumentException.class)
	public void cannotSaveInnerHashMapSubclass() throws Exception {
		Fooble<Double> fd = new Fooble<Double>();
		HashMap<String, HashMap<String, Double>> m = new HashMap<String, HashMap<String, Double>>();
		m.put("FOOBLE", fd);
		
		new MapPersister(folder.newFile(), 0).persist(m);
	}
	
	@Test
	public void unchangedMapWillNotOverwriteDir() throws Exception {
		File f = new File(folder.getRoot(), "a");
		File f_ser = new File(f.getParentFile(), "a.ser");
		MapPersister saver = new MapPersister(f, 2);

		saver.persist(testMap());
		assertTrue("expecting directory/1", f.isDirectory());
		assertFalse("not expecting flat file/1", f_ser.isFile());
		
		saver = new MapPersister(f, 0);
		saver.persist(testMap());
		assertTrue("expecting directory/2", f.isDirectory());
		assertFalse("not expecting flat file/2", f_ser.isFile());
	}

	@Test
	public void unchangedMapWillNotOverwriteFlat() throws Exception {
		File f = new File(folder.getRoot(), "a");
		File f_ser = new File(f.getParentFile(), "a.ser");
		MapPersister saver = new MapPersister(f, 0);

		saver.persist(testMap());
		assertFalse("not expecting directory/1", f.isDirectory());
		assertTrue("expecting flat file/1", f_ser.isFile());
		
		saver = new MapPersister(f, 2);
		saver.persist(testMap());
		assertFalse("not expecting directory/2", f.isDirectory());
		assertTrue("expecting flat file/2", f_ser.isFile());
	}

	@Test
	public void changedMapWillOverwriteDir() throws Exception {
		File f = new File(folder.getRoot(), "a");
		File f_ser = new File(f.getParentFile(), "a.ser");
		MapPersister saver = new MapPersister(f, 2);

		saver.persist(testMap());
		assertTrue("expecting directory/1", f.isDirectory());
		assertFalse("not expecting flat file/1", f_ser.isFile());
		
		HashMap<String, HashMap<String, Object>> m = testMap();
		m.get("TOP").put("GOO", 25.8);
		saver = new MapPersister(f, 0);
		saver.persist(m);
		assertFalse("not expecting directory/2", f.isDirectory());
		assertTrue("expecting flat file/2", f_ser.isFile());
	}

	@Test
	public void changedMapWillOverwriteFlat() throws Exception {
		File f = new File(folder.getRoot(), "a");
		File f_ser = new File(f.getParentFile(), "a.ser");
		MapPersister saver = new MapPersister(f, 0);

		saver.persist(testMap());
		assertFalse("not expecting directory/1", f.isDirectory());
		assertTrue("expecting flat file/1", f_ser.isFile());
		
		HashMap<String, HashMap<String, Object>> m = testMap();
		m.get("TOP").put("GOO", 25.8);
		saver = new MapPersister(f, 2);
		saver.persist(m);
		assertTrue("expecting directory/2", f.isDirectory());
		assertFalse("not expecting flat file/2", f_ser.isFile());
	}
}
