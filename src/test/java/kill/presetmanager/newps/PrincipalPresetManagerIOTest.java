package mi.presetmanager.newps;

import java.io.File;
import java.util.HashSet;

import mi.interf.IEditState;
import mi.interf.newps.IEditChangeNotifiable;
import mi.interf.newps.IPrincipalPreset;
import mi.interf.newps.IPrincipalPresetManager;
import mi.interf.newps.IPrincipalPresetManagerIO;
import mi.interf.newps.ISecondaryPresetManager;
import mi.presetmanager.EditBuffer;
import mi.util.IFileUtils;

import org.alexandrehd.presetter.legacy.ScalarParam;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class PrincipalPresetManagerIOTest {
	Mockery itsContext = new JUnit4Mockery();

	interface Foo extends ISecondaryPresetManager { };
	interface Goo extends ISecondaryPresetManager { };

	@Test
	public void presetBankReadRefreshesSecondaries() throws Exception {
		final IFileUtils fileUtils = itsContext.mock(IFileUtils.class);
		
		final float[] target = new float[] { 0.0f, 0.0f };

		final Foo listener0 = itsContext.mock(Foo.class);
		final Goo listener1 = itsContext.mock(Goo.class);
		
		final File foo = File.createTempFile("FOO", ".tmp");
		foo.deleteOnExit();
	
		itsContext.checking(new Expectations() {{
			exactly(2).of(fileUtils).locateFromStem00("LOCATION", "myfile", ".p_presets");
			will(returnValue(foo));
			one(listener0).editBufferHasChanged();
			one(listener1).editBufferHasChanged();
		}});

		IPrincipalPresetManagerIO bankIO =
			new PrincipalPresetManagerIO(fileUtils, "LOCATION");

		IPrincipalPresetManager pman = new PrincipalPresetManager(1, 2);
		
		HashSet<IEditChangeNotifiable> notifiables = new HashSet<IEditChangeNotifiable>();
		notifiables.add(listener0);
		notifiables.add(listener1);
		pman.setNotifiables(notifiables);
		
		IPrincipalPreset eb = pman.examineEditBuffer();
		eb.getMorphingVector(0).setKeyState(0, 0, new EditBuffer());
		pman.switchToBufferForZone(0);
		IEditState state = pman.getCurrentEditBuffer(0);

		state.setNumScalarParams(8);
		state.putScalar(7, new ScalarParam(4.7f));

		state = pman.getCurrentEditBuffer(1);
		state.putScalar(7, new ScalarParam(9.9f));

		bankIO.write(pman, "myfile");
		
		/*ignore*/ bankIO.bareBonesRead("myfile");
	}
}
