package org.alexandrehd.presetter;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.junit.Test;

public class SpilledSaverTest {
	private HashMap<String, HashMap<String, Double>> testMap() {
		HashMap<String, Double> m = new HashMap<String, Double>();
		m.put("A", 1.0);
		m.put("B", 2.0);
		m.put("C", 3.0);
		
		HashMap<String, HashMap<String, Double>> m2 = new HashMap<String, HashMap<String, Double>>();
		
		m2.put("TOP", m);
		
		return m2;
	}

	@Test
	public void testWorksWithNoNesting() throws Exception {
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		
		SpilledSaver saver = new SpilledSaver(f, 0);
		
		saver.persist(testMap());
		
		File fullF = new File(f.getParentFile(), f.getName() + ".ser");
		assertTrue("serialised file exists", fullF.isFile());
	}
	
	@Test
	public void testWorksWithSingleLevelNesting() throws Exception {
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.delete();
		
		SpilledSaver saver = new SpilledSaver(f, 1);
		
		saver.persist(testMap());
		
		assertTrue("top directory exists", f.isDirectory());
		
		File ser = new File(f, "TOP.ser");
		assertTrue("serialised file exists", ser.isFile());
	}

	@Test
	public void testWorksWithDoubleLevelNesting() throws Exception {
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.delete();
		
		SpilledSaver saver = new SpilledSaver(f, 2);
		
		saver.persist(testMap());
		
		assertTrue("top directory exists", f.isDirectory());
		
		File d = new File(f, "TOP");
		assertTrue("serialised file exists", d.isDirectory());

		File ser = new File(d, "A.ser");
		assertTrue("serialised file exists", ser.isFile());
	}
}
