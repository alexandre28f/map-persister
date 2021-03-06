package kill.presetmanager.newps;

import static org.junit.Assert.*;
import kill.presetmanager.newps.PackMaker;
import kill.presetmanager.newps.PackMaker_MAP;

import org.junit.Test;

public class PackMaker_MAPTest {
	@Test
	public void packSimpleMap() {
		PackMaker p = new PackMaker_MAP("A", 1, "B", 2);
		assertEquals(1, p.walk00("A").look());
		assertEquals(2, p.walk00("B").look());
	}
}
