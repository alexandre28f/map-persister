package kill.presetmanager.newps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import kill.interf.IEditState;
import kill.interf.newps.IEditChangeNotifiable;
import kill.interf.newps.IPrincipalPreset;
import kill.interf.newps.IPrincipalPresetManager;
import kill.interf.newps.ISecondaryPresetManager;
import kill.interf.newps.ISecondaryPresetManagerIO;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.PrincipalPresetManager;
import kill.presetmanager.newps.SecondaryPresetManager;
import kill.presetmanager.newps.SecondaryPresetManagerIO;
import kill.util.IFileUtils;
import kill.util.TestingManifest;
import kill.util.VariousUtils;

import org.alexandrehd.presetter.legacy.ScalarParam;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class SecondaryPresetManagerIOTest {
	Mockery itsContext = new JUnit4Mockery();

	@Ignore
	public void ok() { }
	
	interface Foo extends IEditChangeNotifiable { }
	
	@Test
	public void testWriteRead() throws Exception {
		final IFileUtils fileUtils = itsContext.mock(IFileUtils.class);
		final IEditChangeNotifiable listener = itsContext.mock(IEditChangeNotifiable.class);
		final Foo listener2 = itsContext.mock(Foo.class);
		
		final File foo = File.createTempFile("FOO", ".tmp");
		foo.deleteOnExit();
	
		itsContext.checking(new Expectations() {{
			exactly(2).of(fileUtils).locateFromStem00("LOCATION", "myfile", ".s_presets");
			will(returnValue(foo));
			
			one(listener).editBufferHasChanged();
			one(listener2).editBufferHasChanged();
		}});

		ISecondaryPresetManagerIO io =
				new SecondaryPresetManagerIO(fileUtils, "LOCATION");

		IPrincipalPresetManager pman = new PrincipalPresetManager(1, 1);
		pman.setNotifiables(VariousUtils.makeSet(listener));
		IPrincipalPreset eb = pman.examineEditBuffer();
		eb.getMorphingVector(0).setKeyState(0, 0, new EditBuffer());
		pman.switchToBufferForZone(0);
		ISecondaryPresetManager bank = new SecondaryPresetManager(0, 20, pman, listener, null);

		bank.setNumScalarParams(8);
		bank.handleScalarLocally(7, 4.7f);
		bank.storeNewPresetVersion(14);
		io.write(bank, "myfile");
		
		ISecondaryPresetManager bank2 = io.bareBonesRead("myfile");
		bank2.reconstitute(pman, 0, listener2);
		bank2.doRetrievePreset(14, 0);

		IEditState state = bank2.examineEditBuffer();
		assertEquals(4.7f, state.getScalar(7).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Ignore
	public void replacingSecondaryWillRelinkWithPrincipal() throws Exception {
		final IEditChangeNotifiable listener = itsContext.mock(IEditChangeNotifiable.class);
		
		itsContext.checking(new Expectations() {{
			allowing(listener).editBufferHasChanged();
		}});

		IFileUtils fileUtils = new IFileUtils() {
			@Override
			public File locateFromStem00(String placeholderStem,
										 String stem,
										 String extension
										) {
				String path = "/public-test-data/" + stem + extension;
				URL resource = getClass().getResource(path);
				return new File(resource.getFile());
			}
		};
		
		ISecondaryPresetManagerIO io =
				new SecondaryPresetManagerIO(fileUtils, "BOGUS");

		IPrincipalPresetManager pman = new PrincipalPresetManager(1, 1);
		ISecondaryPresetManager sman = new SecondaryPresetManager(0, 20, pman, null, null);
		pman.setNotifiables(VariousUtils.makeSet(listener));

		IPrincipalPreset eb = pman.examineEditBuffer();
		eb.setZoningEnableForPanel(0, true);

		//	Basic state in zone 0:
		IEditState state = sman.examineEditBuffer();
		state.allowScalarParam(4);
		state.putScalar(3, new ScalarParam(0.1f));
		state.putScalar(4, new ScalarParam(0.2f));
		eb.getMorphingVector(0).setEditBuffer(0, state);

		//	Clone initial state to a second zone position for this preset:
		pman.cloneSelectedEditStatesToZonePosition(1);
		
		// Switch secondaries to this zone:
		pman.switchToBufferForZone(1);
		
		//	Plant some state in zone 1:
		state = sman.examineEditBuffer();
		state.allowScalarParam(4);
		state.putScalar(3, new ScalarParam(990f));
		state.putScalar(4, new ScalarParam(999f));
		
		//	Back to zone 0:
		pman.switchToBufferForZone(0);
		state = sman.examineEditBuffer();
		assertEquals(0.1f, state.getScalar(3).x, TestingManifest.JUNIT_DELTA);
		assertEquals(0.2f, state.getScalar(4).x, TestingManifest.JUNIT_DELTA);
		
		//	Read secondary from disk:
		ISecondaryPresetManager sman2 =
				io.bareBonesRead("s3-5.0-and-s4-10.0.newformat");
		sman2.reconstitute(pman, 0, listener);		
		//	Are we still looking at the same edit buffer?
		state = sman2.examineEditBuffer();
		assertEquals(0.1f, state.getScalar(3).x, TestingManifest.JUNIT_DELTA);
		assertEquals(0.2f, state.getScalar(4).x, TestingManifest.JUNIT_DELTA);

		//	Retrieve and check the newly read data (while overwriting primary at zone 0):
		sman2.doRetrievePreset(0, 0);
		state = sman2.examineEditBuffer();
		assertEquals( 5.0f, state.getScalar(3).x, TestingManifest.JUNIT_DELTA);
		assertEquals(10.0f, state.getScalar(4).x, TestingManifest.JUNIT_DELTA);
		
		//	Switch to zone 1 again:
		pman.switchToBufferForZone(1);
		
		//	Check that the secondary is now pointing at the new data:
		state = sman2.examineEditBuffer();
		assertEquals(990f, state.getScalar(3).x, TestingManifest.JUNIT_DELTA);
		assertEquals(999f, state.getScalar(4).x, TestingManifest.JUNIT_DELTA);
	}
}
