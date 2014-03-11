package mi.presetmanager.newps;

import mi.interf.IEditState;
import mi.interf.newps.IZone;
import mi.interf.newps.IZoneableParameter;
import mi.presetmanager.EditBuffer;
import mi.presetmanager.newps.json.PanelPresetState;
import mi.util.IMultiLinePrinter;

/**	I was about to deprecate this (because a multistate zone handler with a single zone serves the same purpose),
	but the getDiscreteEditState00() method needs to always return the single edit state. (MultiZones would
	not do that.)

	@author nick
 */

public class SingleStateZone implements IZone {
	private static final long serialVersionUID = 1L;
	private IEditState itsState;

	public SingleStateZone(IEditState state) {
		itsState = state;
	}

	public SingleStateZone() {
		this(new EditBuffer());
	}
	
	public SingleStateZone(MultiStateZone multi) {
		this(multi.getDiscreteEditState00(0));
	}

	@Override
	public IEditState generateMorphedState(float pos) {
		return itsState.deepClone();
	}


	@Override
	public IEditState generateMorphedState(Class<? extends IZoneableParameter> scalarEnumClass,
										   Class<? extends IZoneableParameter> vectorEnumClass,
										   float xfade,
										   int defaultEditStateForNonZoned
										  ) {
		return generateMorphedState(0f);
	}

	@Override
	public IEditState getDiscreteEditState00(int pos) {
		return itsState/*.deepClone()*/;
	}

	@Override
	public void setKeyState(int i, float position, IEditState b1) {
		itsState = b1.deepClone();
	}

	@Override
	public void disableKeyState(int i) { }

	@Override
	public void enableKeyState(int i) { }

	@Override
	public void setEditBuffer(int zone, IEditState state) {
		itsState = state;
	}

	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		printer.printLine("[SingleStateZone]");
	}

	@Override
	public void setZoneBarrier(int zone, float position) {
		//	No effect.
	}

	@Override
	public boolean zoningSupported() { return false; }

	@Override
	public float examineScalarMorphState(int param, float xfade) {
		return itsState.getScalar(param).x;
	}

	@Override
	public float[] examineVectorMorphState(int param, float xfade) {
		return itsState.getVector(param).x;
	}

	@Override
	public boolean keyStateIsEnabled(int i) {
		return true;
	}

	@Override
	public int getNumKeyStates() {
		return 1;
	}

	/**	Distribute zone state: no-op for single state zones.
		@see mi.interf.newps.IZone#distributeSpecifiedState(int, Enum[], Enum[])
	 */

	@Override
	public void distributeSpecifiedState(int sourceZone,
										 Enum<?>[] nonZoneableScalars,
										 Enum<?>[] nonZoneableVectors) { }

	/**	Copy zone state: no-op for single state zones.
		@see mi.interf.newps.IZone#copyEntireState(int, int)
	 */
	
	@Override
	public void copyEntireState(int sourceZone, int targetZone) { }

	/**	@see mi.interf.newps.IZone#exportState() */
	@Override
	public PanelPresetState exportState() {
		return new PanelPresetState(itsState.exportState(), null);
	}
}
