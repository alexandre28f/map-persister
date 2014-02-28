package mi.presetmanager.newps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import mi.interf.IEditState;
import mi.interf.newps.IDeepCloneable;
import mi.interf.newps.IPresetFrame;
import mi.util.TestingManifest;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class PresetFrameTest {
	Mockery itsContext = new JUnit4Mockery();
	
	class MyInt implements IDeepCloneable<MyInt> {
		int myI;
		
		MyInt(int i) { myI = i; }
		
		@Override
		public MyInt deepClone() {
			return new MyInt(myI);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof MyInt && ((MyInt) other).myI == myI;
		}
	}
	
	private MyInt i(int val) { return new MyInt(val); }
	
	private <T extends IDeepCloneable<T>> PresetFrame<T> make() {
		return new PresetFrame<T>(10) {
			private static final long serialVersionUID = 1L;

			@Override
			public void doRetrievePreset(int presetSlot, int version) { }

			@Override
			public UUID storeNewPresetVersion(int presetSlot) { return null; }

			@Override
			public void replacePreset(int presetSlot, int version) { }

			@Override
			public T examineEditBuffer() { return null; }

			@Override
			public void pushEditState() { }
		};
	}

	@Test
	public void testStoreNewPreset() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		assertEquals(0, frame.getLastVersionAccessed(0));
	}

	@Test
	public void testRetrievePreset() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		assertEquals(i(34), frame.retrievePresetObject(0, 0));
		assertEquals(0, frame.getLastVersionAccessed(0));

		frame.storeNewPreset(i(34), 0);
		assertEquals(1, frame.getLastVersionAccessed(0));

		frame.storeNewPreset(i(34), 7);
		assertEquals(0, frame.getLastVersionAccessed(7));
}
	
	@Test
	public void storageIsCloned() {
		 class Foo implements IDeepCloneable<Foo> {
			 int a;
			 
			 Foo(int i) { a = i; }

			 @Override
			 public Foo deepClone() {
				 return new Foo(a);
			 }
		 	}

		 Foo x = new Foo(1);
		 
		 PresetFrame<Foo> frame = make();
		 frame.storeNewPreset(x, 0);
		 x.a = 2;
		 
		 Foo y = frame.retrievePresetObject(0, 0);
		 assertEquals(1, y.a);
	}

	@Test
	public void retrievalIsCloned() {
		 class Foo implements IDeepCloneable<Foo> {
			 int a;
			 
			 Foo(int i) { a = i; }

			 @Override
			 public Foo deepClone() {
				 return new Foo(a);
			 }
		 }

		 Foo x = new Foo(1);
		 
		 PresetFrame<Foo> frame = make();
		 frame.storeNewPreset(x, 0);
		 
		 Foo y = frame.retrievePresetObject(0, 0);
		 y.a = 2;
		 
		 y = frame.retrievePresetObject(0, 0);
		 assertEquals(1, y.a);
	}

	@Test
	public void testReplacePreset() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.replacePreset(i(42), 0, 0);
		assertEquals(i(42), frame.retrievePresetObject(0, 0));
	}

	@Test
	public void testRemoveAllVersions() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(111), 5);		//	Version 0.
		frame.storeNewPreset(i(222), 5);		//	Version 1.

		assertArrayEquals(new int[] { 5, 1 }, frame.getSelectionOriginalLocation());

		frame.removeAllVersions(5);
		assertEquals(0, frame.getNumVersions(5));
		assertArrayEquals(new int[] { -1, -1 }, frame.getSelectionOriginalLocation());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void cannotNameEmptySlot() {
		IPresetFrame<MyInt> frame = make();
		frame.setName(0, 0, "hello");
	}

	@Test
	public void testSetName() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.setName(0, 0, "hello");
	}

	@Test
	public void testGetName() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.setName(0, 0, "hello");
		assertEquals("hello", frame.getName(0, 0));
	}

	@Test
	public void testSetColour() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.setColour(0, new float[] { 0.1f, 0.2f, 0.3f });
	}

	@Test
	public void testGetColour() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.setColour(0, new float[] { 0.1f, 0.2f, 0.3f });
		float[] f = frame.getColour(0);
		assertEquals(0.1f, f[0], TestingManifest.JUNIT_DELTA);
		assertEquals(0.2f, f[1], TestingManifest.JUNIT_DELTA);
		assertEquals(0.3f, f[2], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void ensureColourIsCloned() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		frame.setColour(0, new float[] { 0.1f, 0.2f, 0.3f });
		float[] f = frame.getColour(0);

		frame.setColour(0, new float[] { 0.11f, 0.22f, 0.33f });
		
		assertEquals(0.1f, f[0], TestingManifest.JUNIT_DELTA);
		assertEquals(0.2f, f[1], TestingManifest.JUNIT_DELTA);
		assertEquals(0.3f, f[2], TestingManifest.JUNIT_DELTA);
		
		f = frame.getColour(0);
		f[0] = 9.9f;
		f = frame.getColour(0);
		assertEquals(0.11f, f[0], TestingManifest.JUNIT_DELTA);
	}

	@Test
	public void testGetDateStored() {
		PresetFrame<MyInt> frame = make();
		frame.storeNewPreset(i(34), 0);
		Date d = frame.getDateStored(0, 0);
		Date now = Calendar.getInstance().getTime();
		assertTrue(now.getTime() - d.getTime() < 1000);
	}

	@Test
	public void testGetNumPresetSlots() {
		IPresetFrame<MyInt> frame = make();
		assertEquals(10, frame.getNumPresetSlots());
	}

	@Test
	public void testSetNumPresetSlots() {
		PresetFrame<MyInt> frame = make();
		
		frame.storeNewPreset(i(34), 0);
		frame.setNumPresetSlots(15);
		assertEquals(15, frame.getNumPresetSlots());
		assertEquals(i(34), frame.retrievePresetObject(0, 0));
	}

	@Test
	public void testGetNumVersions() {
		PresetFrame<MyInt> frame = make();
		
		frame.storeNewPreset(i(34), 0);
		assertEquals(1, frame.getNumVersions(0));

		frame.storeNewPreset(i(56), 0);
		assertEquals(2, frame.getNumVersions(0));
	}

	@Test
	public void testDeletionOfSelectedVersion() {
		PresetFrame<MyInt> frame = make();
		
		assertEquals(-1, frame.getLastVersionAccessed(0));
		frame.storeNewPreset(i(34), 0);
		assertEquals(0, frame.getLastVersionAccessed(0));
		assertArrayEquals(new int[] { 0, 0 }, frame.getSelectionOriginalLocation());
		frame.deleteVersion(0, 0);
		assertEquals(-1, frame.getLastVersionAccessed(0));
		assertArrayEquals(new int[] { -1, -1 }, frame.getSelectionOriginalLocation());
	}
	
	@Test
	public void testSelectionShiftAfterDelete() {
		PresetFrame<MyInt> frame = make();
		
		frame.storeNewPreset(i(34), 0);
		assertEquals(0, frame.getLastVersionAccessed(0));
		frame.deleteVersion(0, 0);
		assertEquals(-1, frame.getLastVersionAccessed(0));
	}
	
	@Test
	public void testSelectionAfterClear() {
		PresetFrame<MyInt> frame = make();
		
		frame.storeNewPreset(i(34), 0);
		frame.clear(0);
		assertEquals(-1, frame.getLastVersionAccessed(0));
	}

	@Test
	public void testStateAfterClearAll() {
		PresetFrame<MyInt> frame = make();
		
		frame.storeNewPreset(i(34), 0);
		frame.storeNewPreset(i(45), 7);
		frame.storeNewPreset(i(45), 7);

		frame.clearAll();
		
		assertEquals(-1, frame.getLastVersionAccessed(0));
		assertEquals(0, frame.getNumVersions(0));
		assertEquals(0, frame.getNumVersions(7));
	}

	@Test
	public void testFindByUUID00() {
		PresetFrame<MyInt> frame = make();
		UUID id = frame.storeNewPreset(i(333), 3);
		int[] pos = frame.findByUUID00(id);
		assertArrayEquals(new int[] { 3, 0 }, pos);
	}

	@Test
	public void testGetUUID() {
		PresetFrame<MyInt> frame = make();
		UUID id = frame.storeNewPreset(i(333), 3);
		assertEquals(id, frame.getUUID(3, 0));
	}

	@Test
	public void trackLastSlotWritten() {
		PresetFrame<MyInt> frame = make();

		assertEquals(-1, frame.getLastSlotAccessed());
		frame.storeNewPreset(i(333), 7);
		assertEquals(7, frame.getLastSlotAccessed());
		assertEquals(0, frame.getLastVersionAccessed(7));
	}
	
	@Test
	public void trackLastSlotRead() {
		PresetFrame<MyInt> frame = make();

		frame.storeNewPreset(i(333), 7);
		frame.storeNewPreset(i(444), 7);
		frame.storeNewPreset(i(555), 8);

		frame.retrievePresetObject(7, 1);
		assertEquals(7, frame.getLastSlotAccessed());
		assertEquals(1, frame.getLastVersionAccessed(7));
	}

	@Test
	public void trackLastSlotReplaced() {
		PresetFrame<MyInt> frame = make();

		frame.storeNewPreset(i(333), 7);
		frame.storeNewPreset(i(555), 9);
		frame.replacePreset(i(444), 7, 0);

		assertEquals(7, frame.getLastSlotAccessed());
		assertEquals(0, frame.getLastVersionAccessed(7));

		frame.storeNewPreset(i(555), 9);
		
		frame.replacePreset(i(444), 9, 0);
		assertEquals(0, frame.getLastVersionAccessed(9));

		frame.replacePreset(i(444), 9, 1);
		assertEquals(1, frame.getLastVersionAccessed(9));
	}
}
