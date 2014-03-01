package org.alexandrehd.presetter;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestSaveableStatePersister {

	@Test
	public void testPersistAndUnpersist() throws Exception {
		float[][] twoD = new float[][] { new float[] { 1f }, new float[] { 2f }};
		Map<String, SaveableState> m = new HashMap<String, SaveableState>();
		m.put("A", new SaveableState(null, new float[] { 1.0f, 2.0f }, twoD));
		
		SaveableState state = new SaveableState(m, new float[] { 3f, 4f }, twoD);
		
		SaveableStatePersister p = new SaveableStatePersister();
		
		File f = File.createTempFile("bogosave", ".tmpsaved");
		System.out.println(f);
		
		p.persistState(state, f);
		
		SaveableState s2 = p.unpersistState(f);
		
		assertEquals(state, s2);
	}
}
