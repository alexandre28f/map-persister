package kill.presetmanager.newps;

import static org.junit.Assert.assertEquals;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.MultiStateZone;
import kill.presetmanager.newps.ScaledZone;
import kill.util.TestingManifest;

import org.alexandrehd.persister.legacy.ScalarParam;
import org.junit.Test;

public class ScaledZoneTest {
	@Test
	public void interpolatesAtExtremes() {
		MultiStateZone z = new MultiStateZone(2);
		
		EditBuffer es0 = new EditBuffer();
		EditBuffer es1 = new EditBuffer();

		es0.putScalar(0, new ScalarParam(5.0f));
		es1.putScalar(0, new ScalarParam(10.0f));
		
		ScaledZone s = new ScaledZone(z);

		z.setEditBuffer(0, es0);
		z.setEditBuffer(1, es1);
		
		s.setZoneBarrier(0, 1000.0f);
		s.setZoneBarrier(1, 2000.0f);
		
		//	Non-domain calculation:
		assertEquals(7.5f,
					 z.generateMorphedState(0.5f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);
		
		//	Domain calculation:
		assertEquals(7.5f,
					 s.generateMorphedState(1500f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);
	}
	
	@Test
	public void generatesStateForSingleZone() {
		MultiStateZone z = new MultiStateZone(2);
		
		EditBuffer es0 = new EditBuffer();

		es0.putScalar(0, new ScalarParam(5.0f));
		
		ScaledZone s = new ScaledZone(z);

		z.setEditBuffer(0, es0);
		
		s.setZoneBarrier(0, 1000.0f);
		
		assertEquals(5.0f,
					 s.generateMorphedState(900f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);
		
		assertEquals(5.0f,
					 s.generateMorphedState(1100f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);
	}
	
	@Test
	public void generatesStateWhenSecondZoneDisabled() {
		MultiStateZone z = new MultiStateZone(2);
		
		EditBuffer es0 = new EditBuffer();
		EditBuffer es1 = new EditBuffer();

		es0.putScalar(0, new ScalarParam(5.0f));
		es1.putScalar(0, new ScalarParam(10.0f));
		
		ScaledZone s = new ScaledZone(z);

		z.setEditBuffer(0, es0);
		z.setEditBuffer(1, es1);
		
		s.setZoneBarrier(0, 1000.0f);
		s.setZoneBarrier(1, 2000.0f);
		
		z.disableKeyState(1);
		
		assertEquals(5.0f,
					 s.generateMorphedState(1500f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);
	}

	@Test
	public void generatesStateWhenEndZoneDisabled() {
		MultiStateZone z = new MultiStateZone(3);
		
		EditBuffer es0 = new EditBuffer();
		EditBuffer es1 = new EditBuffer();
		EditBuffer es2 = new EditBuffer();

		es0.putScalar(0, new ScalarParam(5.0f));
		es1.putScalar(0, new ScalarParam(10.0f));
		es2.putScalar(0, new ScalarParam(15.0f));
		
		z.setEditBuffer(0, es0);
		z.setEditBuffer(1, es1);
		z.setEditBuffer(2, es2);
		
		ScaledZone s = new ScaledZone(z);
		
		s.setZoneBarrier(0, 1000.0f);
		s.setZoneBarrier(1, 2000.0f);
		s.setZoneBarrier(2, 3000.0f);
		
		s.disableKeyState(2);

		assertEquals(7.5f,
					 s.generateMorphedState(1500f).getScalar(0).x,
					 TestingManifest.JUNIT_DELTA
					);		
	}
	
	@Test
	public void scaledZoneCanGrow() {
		MultiStateZone z = new MultiStateZone(3);
		ScaledZone s = new ScaledZone(z);

		s.setZoneBarrier(0, 1000f);
		s.setZoneBarrier(1, 2000f);
		s.setZoneBarrier(2, 3000f);
		
		s.setZoneBarrier(4, 4000f);
	}
}
