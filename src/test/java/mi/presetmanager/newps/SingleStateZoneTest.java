package mi.presetmanager.newps;

import static org.junit.Assert.*;
import mi.interf.IEditState;
import mi.interf.newps.IZone;
import mi.presetmanager.EditBuffer;
import mi.util.TestingManifest;

import org.alexandrehd.presetter.legacy.VectorParam;
import org.junit.Test;

public class SingleStateZoneTest {
	@Test
	public void initialisesWithState() {
		IZone zone = new SingleStateZone();
		IEditState state = zone.getDiscreteEditState00(3);
		assertEquals(0, state.getNumScalarParams());
		assertEquals(0, state.getNumVectorParams());
	}
	
	@Test
	public void returnsOnlyState() {
		IZone zone = new SingleStateZone();
		IEditState state = new EditBuffer();
		state.allowVectorParam(4);
		VectorParam vp = new VectorParam();
		vp.x = new float[] { 1.1f, 9.9f };
		state.putVector(4, vp);
		zone.setKeyState(2, 0, state);
		
		vp.x = new float[] { 4.4f, 7.89f };
		
		assertArrayEquals(new float[] { 1.1f, 9.9f },
						  zone.generateMorphedState(0.7f).getVector(4).x,
						  TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canCreateFromMultiZone() {
		MultiStateZone m = new MultiStateZone(2);

		IEditState state = new EditBuffer();
		state.allowVectorParam(4);
		VectorParam vp = new VectorParam();
		vp.x = new float[] { 1.1f, 9.9f };
		state.putVector(4, vp);
		m.setKeyState(0, 0, state);

		IZone zone = new SingleStateZone(m);
		
		assertArrayEquals(new float[] { 1.1f, 9.9f },
				  		  zone.getDiscreteEditState00(0).getVector(4).x,
				  		  TestingManifest.JUNIT_DELTA);
	}
}
