package mi.interf.newps;

import java.io.Serializable;

import mi.interf.IEditState;
import mi.interf.newps.IZoneableParameter;
import mi.presetmanager.newps.json.PanelPresetState;
import mi.util.IPrettyPrintable;

public interface IZone extends Serializable, IPrettyPrintable {
	/**	Morph a state somewhere along the vector. Return the single edit state if
		there's only one (so, don't interpolate towards empty). Return null if there
		are no edit states (so, don't return an initial state).
		
		<P>Takes a enum type for the parameters to determine whether they should be
		zoned. If not, return a value from a specific fixed edit state.
		
		@param scalarParams the enum type for determining which scalar parameters are zoned
		@param vectorParams the enum type for determining which vector parameters are zoned
		
		@param xfade the position in the vector, 0.0f to 1.0f
		
		@param defaultEditStateForNonZoned the index of the edit state to use for
		non-zoned parameters

		@return a generated morphed edit state
	 */

	IEditState generateMorphedState(Class<? extends IZoneableParameter> scalarParams,
									Class<? extends IZoneableParameter> vectorParams,
									float xfade,
									int defaultEditStateForNonZoned
								   );
	
	
	/**	A degenerate form of interpolation where every parameter is zoneable.

		@param xfade the position in the vector, 0.0f to 1.0f
		@return	 a generated morphed edit state
	 */

	IEditState generateMorphedState(float xfade);
	
	IEditState getDiscreteEditState00(int pos);
	void setKeyState(int i, float position, IEditState b1);
	void disableKeyState(int i);
	void enableKeyState(int i);
	boolean keyStateIsEnabled(int i);

	/**	Just set the edit buffer at a zone pos (assume it's already cloned).
	 */

	void setEditBuffer(int zone, IEditState state);
	void setZoneBarrier(int zone, float position);
	boolean zoningSupported();
	@Deprecated float examineScalarMorphState(int param, float xfade);
	@Deprecated float[] examineVectorMorphState(int param, float xfade);
	int getNumKeyStates();
	void distributeSpecifiedState(int sourceZone, Enum<?>[] nonZoneableScalars, Enum<?>[] nonZoneableVectors);
	void copyEntireState(int sourceZone, int targetZone);


	/**	Export state for JSON: either a single edit buffer or a zoned set of buffers.

		@return the state of the zone
	 */

	PanelPresetState exportState();
}
