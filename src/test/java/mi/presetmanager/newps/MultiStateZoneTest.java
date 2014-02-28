package mi.presetmanager.newps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import mi.basicdata.VectorParam;
import mi.interf.IEditState;
import mi.interf.newps.IZone;
import mi.interf.newps.IZoneableParameter;
import mi.presetmanager.EditBuffer;
import mi.util.TestingManifest;

import org.junit.Test;

public class MultiStateZoneTest {
	@Test
	public void testMorphing() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1.0f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 4.0f;
		
		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 1f, b2);
		
		assertEquals(2.5f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void zoneWillAutoExtend() {
		IZone v = new MultiStateZone(2);
		v.enableKeyState(2);
		v.disableKeyState(3);
		v.getDiscreteEditState00(4);
		assertFalse(v.keyStateIsEnabled(5));
	}
	
	@Test
	public void morphReturnsAnySingleEndpoint() {
		IZone v = new MultiStateZone(1);

		EditBuffer b1 = new EditBuffer();
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1.0f;

		v.setKeyState(0, 0, b1);
		
		assertEquals(1.0f, v.generateMorphedState(0.7f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void morphClonesKeyState() {
		IZone v = new MultiStateZone(1);

		EditBuffer b1 = new EditBuffer();
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1.0f;

		v.setKeyState(0, 0, b1);
		b1.scalars[0].x = 2.67f;
		
		assertEquals(1.0f, v.generateMorphedState(0.7f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void originalReturnStateIsMorphed() {
		IZone v = new MultiStateZone(1);

		EditBuffer b1 = new EditBuffer();
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1.0f;
		v.setKeyState(0, 0, b1);
		
		IEditState es = v.generateMorphedState(0.0f);
		es.getScalar(0).x = 4.7f;

		es = v.getDiscreteEditState00(0);
		assertEquals(1.0f, es.getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canInterpolateWhenEditStatesMissing() {
		IZone v = new MultiStateZone(3);
	
		EditBuffer b1 = new EditBuffer();
		b1.allowScalarParam(0);
		b1.scalars[0].x = 5.5f;
		v.setKeyState(1, 0f, b1);			//	No slot 0 or 2.

		assertEquals(5.5f, v.generateMorphedState(0.294f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionIfAllAbsent() {
		IZone v = new MultiStateZone(2);
		v.generateMorphedState(0.5f);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionIfAllDisabled() {
		IZone v = new MultiStateZone(2);
		v.setKeyState(0, 0f, new EditBuffer());
		v.setKeyState(1, 1f, new EditBuffer());
		v.disableKeyState(0);
		v.disableKeyState(1);
		v.generateMorphedState(0.5f);
	}
	
	@Test
	public void canMorphVectors() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowVectorParam(0);
		b1.vectors[0].x = new float[] { 1.0f, 10.0f };

		b2.allowVectorParam(0);
		b2.vectors[0].x = new float[] { 4.0f, 12.0f };
		
		v.setKeyState(0, 0.0f, b1);
		v.setKeyState(1, 1.0f, b2);
		
		assertArrayEquals(new float[] { 2.5f, 11.0f },
						  v.generateMorphedState(0.5f).getVector(0).x,
						  TestingManifest.JUNIT_DELTA
						 );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void signalsOnDifferentNumberOfParams() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b2.allowVectorParam(9);
		
		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 1f, b2);
		v.generateMorphedState(0.5f);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void vectorSizeMismatchSignals() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowVectorParam(0);
		b1.vectors[0].x = new float[] { 1.0f, 10.0f };

		b2.allowVectorParam(0);
		b2.vectors[0].x = new float[] { 4.0f, 12.0f, 9.5f };
		
		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 1f, b2);
		v.generateMorphedState(0.5f);
	}
	
	@Test
	public void morphingActuallyWorksProportionally() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;
		b1.allowVectorParam(0);
		b1.vectors[0].x = new float[] { 1.0f, 10.0f };

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		b2.allowVectorParam(0);
		b2.vectors[0].x = new float[] { 4.0f, 12.0f };
		
		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 1f, b2);
		
		IEditState morphed = v.generateMorphedState(0.25f);
		assertEquals(1.5f, morphed.getScalar(0).x, TestingManifest.JUNIT_DELTA);
		
		assertArrayEquals(new float[] { 1.75f, 10.5f },
						  morphed.getVector(0).x,
						  TestingManifest.JUNIT_DELTA
						 );
	}
	
	@Test
	public void morphsForNonEndPoints() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;
		b1.allowVectorParam(0);
		b1.vectors[0].x = new float[] { 1.0f, 10.0f };

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		b2.allowVectorParam(0);
		b2.vectors[0].x = new float[] { 4.0f, 12.0f };
		
		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 0.5f, b2);

		IEditState morphed = v.generateMorphedState(0.125f);
		assertEquals(1.5f, morphed.getScalar(0).x, TestingManifest.JUNIT_DELTA);
		
		assertArrayEquals(new float[] { 1.75f, 10.5f },
						  morphed.getVector(0).x,
						  TestingManifest.JUNIT_DELTA
						 );
	}
	
	@Test
	public void valuesCorrectBeyondSingleZonePoint() {
		IZone v = new MultiStateZone(1);
		EditBuffer b1 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		v.setKeyState(0, 0.25f, b1);
		
		assertEquals(1f, v.generateMorphedState(0f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(1f, v.generateMorphedState(1f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void valuesCorrectBeyondEndZones() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		
		v.setKeyState(0, 0.25f, b1);
		v.setKeyState(1, 0.75f, b2);
		
		assertEquals(1f, v.generateMorphedState(0f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(3f, v.generateMorphedState(1f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void interpolatesInnerKeyPoint() {
		IZone v = new MultiStateZone(3);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		EditBuffer b3 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		
		b3.allowScalarParam(0);
		b3.scalars[0].x = 2f;

		v.setKeyState(0, 0.25f, b1);
		v.setKeyState(1, 0.5f, b2);
		v.setKeyState(2, 0.75f, b3);
		
		assertEquals(3f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canDisableAndEnableInnerZone() {
		IZone v = new MultiStateZone(3);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		EditBuffer b3 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		
		b3.allowScalarParam(0);
		b3.scalars[0].x = 2f;

		v.setKeyState(0, 0.25f, b1);
		v.setKeyState(1, 0.5f, b2);
		v.setKeyState(2, 0.75f, b3);
		
		assertEquals(3f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);

		v.disableKeyState(1);
		assertEquals(1.5f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);

		v.enableKeyState(1);
		assertEquals(3f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canDisableAndEnableEndZone() {
		IZone v = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;
		
		v.setKeyState(0, 0.25f, b1);
		v.setKeyState(1, 0.75f, b2);
		
		assertEquals(3f, v.generateMorphedState(1f).getScalar(0).x, TestingManifest.JUNIT_DELTA);

		v.disableKeyState(1);
		assertEquals(1f, v.generateMorphedState(1f).getScalar(0).x, TestingManifest.JUNIT_DELTA);

		v.enableKeyState(1);
		assertEquals(3f, v.generateMorphedState(1f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void newLowZonePushesHigherOnes() {
		IZone v = new MultiStateZone(3);
		
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(0);
		b1.scalars[0].x = 1f;

		b2.allowScalarParam(0);
		b2.scalars[0].x = 3f;

		v.setKeyState(0, 0f, b1);
		v.setKeyState(2, 0f, b2);
		
		EditBuffer bx = new EditBuffer();
		bx.allowScalarParam(0);
		v.setKeyState(1, 0.5f, bx);		//	Pushes #2 upwards?

		v.disableKeyState(1);
		assertEquals(1f, v.generateMorphedState(0f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(3f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);	
	}
	
	@Test
	public void canCreateFromSingleZone() {
		SingleStateZone s = new SingleStateZone();
		
		IEditState state = new EditBuffer();
		state.allowVectorParam(4);
		VectorParam vp = new VectorParam();
		vp.x = new float[] { 1.1f, 9.9f };
		state.putVector(4, vp);
		s.setKeyState(0, 0, state);
		
		IZone m = new MultiStateZone(5, s);
		
		vp.x = new float[] { 4.6f, 9.99f };
		
		assertArrayEquals(new float[] { 1.1f, 9.9f },
						  m.generateMorphedState(0).getVector(4).x,
						  TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void zonesAutoExtend() {
		IZone v = new MultiStateZone(1);

		EditBuffer bx = new EditBuffer();
		bx.allowScalarParam(0);
		bx.scalars[0].x = 10f;
		
		v.setKeyState(0, 0f, bx);
		
		bx.scalars[0].x = 20f;
		v.setKeyState(2, 0f, bx);
		v.setZoneBarrier(2, 1f);

		assertEquals(15f, v.generateMorphedState(0.5f).getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}

	enum TestScalar implements IZoneableParameter {
		A(true), B(false);

		private boolean itsZoneable;
		TestScalar(boolean b) { itsZoneable = b; }
		@Override public boolean isZoneable() { return itsZoneable; }
	} 
	
	enum TestVector implements IZoneableParameter {
		A(false), B(true);

		private boolean itsZoneable;
		TestVector(boolean b) { itsZoneable = b; }
		@Override public boolean isZoneable() { return itsZoneable; }
	} 
	
	@Test
	public void zoningRespectsZoneableParameters() {
		IZone v = new MultiStateZone(2);
			//	p[0] of scalars is zoned (only); p[1] of vectors is zoned (only).
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		b1.allowScalarParam(1);
		b1.scalars[0].x = 1.0f;
		b1.scalars[1].x = 101f;
		
		b1.allowVectorParam(1);
		b1.vectors[0].x = new float[] { 1f, 11f };
		b1.vectors[1].x = new float[] { 2f, 22f };

		b2.allowScalarParam(1);
		b2.scalars[0].x = 4.0f;
		b2.scalars[1].x = 104f;
		
		b2.allowVectorParam(1);
		b2.vectors[0].x = new float[] { 3f, 33f };
		b2.vectors[1].x = new float[] { 4f, 44f };

		v.setKeyState(0, 0f, b1);
		v.setKeyState(1, 1f, b2);
		
		assertEquals(2.5f, v.generateMorphedState(TestScalar.class, TestVector.class, 0.5f, 0).getScalar(0).x,
						   TestingManifest.JUNIT_DELTA
						  );
		assertEquals(101f, v.generateMorphedState(TestScalar.class, TestVector.class, 0.5f, 0).getScalar(1).x,
						   TestingManifest.JUNIT_DELTA
						  );
		assertArrayEquals(new float[] { 1f, 11f },
						  v.generateMorphedState(TestScalar.class, TestVector.class, 0.5f, 0).getVector(0).x,
						  TestingManifest.JUNIT_DELTA
						 );
		assertArrayEquals(new float[] { 3f, 33f },
						  v.generateMorphedState(TestScalar.class, TestVector.class, 0.5f, 0).getVector(1).x,
						  TestingManifest.JUNIT_DELTA
				 		 );
	}
	
	enum Blatt { ZERO, ONE };
	@Test
	public void canDistributeSpecifiedState() {
		IZone v = new MultiStateZone(3);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		EditBuffer b3 = new EditBuffer();

		b1.allowScalarParam(1);
		b1.scalars[0].x =   0f;
		b1.scalars[1].x = 100f;
		b1.allowVectorParam(1);
		b1.vectors[0].x = new float[] { 0f, 0f };
		b1.vectors[1].x = new float[] { 100f, 100f };

		b2.allowScalarParam(1);
		b2.scalars[0].x =   1f;
		b2.scalars[1].x = 101f;
		b2.allowVectorParam(1);
		b2.vectors[0].x = new float[] { 1f, 1f };
		b2.vectors[1].x = new float[] { 101f, 101f };

		b3.allowScalarParam(1);
		b3.scalars[0].x =   2f;
		b3.scalars[1].x = 102f;
		b3.allowVectorParam(1);
		b3.vectors[0].x = new float[] { 2f, 2f };
		b3.vectors[1].x = new float[] { 102f, 102f };
		
		v.setKeyState(0, 0.0f, b1);
		v.setKeyState(1, 0.5f, b2);
		v.setKeyState(2, 1.0f, b3);
		
		v.distributeSpecifiedState(1, new Blatt[] { Blatt.ZERO }, new Blatt[] { Blatt.ONE });
		
		assertEquals(  1f, v.getDiscreteEditState00(0).getScalar(0).x, TestingManifest.JUNIT_DELTA);	//	Distributed.
		assertEquals(100f, v.getDiscreteEditState00(0).getScalar(1).x, TestingManifest.JUNIT_DELTA);	//	Untouched.
		assertEquals(  1f, v.getDiscreteEditState00(2).getScalar(0).x, TestingManifest.JUNIT_DELTA);	//	Distributed.
		assertEquals(102f, v.getDiscreteEditState00(2).getScalar(1).x, TestingManifest.JUNIT_DELTA);	//	Untouched.

		assertArrayEquals(new float[] { 0f, 0f },
						  v.getDiscreteEditState00(0).getVector(0).x,
						  TestingManifest.JUNIT_DELTA
						 );	//	Untouched.
		assertArrayEquals(new float[] { 101f, 101f },
				  		  v.getDiscreteEditState00(0).getVector(1).x,
				  		  TestingManifest.JUNIT_DELTA
				 		 );	//	Distributed.
		assertArrayEquals(new float[] { 2f, 2f },
						  v.getDiscreteEditState00(2).getVector(0).x,
						  TestingManifest.JUNIT_DELTA
				 		 );	//	Untouched.
		assertArrayEquals(new float[] { 101f, 101f },
						  v.getDiscreteEditState00(2).getVector(1).x,
						  TestingManifest.JUNIT_DELTA
		 		 		 );	//	Distributed.
		
		//	Ensure deep cloned:
		v.getDiscreteEditState00(2).getVector(1).x[0] = 219f;
		assertEquals(101f, v.getDiscreteEditState00(1).getVector(1).x[0], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canCopyEntireState() {
		IZone v = new MultiStateZone(3);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		EditBuffer b3 = new EditBuffer();

		b1.allowScalarParam(1);
		b1.scalars[0].x =   0f;
		b1.scalars[1].x = 100f;
		b1.allowVectorParam(1);
		b1.vectors[0].x = new float[] { 0f, 0f };
		b1.vectors[1].x = new float[] { 100f, 100f };

		b2.allowScalarParam(1);
		b2.scalars[0].x =   1f;
		b2.scalars[1].x = 101f;
		b2.allowVectorParam(1);
		b2.vectors[0].x = new float[] { 1f, 1f };
		b2.vectors[1].x = new float[] { 101f, 101f };

		b3.allowScalarParam(1);
		b3.scalars[0].x =   2f;
		b3.scalars[1].x = 102f;
		b3.allowVectorParam(1);
		b3.vectors[0].x = new float[] { 2f, 2f };
		b3.vectors[1].x = new float[] { 102f, 102f };
		
		v.setKeyState(0, 0.0f, b1);
		v.setKeyState(1, 0.5f, b2);
		v.setKeyState(2, 1.0f, b3);
		
		v.copyEntireState(1, 0);
		
		assertEquals(1f, v.getDiscreteEditState00(0).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(1f, v.getDiscreteEditState00(1).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(2f, v.getDiscreteEditState00(2).getScalar(0).x, TestingManifest.JUNIT_DELTA);
		
		assertArrayEquals(new float[] { 1f, 1f },
		  		  		  v.getDiscreteEditState00(0).getVector(0).x,
		  		  		  TestingManifest.JUNIT_DELTA
		 		 		 );
		assertArrayEquals(new float[] { 1f, 1f },
						  v.getDiscreteEditState00(1).getVector(0).x,
						  TestingManifest.JUNIT_DELTA
		 		 		 );
		assertArrayEquals(new float[] { 2f, 2f },
		  		  		  v.getDiscreteEditState00(2).getVector(0).x,
		  		  		  TestingManifest.JUNIT_DELTA
		 		 		 );
		
		//	Ensure deep cloned:
		v.getDiscreteEditState00(0).getVector(0).x[0] = 219f;
		assertEquals(101f, v.getDiscreteEditState00(1).getVector(1).x[0], TestingManifest.JUNIT_DELTA);
	}
	
	enum SomeArbitraryParamType implements IZoneableParameter {
		ZONEABLE_1(true), NONZONEABLE_2(false), ZONEABLE_3(true), NONEZONABLE_4(false);

		private boolean itsZoneable;
		private SomeArbitraryParamType(boolean zoneable) { itsZoneable = zoneable; }

		@Override
		public boolean isZoneable() { return itsZoneable; }
	}
	
	enum Always implements IZoneableParameter {
		_;
		@Override public boolean isZoneable() { return true; }
	}
	
	/**	Newer design with tagged enums for the parameters: check that we can generate
	 	a complete edit state for an interpolation, given an enum type (subclassing
	 	{@link IZoneableParameter}.
	 	
	 	Also tests for different default edit states for non-zoneables. */

	@Test
	public void canInterpolateWithSomeEnumsZoneable() {
		IZone zone = new MultiStateZone(2);
		EditBuffer b1 = new EditBuffer();
		EditBuffer b2 = new EditBuffer();
		
		SomeArbitraryParamType[] paramEnums = SomeArbitraryParamType.values();

		b1.allowScalarParam(paramEnums.length - 1);
		b2.allowScalarParam(paramEnums.length - 1);

		//	Set endpoints as 0.0...1.0 for every param. 
		for (int i = 0; i < paramEnums.length; i++) {
			b1.scalars[i].x = 0f;
			b2.scalars[i].x = 10f;
		}

		zone.setKeyState(0, 0.0f, b1);
		zone.setKeyState(1, 1.0f, b2);

		IEditState state0 = zone.generateMorphedState(SomeArbitraryParamType.class, Always.class, 0.5f, 0);
		IEditState state1 = zone.generateMorphedState(SomeArbitraryParamType.class, Always.class, 0.5f, 1);
		
		for (int i = 0; i < paramEnums.length; i++) {
			if (paramEnums[i].isZoneable()) {
				assertEquals(5f, state0.getScalar(i).x, TestingManifest.JUNIT_DELTA);
				assertEquals(5f, state1.getScalar(i).x, TestingManifest.JUNIT_DELTA);
			} else {
				assertEquals(0f, state0.getScalar(i).x, TestingManifest.JUNIT_DELTA);
				assertEquals(10f, state1.getScalar(i).x, TestingManifest.JUNIT_DELTA);
			}
		}
	}
}
