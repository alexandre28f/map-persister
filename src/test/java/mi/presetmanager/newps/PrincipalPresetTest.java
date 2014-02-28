package mi.presetmanager.newps;

import static org.junit.Assert.*;
import mi.basicdata.ScalarParam;
import mi.basicdata.VectorParam;
import mi.interf.IEditState;
import mi.interf.newps.IPrincipalPreset;
import mi.interf.newps.IZone;
import mi.presetmanager.EditBuffer;
import mi.util.TestingManifest;

import org.junit.Ignore;
import org.junit.Test;

public class PrincipalPresetTest {
	@Test
	public void hasFirstEditBuffer() {
		IPrincipalPreset preset = new PrincipalPreset(5, true);
		preset.getSelectedEditStateForSecondary(0);
	}
	
	@Test
	public void canSelectOccupiedZoneState() {
		IPrincipalPreset preset = new PrincipalPreset(5, true);
		preset.selectDiscreteBufferForZoneAcrossAllPanels(0);
	}
	
	@Test
	public void canPlantSecondEditBuffer() {
		IPrincipalPreset preset = new PrincipalPreset(1, true);
		preset.cloneSelectedEditStatesForSecondary(1);
		preset.selectDiscreteBufferForZoneAcrossAllPanels(1);
		preset.getSelectedEditStateForSecondary(0);
	}
	
	@Test
	public void secondBufferIsCloneOfFirst() {
		IPrincipalPreset preset = new PrincipalPreset(1, true);
		IEditState state = preset.getSelectedEditStateForSecondary(0);
		
		state.allowVectorParam(3);
		VectorParam vp = new VectorParam();
		vp.x = new float[] { 1.1f, 2.2f };
		state.putVector(3, vp);
		
		preset.cloneSelectedEditStatesForSecondary(1);
		preset.selectDiscreteBufferForZoneAcrossAllPanels(1);
		IEditState state2 = preset.getSelectedEditStateForSecondary(0);
		assertArrayEquals(vp.x, state2.getVector(3).x, TestingManifest.JUNIT_DELTA);
		
		vp.x[1] = 4.5f;
		vp.x = new float[] { 6.6f, 7.7f };
		state.putVector(3, vp);
		assertArrayEquals(new float[] { 1.1f, 2.2f }, state2.getVector(3).x, TestingManifest.JUNIT_DELTA);		
	}
	
	@Test
	public void canIncreaseNumberOfPanels() {
		IPrincipalPreset preset = new PrincipalPreset(1, true);
		IEditState state = preset.getSelectedEditStateForSecondary(0);
		preset.getSelectedEditStateForSecondary(17);
		assertSame(state, preset.getSelectedEditStateForSecondary(0));
	}
	
	@Test
	public void canPromoteUnzonedToZoned() {
		final int PANEL = 0;
		final int PARAM = 0;
		
		final float F1 = 14, F2 = 24;
		
		IPrincipalPreset preset = new PrincipalPreset(2, false);
		IEditState state = new EditBuffer();
		
		state.allowScalarParam(PARAM);
		state.putScalar(PARAM, new ScalarParam(F1));

		//	Place this edit state into the preset:
		preset.setSelectedUnzonedEditStateForSecondary(PANEL, state);
		//	Turn on zoning!
		preset.setZoningEnableForPanel(PANEL, true);
		preset.setZoneBarrierPosition(1, 1.0f);		//	Second zone barrier at rightmost point.

		preset.selectDiscreteBufferForZoneAcrossAllPanels(1);
		//	This will assign a clone of the zero'th edit buffer:
		preset.getSelectedEditStateForSecondary(PANEL).getScalar(PARAM).x = F2;

		state = preset.getMorphingVector(PANEL).generateMorphedState(0.5f);
		assertEquals((F1 + F2) / 2f, state.getScalar(PARAM).x, TestingManifest.JUNIT_DELTA);
	}
	
	@Test
	public void canDemoteZonedToUnzoned() {
		final int PANEL = 0;
		final int PARAM = 0;
		
		final float F1 = 14, F2 = 24;
		
		IPrincipalPreset preset = new PrincipalPreset(2, true);
		IEditState state;
		
		preset.selectDiscreteBufferForZoneAcrossAllPanels(0);
		state = preset.getSelectedEditStateForSecondary(PANEL);
		state.allowScalarParam(PARAM);
		state.getScalar(PARAM).x = F1;
		
		preset.selectDiscreteBufferForZoneAcrossAllPanels(1);
		state = preset.getSelectedEditStateForSecondary(PANEL);
		state.allowScalarParam(PARAM);
		state.getScalar(PARAM).x = F2;
		
		preset.setZoneBarrierPosition(1, 1.0f);
	
		assertEquals((F1 + F2) / 2f,
					 preset.getMorphingVector(PANEL).generateMorphedState(0.5f).getScalar(PARAM).x,
					 TestingManifest.JUNIT_DELTA
					);

		preset.setZoningEnableForPanel(PANEL, false);

		assertEquals(F1,
					 preset.getMorphingVector(PANEL).generateMorphedState(0.5f).getScalar(PARAM).x,
					 TestingManifest.JUNIT_DELTA
					);
	}
}
