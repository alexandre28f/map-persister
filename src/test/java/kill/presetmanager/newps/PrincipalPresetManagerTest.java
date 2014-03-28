package kill.presetmanager.newps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.jar.Manifest;

import kill.interf.IEditState;
import kill.interf.newps.IEditChangeNotifiable;
import kill.interf.newps.IPrincipalPreset;
import kill.interf.newps.IPrincipalPresetManager;
import kill.interf.newps.ISecondaryPresetManager;
import kill.interf.newps.IZone;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.PresetFrame;
import kill.presetmanager.newps.PrincipalPresetManager;
import kill.presetmanager.newps.SecondaryPresetManager;
import kill.sys.XManifest;
import kill.util.IMultiLinePrinter;
import kill.util.TestingManifest;

import org.alexandrehd.persister.util.Cloner;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**	Many of these tests are for the preset machinery, which is a bit daft (we test that
 	already at the {@link PresetFrame} level) but we're starting to test the linkage to
 	{@link SecondaryPreset} clients.
 	
	@author nick
 */

@RunWith(JMock.class)
public class PrincipalPresetManagerTest {
	Mockery itsContext = new JUnit4Mockery();

	private PrincipalPresetManager make() {
		return new PrincipalPresetManager(10, 1);
	}
	
	@Test
	public void canConvertSecondaryBankPreservingEditBuffer() {
		IPrincipalPresetManager pman = new PrincipalPresetManager(1, 1);
		ISecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);
		sman.handleScalarLocally(5, 7.9f);
		sman.handleVectorLocally(9, new float[] { 1.1f, 2.2f, 3.3f });
		sman.storeNewPresetVersion(3);		//	Should be remembered on conversion as the last accessed.

		pman = new PrincipalPresetManager(sman, XManifest.MAX_SECONDARY_PRESETTERS);
		//	We should have associated it with the hidden panel:
		IEditState state = pman.getCurrentEditBuffer(XManifest.HIDDEN_PANEL);

		assertEquals(7.9f, state.getScalar(5).x, TestingManifest.JUNIT_DELTA);
		assertArrayEquals(new float[] { 1.1f, 2.2f, 3.3f }, state.getVector(9).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void secondaryBankConversionPopulatesPrincipals() {
		//	We initially got this wrong: we didn't actually put the (converted) presets into the principal
		//	bank!
		
		IPrincipalPresetManager pman = new PrincipalPresetManager(1, 1);
		ISecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);
		sman.handleScalarLocally(5, 7.9f);
		sman.handleVectorLocally(9, new float[] { 1.1f, 2.2f, 3.3f });
		sman.storeNewPresetVersion(3);		//	Should be remembered on conversion as the last accessed.

		pman = new PrincipalPresetManager(sman, XManifest.MAX_SECONDARY_PRESETTERS);

		IPrincipalPreset preset = pman.retrievePresetObject(3, 0);
		//	I don't care what's in place for the other panels (we need to test preservation of
		//	that information eventually) but the state for the VIP panel should be there
		//	(and this state shouldn't actually be zone-capable).
		IEditState state = preset.getSelectedEditStateForSecondary(XManifest.HIDDEN_PANEL);

		assertEquals(7.9f, state.getScalar(5).x, TestingManifest.JUNIT_DELTA);
		assertArrayEquals(new float[] { 1.1f, 2.2f, 3.3f }, state.getVector(9).x, TestingManifest.JUNIT_DELTA);
	}
	
	//	Check that preset club is present (even if unused).
	//	Check that we restore to the #10 secondary.
	//	Check morphing state (?).
	//	Dates are not preserved!

	@Test
	public void getEditBufferOK() {
		PrincipalPresetManager b = make();
		b.examineMorphState(0, 0.4f);
	}
	
	@Test
	public void initialEditStateNotLinkedToPreset() {
		PrincipalPresetManager pb = make();

		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void loadedEditStateLinkedToPreset() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		pb.retrievePresetObject(5, 1);
		
		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void storingEditStateToNewUpdatesSlotLinkage() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.

		assertArrayEquals(new int[] { 5, 0 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void storingEditStateByReplaceUpdatesSlotLinkage() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
		
		pb.replacePreset(5, 0);

		assertArrayEquals(new int[] { 5, 0 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void clearingPresetSlotUnlinksEditState() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		
		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
	
		pb.removeAllVersions(5);

		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void removingIntermediateSlotUpdatesEditState() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(5);		//	Version 0.
		pb.storeNewPresetVersion(5);		//	Version 1.
		pb.storeNewPresetVersion(5);		//	Version 2.

		assertArrayEquals(new int[] { 5, 2 }, pb.getSelectionOriginalLocation());

		pb.deleteVersion(5, 1);

		assertArrayEquals(new int[] { 5, 1 }, pb.getSelectionOriginalLocation());
	}
	
	@Test
	public void clearAllResetsEditState() {
		PrincipalPresetManager pb = make();
		pb.storeNewPresetVersion(4);
		
		assertArrayEquals(new int[] { 4, 0 }, pb.getSelectionOriginalLocation());

		
		pb.clearAll();

		assertArrayEquals(new int[] { -1, -1 }, pb.getSelectionOriginalLocation());
	}

	@Test
	public void canResize() {
		PrincipalPresetManager pb = make();
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
	public void editStateLinkageFollowsRetrievedVersion() {
		PrincipalPresetManager b = make();
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
		PrincipalPresetManager b = make();
		EditBuffer eb = new EditBuffer();
		eb.setNumScalarParams(1);
		eb.scalars[0].x = 4.9f;
		IPrincipalPreset pp = b.examineEditBuffer();
		pp.getMorphingVector(0).setKeyState(0, 0, eb);
		b.storeNewPresetVersion(7);
		
		PrincipalPresetManager b2 = new Cloner<PrincipalPresetManager>().deepCopy(b);
		b2.retrievePresetObject(7, 0);
		
		IEditState state = b2.examineMorphState(0, 0.0f);
		assertEquals(4.9f, state.getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void numPresetsOK() {
		assertEquals(13, new PrincipalPresetManager(13, 1).getNumPresetSlots());
	}
	
	@Test
	public void clearAll() {
		PrincipalPresetManager b = make();
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
		PrincipalPresetManager b = make();
		b.storeNewPresetVersion(0);
		b.setName(0, 0, "fooble");
		assertEquals("fooble", b.getName(0, 0));
	}
	
	@Test
	public void canRetrieveNewPresetByUUID() {
		PrincipalPresetManager bank = make();
		
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
		PrincipalPresetManager bank = make();
		
		UUID id = bank.storeNewPresetVersion(7);
		UUID id2 = bank.getUUID(7, 0);			//	Slot 7, initial version.
		assertEquals(id, id2);
	}
	
	@Test
	public void trackLastSlotWritten() {
		PrincipalPresetManager bank = make();
		bank.storeNewPresetVersion(7);
		
		assertEquals(7, bank.getLastSlotAccessed());
	}
	
	@Test
	public void trackLastSlotRead() {
		PrincipalPresetManager bank = make();

		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(8);

		bank.retrievePresetObject(7, 1);
		assertEquals(7, bank.getLastSlotAccessed());
	}

	@Test
	public void trackLastSlotReplaced() {
		PrincipalPresetManager bank = make();

		bank.storeNewPresetVersion(7);
		bank.storeNewPresetVersion(9);
		bank.replacePreset(7, 0);

		assertEquals(7, bank.getLastSlotAccessed());
	}
	
	@Ignore
	public void canPrettyPrint() {
		PrincipalPresetManager bank = make();
		final List<String> lines = new ArrayList<String>();

		bank.prettyprint(new IMultiLinePrinter() {
			@Override
			public void printLine(String line) {
				lines.add(line);
			}
		});
		
		assertEquals(lines.get(0), "0: ---");
	}
	
	@Test
	public void canRetrieveByUUID() {
		PrincipalPresetManager man = new PrincipalPresetManager(10, 1);
		man.storeNewPresetVersion(5);
		UUID id = man.getUUID(5, 0);
		int[] pos = man.findByUUID00(id);
		
		assertEquals(5, pos[0]);
		assertEquals(0, pos[1]);
	}
	
	@Test
	public void respondsToEditsFromSecondary() {
		PrincipalPresetManager pman = new PrincipalPresetManager(10, 1);
		IPrincipalPreset eb = pman.examineEditBuffer();
		IZone vec = eb.getMorphingVector(0);
		vec.setKeyState(0, 0, new EditBuffer());
		
		pman.setNotifiables(new HashSet<IEditChangeNotifiable>());
		pman.switchToBufferForZone(0);
		SecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);
		sman.handleVectorLocally(14, new float[] { 1.2f, 3.4f });
		
		IEditState state = vec.getDiscreteEditState00(0);
		assertArrayEquals(new float[] { 1.2f, 3.4f }, state.getVector(14).x, TestingManifest.JUNIT_DELTA);
	}

	@Test
	public void respondsToStateFromSecondary() {
		PrincipalPresetManager pman = new PrincipalPresetManager(10, 1);
		IPrincipalPreset eb = pman.examineEditBuffer();
		IZone vec = eb.getMorphingVector(0);
		vec.setKeyState(0, 0, new EditBuffer());

		pman.setNotifiables(new HashSet<IEditChangeNotifiable>());
		pman.switchToBufferForZone(0);

		SecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);

		EditBuffer b = new EditBuffer();
		b.allowScalarParam(0);
		b.getScalar(0).x = 4.7f;
		sman.handleSeqEditState(b);
		
		IEditState es = eb.getMorphingVector(0).getDiscreteEditState00(0);
		assertEquals(4.7f, es.getScalar(0).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void morphsAfterSecondaryInput() {
		IPrincipalPresetManager pman = new PrincipalPresetManager(10, 1);
		IPrincipalPreset eb = pman.examineEditBuffer();

		eb.setZoningEnableForPanel(0, true);

		IZone vec = eb.getMorphingVector(0);
		
		vec.setKeyState(0, 0f, new EditBuffer());
		vec.setKeyState(1, 1f, new EditBuffer());

		pman.setNotifiables(new HashSet<IEditChangeNotifiable>());
		pman.switchToBufferForZone(0);

		SecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);

		sman.handleVectorLocally(14, new float[] { 1.1f, 2.2f });
		
		pman.switchToBufferForZone(1);
		sman.handleVectorLocally(14, new float[] { 11.1f, 22.2f });
		
		eb.setZoneBarrierEnable(1, true);
		eb.setZoneBarrierPosition(1, 1.0f);

		IEditState state = pman.examineMorphState(0, 0.5f);
		assertArrayEquals(new float[] { 6.1f, 12.2f },
						  state.getVector(14).x,
						  TestingManifest.JUNIT_DELTA);
	}
}
