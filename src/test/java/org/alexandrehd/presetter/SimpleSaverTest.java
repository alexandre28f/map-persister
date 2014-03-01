package org.alexandrehd.presetter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SimpleSaverTest {
	@Test
	public void testPersistSimpleMap() throws IOException, Exception {
		Map<String, Double> m = new HashMap<String, Double>();
		m.put("FOOBLE", 4.0);
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		SimpleSaver.persist((HashMap<String, ?>) m, f);
		
		assertEquals(m, SimpleSaver.unpersist(f));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBouncesBadTypes() throws IOException {
		Map<String, Date> m = new HashMap<String, Date>();
		m.put("FOO", new Date());
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		SimpleSaver.persist((HashMap<String, ?>) m, f);
	}
}
