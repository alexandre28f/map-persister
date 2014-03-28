package kill.presetmanager.newps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import kill.interf.IEditState;
import kill.interf.newps.IEditChangeNotifiable;
import kill.interf.newps.IPrincipalPreset;
import kill.interf.newps.IPrincipalPresetManager;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.PrincipalPresetManager;
import kill.presetmanager.newps.SecondaryPresetManager;
import kill.util.IMultiLinePrinter;
import kill.util.TestingManifest;

import org.alexandrehd.persister.legacy.VectorParam;
import org.alexandrehd.persister.util.Cloner;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**	The secondary preset manager bears a strong resemblance to the legacy {@link PresetBank} (except
 	that there's only one edit buffer), so many of these tests are pretty much duplicates
 	of the PresetBank tests. Rather than getting the edit buffer directly,
 	{@link SecondaryPresetManager} calls a listener to get the edit state.
 	
 	<P>Note: many of these tests actually exercise the edit buffer, which at some stage
 	soon we need to factor out. We're copying many of the actual preset manager tests
 	into PrincipalPresetManager (which will eventually be the only kind).
 	
	@author nick
 */

@RunWith(JMock.class)
public class SecondaryPresetManagerTest {
	Mockery itsContext = new JUnit4Mockery();

	private IPrincipalPresetManager makeMan() {
		PrincipalPresetManager pm = new PrincipalPresetManager(1, 1);
		pm.setNotifiables(new HashSet<IEditChangeNotifiable>());
		IPrincipalPreset eb = pm.examineEditBuffer();
		eb.getMorphingVector(0).setKeyState(0, 0, new EditBuffer());
		pm.switchToBufferForZone(0);
		return pm;
	}
	
	private SecondaryPresetManager make() {
		return new SecondaryPresetManager(0, 10, makeMan(), null, null);
	}
	
	@Test
	public void getEditBufferOK() {
		SecondaryPresetManager b = make();
		b.examineEditBuffer();
	}
	
	@Test
	public void initialEditBufferNotLinkedToPreset() {
		SecondaryPresetManager pb = make();
		
		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void loadedEditBufferLinkedToPreset() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		pb.doRetrievePreset(5, 1);

		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void storingEditBufferToNewUpdatesSlotLinkage() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.

		assertArrayEquals(new int[] { 5, 0 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void storingEditBufferByReplaceUpdatesSlotLinkage() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());

		pb.replacePreset(5, 0);

		assertArrayEquals(new int[] { 5, 0 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void clearingPresetSlotUnlinksEditBuffer() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());

		pb.removeAllVersions(5);

		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void removingIntermediateSlotUpdatesEditBuffer() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		pb.storeNewPresetVersion(5);		//	Version 2.

		assertArrayEquals(new int[] { 5, 2 }, pb.getSelectionOriginalLocation());

		pb.deleteVersion(5, 1);

		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void clearAllResetsEditBuffers() {
		SecondaryPresetManager pb = make();
		pb.storeNewPresetVersion(4);
		
		assertArrayEquals(new int[] { 4, 0 }, pb.getSelectionOriginalLocation());

		pb.clearAll();

		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}

	@Test
	public void canResize() {
		SecondaryPresetManager pb = make();
		float[] colour = new float[] { 1.1f, 2.2f, 3.3f };
		pb.setColour(5, colour);
		
		pb.setNumPresetSlots(40);
		
		float[] c2 = pb.getColour(5);
		
		for (int i = 0; i < colour.length; i++) {
			assertEquals(colour[i], c2[i], TestingManifest.JUNIT_DELTA);
		}
		
		pb.storeNewPresetVersion(39);
	}
	
	@Test
	public void simpleScalarStore() {
		SecondaryPresetManager bank = make();
		bank.setNumScalarParams(6);
		bank.handleScalarLocally(5, 5.6f);
		bank.storeNewPresetVersion(0);
		
		bank.handleScalarLocally(5, 456.7432f);
		
		bank.doRetrievePreset(0, 0);
		IEditState eb2 = bank.examineEditBuffer();
		assertEquals(5.6f, eb2.getScalar(5).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void replaceOnEmptySlot() {
		SecondaryPresetManager bank = make();
		bank.setNumScalarParams(6);
		bank.handleScalarLocally(5, 5.6f);
		bank.replacePreset(0, 0);
		
		bank.handleScalarLocally(5, 456.7432f);
		
		bank.doRetrievePreset(0, 0);
		IEditState eb2 = bank.examineEditBuffer();
		assertEquals(5.6f, eb2.getScalar(5).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void simpleVectorStore() {
		SecondaryPresetManager bank = make();
		bank.setNumVectorParams(6);
		float[] f1 = new float[] { 5.6f, 7.8f };
		bank.handleVectorLocally(5, f1);
		bank.storeNewPresetVersion(0);
		bank.doRetrievePreset(0, 0);

		IEditState eb2 = bank.examineEditBuffer();
		
		for (int i = 0; i < 2; i++) {
			assertEquals(f1[i],
						 eb2.getVector(5).x[i],
						 TestingManifest.JUNIT_DELTA
						);
		}
	}
	
	@Test
	public void editBufferIsClonedFromStore() {
		SecondaryPresetManager bank = make();
		bank.setNumScalarParams(6);
		bank.handleScalarLocally(5, 5.5f);
		bank.storeNewPresetVersion(0);

		bank.doRetrievePreset(0, 0);
		bank.handleScalarLocally(5, 9.99f);

		//	Check that we get back an original state.
		bank.doRetrievePreset(0, 0);
		IEditState eb = bank.examineEditBuffer();
		assertEquals(5.5f, eb.getScalar(5).x, TestingManifest.JUNIT_DELTA);
		
		bank.handleScalarLocally(5, 8.88f);
		
		//	Check that we didn't just modify the stored state.
		bank.doRetrievePreset(0, 0);
		eb = bank.examineEditBuffer();
		assertEquals(5.5f, eb.getScalar(5).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void editBufferLinkageFollowsRetrievedVersion() {
		SecondaryPresetManager b = make();
		b.setNumPresetSlots(20);
		b.storeNewPresetVersion(12);

		assertArrayEquals(new int[] { 12, 0 }, b.getSelectionOriginalLocation());

		b.storeNewPresetVersion(12);

		assertArrayEquals(new int[] { 12, 1 }, b.getSelectionOriginalLocation());

		b.storeNewPresetVersion(12);

		assertArrayEquals(new int[] { 12, 2 }, b.getSelectionOriginalLocation());
	}
	
	@Test
	public void serializationOK() {
		SecondaryPresetManager b = make();
		b.setNumScalarParams(1);
		b.handleScalarLocally(0, 1.1f);
		b.storeNewPresetVersion(0);
		b.handleScalarLocally(0, 2.2f);
		b.storeNewPresetVersion(7);
		
		SecondaryPresetManager b2 = new Cloner<SecondaryPresetManager>().deepCopy(b);
		b2.reconstitute(makeMan(), 0, null);
		
		b.handleScalarLocally(0, 9.9f);
		b.storeNewPresetVersion(0);
		
		b2.handleDirectedToEditState(new EditBuffer());
		b2.doRetrievePreset(7, 0);
		IEditState state = b2.examineEditBuffer();
		assertEquals(2.2f, state.getScalar(0).x, TestingManifest.JUNIT_DELTA);
		b2.doRetrievePreset(0, 0);
		state = b2.examineEditBuffer();
		assertEquals(1.1f, state.getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void numPresetsOK() {
		assertEquals(13, new SecondaryPresetManager(0, 13, makeMan(), null, null).getNumPresetSlots());
	}
	
	@Test
	public void clearAll() {
		SecondaryPresetManager b = make();
		b.storeNewPresetVersion(0);
		assertEquals(1, b.getNumVersions(0));
		b.clearAll();
		assertEquals(0, b.getNumVersions(0));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void cannotSetNameIfEmpty() {
		make().setName(0, 0, "fooble");
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void cannotGetNameIfEmpty() {
		make().getName(0, 0);
	}
	
	@Test
	public void setNameOK() {
		SecondaryPresetManager b = make();
		b.storeNewPresetVersion(0);
		b.setName(0, 0, "fooble");
		assertEquals("fooble", b.getName(0, 0));
	}
	
	//	Tests for preset extensibility.
	
	@Test
	public void canExtendScalars() {
		SecondaryPresetManager bank = make();
		
		bank.setNumScalarParams(1);
		bank.handleScalarLocally(0, 1.1f);
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(1, 4.9f);
		
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(1.1f, state.getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(4.9f, state.getScalar(1).x, TestingManifest.JUNIT_DELTA);
	}

	@Test
	public void canExtendVectors() {
		SecondaryPresetManager bank = make();
		
		bank.setNumVectorParams(1);
		bank.handleVectorLocally(0, new float[] { 1.1f });
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(1, new float[] { 4.9f });
		
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(1.1f, state.getVector(0).x[0], TestingManifest.JUNIT_DELTA);
		assertEquals(4.9f, state.getVector(1).x[0], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void scalarsExtendWithHoles() {
		SecondaryPresetManager bank = make();
		
		bank.setNumScalarParams(1);
		bank.handleScalarLocally(0, 1.1f);
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(2, 4.9f);
		
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(1.1f, state.getScalar(0).x, TestingManifest.JUNIT_DELTA);
		assertEquals(0.0f, state.getScalar(1).x, TestingManifest.JUNIT_DELTA);
		assertEquals(4.9f, state.getScalar(2).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void vectorsExtendWithHoles() {
		SecondaryPresetManager bank = make();
		
		bank.setNumVectorParams(1);
		bank.handleVectorLocally(0, new float[] { 1.1f });
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(2, new float[] { 4.9f });
		
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(1.1f, state.getVector(0).x[0], TestingManifest.JUNIT_DELTA);
		assertEquals(0, state.getVector(1).x.length);
		assertEquals(4.9f, state.getVector(2).x[0], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void extendedParametersRemainTransient() {
		SecondaryPresetManager bank = make();
		
		bank.setNumScalarParams(1);
		bank.handleScalarLocally(0, 1.1f);
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(1, 4.9f);
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(4.9f, state.getScalar(1).x, TestingManifest.JUNIT_DELTA);

		bank.setTransientParameter(1, 5.6f);
		bank.doRetrievePreset(0, 0);
		state = bank.examineEditBuffer();
		assertEquals(5.6f, state.getScalar(1).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void extendedVectorsAreClonedFromTemplate() {
		SecondaryPresetManager bank = make();
		
		bank.storeNewPresetVersion(0);

		bank.setTransientParameter(0, new float[] { 4.9f });
		
		bank.doRetrievePreset(0, 0);
		IEditState state = bank.examineEditBuffer();
		assertEquals(4.9f, state.getVector(0).x[0], TestingManifest.JUNIT_DELTA);
		
		state.getVector(0).x[0] = 8.9f;		//	Possibly corruption inside preset bank.
		
		bank.doRetrievePreset(0, 0);
		state = bank.examineEditBuffer();
		assertEquals(4.9f, state.getVector(0).x[0], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canRetrieveNewPresetByUUID() {
		SecondaryPresetManager bank = make();
		
		UUID id = bank.storeNewPresetVersion(7);
	
		int[] position = bank.findByUUID00(id);
		
		assertEquals(position[0], 7);
		assertEquals(position[1], 0);			//	Initial revision.
		
		id = bank.storeNewPresetVersion(7);
		
		position = bank.findByUUID00(id);
		
		assertEquals(position[0], 7);
		assertEquals(position[1], 1);			//	Second revision.
	}
	
	@Test
	public void canLookupUUID() {
		SecondaryPresetManager bank = make();
		
		UUID id = bank.storeNewPresetVersion(7);
		UUID id2 = bank.getUUID(7, 0);			//	Slot 7, initial version.
		assertEquals(id, id2);
	}
	
	@Test
	public void trackLastSlotWritten() {
		SecondaryPresetManager bank = make();
		bank.storeNewPresetVersion(7);
		
		assertEquals(7, bank.getLastSlotAccessed());
	}
	
	@Test
	public void trackLastSlotRead() {
		SecondaryPresetManager bank = make();

		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(8);

		bank.doRetrievePreset(7, 1);
		assertEquals(7, bank.getLastSlotAccessed());
	}

	@Test
	public void trackLastSlotReplaced() {
		SecondaryPresetManager bank = make();

		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(9);
		bank.replacePreset(7, 0);

		assertEquals(7, bank.getLastSlotAccessed());
	}
	
	@Test
	public void canPrettyPrint() {
		SecondaryPresetManager bank = make();
		final List<String> lines = new ArrayList<String>();

		bank.prettyprint(new IMultiLinePrinter() {
			@Override
			public void printLine(String line) {
				lines.add(line);
			}
		});
		
		assertEquals(lines.get(1), "    slot 0: ---");
	}
	
	@Test
	public void canExpandBufferForNewParameters() {
		SecondaryPresetManager manager = new SecondaryPresetManager(0, 1, makeMan(), null, null);
		manager.handleScalarLocally(14, 3.4f);
		manager.handleVectorLocally(9, new float[] { 3.4f });
	}
	
	@Test
	public void presetRetrievalTriggersListener() {
		final IEditChangeNotifiable listener = itsContext.mock(IEditChangeNotifiable.class);
		
		itsContext.checking(new Expectations() {{
			one(listener).editBufferHasChanged();
		}});
		
		SecondaryPresetManager manager = new SecondaryPresetManager(0, 10, makeMan(), listener, null);
		manager.storeNewPresetVersion(5);		//	Default edit state?
		manager.doRetrievePreset(5, 0);
	}
	
	@Test
	public void canExamineVectorParameter() {
		SecondaryPresetManager man = new SecondaryPresetManager(0, 10, makeMan(), null, null);
		man.handleVectorLocally(14, new float[] { 1.1f, 2.2f });
		VectorParam vp = man.examineVectorParameter00(14);
		assertEquals(1.1f, vp.x[0], TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void cannotExamineVectorParameterOutOfRange() {
		SecondaryPresetManager man = new SecondaryPresetManager(0, 10, makeMan(), null, null);
		VectorParam vp = man.examineVectorParameter00(14);
		assertNull(vp);
	}
	
	@Test
	public void canRetrieveByUUID() {
		SecondaryPresetManager man = new SecondaryPresetManager(0, 10, makeMan(), null, null);
		man.storeNewPresetVersion(5);
		UUID id = man.getUUID(5, 0);
		int[] pos = man.findByUUID00(id);
		
		assertEquals(5, pos[0]);
		assertEquals(0, pos[1]);
	}
	
	@Test
	public void alteringCorrectEditBuffer() {
		PrincipalPresetManager pm = new PrincipalPresetManager(1, 2);
		pm.setNotifiables(new HashSet<IEditChangeNotifiable>());
		IPrincipalPreset eb = pm.examineEditBuffer();
		eb.getMorphingVector(0).setKeyState(0, 0, new EditBuffer());
		eb.getMorphingVector(1).setKeyState(0, 0, new EditBuffer());
		pm.switchToBufferForZone(0);

		//	SPM at location 1.
		SecondaryPresetManager man = new SecondaryPresetManager(1, 10, pm, null, null);
		man.handleScalarLocally(5, 4.7f);

		IEditState state = eb.getMorphingVector(0).getDiscreteEditState00(0);
		assertEquals(0, state.getNumScalarParams());
		
		state = eb.getMorphingVector(1).getDiscreteEditState00(0);
		assertEquals(6, state.getNumScalarParams());
	}
}
