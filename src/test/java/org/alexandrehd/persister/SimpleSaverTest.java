package org.alexandrehd.persister;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kill.sys.XManifest;

import org.alexandrehd.persister.SimpleSaver;
import org.junit.Test;

public class SimpleSaverTest {
	@Test
	public void testPersistSimpleMap() throws IOException, Exception {
		Map<String, Double> m = new HashMap<String, Double>();
		m.put("FOOBLE", 4.0);
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		SimpleSaver.persist((HashMap<String, ?>) m, f);
		
		assertEquals(m, SimpleSaver.unpersist(f));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBouncesBadTypes() throws IOException {
		Map<String, Date> m = new HashMap<String, Date>();
		m.put("FOO", new Date());
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		SimpleSaver.persist((HashMap<String, ?>) m, f);
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testThrowsFNF() throws Exception {
		/*ignore*/ SimpleSaver.unpersistAny(new File("/foo/bar/baz/bonk"));
	}
	
	static class Fooble<T> extends HashMap<String, T> {
		private static final long serialVersionUID = 1L;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCannotSaveHashMapSubclass() throws IOException {
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		SimpleSaver.persist(new Fooble<Double>(), f);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCannotSaveInnerHashMapSubclass() throws IOException {
		Fooble<Double> fd = new Fooble<Double>();
		HashMap<String, HashMap<String, Double>> m = new HashMap<String, HashMap<String, Double>>();
		m.put("FOOBLE", fd);
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		SimpleSaver.persist(m, f);
	}
	
	public void testCanPersistValidBaseType() throws Exception {
		double d = 4.5;
		File f = File.createTempFile("bogosave", ".tmpsaved");
		f.deleteOnExit();
		SimpleSaver.persistAny(d, f);
		
		Double d2 = (Double) SimpleSaver.unpersistAny(f);
		assertEquals(d, d2.doubleValue(), XManifest.EPSILON);
	}
}
