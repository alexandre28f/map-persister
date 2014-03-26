package org.alexandrehd.presetter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import kill.sys.XManifest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SpilledSaverTest {
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
	public void testThrowsFNF() throws Exception {
		SpilledSaver saver = new SpilledSaver(new File(folder.getRoot(), "abcde"), 0);
		/*ignore*/ saver.unpersist();
	}
	
	@Test(expected=IllegalStateException.class)
	public void cannotUnpersistFromNonSavedDirectory() throws Exception {
		File f = folder.newFolder("temp");
		SpilledSaver saver = new SpilledSaver(f, 0);
		/*ignore*/ saver.unpersist();	
	}


	@Test
	public void testWorksWithNoNesting() throws Exception {
		File f = folder.newFile("abcde");
		SpilledSaver saver = new SpilledSaver(f, 0);
		
		saver.persist(testMap());
		
		File fullF = new File(f.getParentFile(), f.getName() + ".ser");
		assertTrue("serialised file exists", fullF.isFile());
	}
	
	@Test
	public void persistsWithSingleLevelNesting() throws Exception {
		File f = new File(folder.getRoot(), "temp2");
		
		SpilledSaver saver = new SpilledSaver(f, 1);
		
		saver.persist(testMap());
		
		assertTrue("top directory exists", f.isDirectory());
		
		File ser = new File(f, "TOP.ser");
		assertTrue("serialised file exists", ser.isFile());
		
		assertEquals(3.0,
					 ((Double) SimpleSaver.unpersist(ser).get("C")).doubleValue(),
					 XManifest.EPSILON
					);
	}

	@Test
	public void persistsWithDoubleLevelNesting() throws Exception {
		File f = new File(folder.getRoot(), "temp2");
		
		SpilledSaver saver = new SpilledSaver(f, 2);
		
		saver.persist(testMap());
		
		assertTrue("top directory exists", f.isDirectory());
		
		File d = new File(f, "TOP");
		assertTrue("serialised file exists", d.isDirectory());

		File ser = new File(d, "C.ser");
		assertTrue("serialised file exists", ser.isFile());

		assertEquals(3.0,
				     ((Double) SimpleSaver.unpersistAny(ser)).doubleValue(),
				     XManifest.EPSILON
				    );
	}
	
	@Test
	public void testCanUnpersistUnnested() throws Exception {
		File f = new File(folder.getRoot(), "temp2");

		SpilledSaver saver = new SpilledSaver(f, 0);
		saver.persist(testMap());
		assertEquals(testMap(), saver.unpersist());
	}
	
	@Ignore
	public void testCanUnpersistNested() throws Exception {
		File f = new File(folder.getRoot(), "a");
		SpilledSaver saver = new SpilledSaver(f, 2);
		saver.persist(testMap());
		assertEquals(testMap(), saver.unpersist());
	}
	
	@Ignore
	public void willRemoveFlatSaveOnNestedSave() throws Exception {
		File f = new File(folder.getRoot(), "a");
		SpilledSaver saver = new SpilledSaver(f, 0);
		saver.persist(testMap());

		saver = new SpilledSaver(f, 2);
		saver.persist(testMap2());
		
		assertEquals(testMap2(), saver.unpersist());
		
	}
	
	@Test
	public void canPersistIntoExistingPersistedDirectory() throws Exception {
		File f = new File(folder.getRoot(), "a");
		SpilledSaver saver = new SpilledSaver(f, 2);
		saver.persist(testMap());
		saver.persist(testMap());
	}
	
	@Test(expected=IllegalStateException.class)
	public void willNotPersistIntoNonPresetDirectory() throws Exception {
		File f = new File(folder.getRoot(), "a");
		f.mkdir();		//	If the directory is created by other machinery (or manually),
						//	we should reject the persist() attempt.
		
		SpilledSaver saver = new SpilledSaver(f, 2);
		saver.persist(testMap());
	}
}
