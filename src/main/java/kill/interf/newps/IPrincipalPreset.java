package kill.interf.newps;

import java.io.Serializable;

import kill.interf.IEditState;
import kill.m.dispatch.INonZoneableCollector;
import kill.util.IPrettyPrintable;

public interface IPrincipalPreset extends IDeepCloneable<IPrincipalPreset>,
										  IBasicPresetState,
										  IPrettyPrintable,
										  Serializable
{
	/**	Get the zone vector for a specific secondary. */
	IZone getMorphingVector(int panel);

	/** Set selection of a particular set of edit buffers at a particular zone slot. */
	void selectDiscreteBufferForZoneAcrossAllPanels(int selection);

	/** Get an edit buffer for a specific secondary at the current zone position. We
		clone an existing (next lower) state to this position if there isn't one already. */
	IEditState getSelectedEditStateForSecondary(int panel);

	/** Clone all edit buffers at current zone position to a new position. */
	void cloneSelectedEditStatesForSecondary(int pos);
	/**	When updating an old-format file: set an edit state. */
	void setSelectedUnzonedEditStateForSecondary(int panel, IEditState state);

	/**	Set the barrier position for this zone for all panels which aren't
		{@link kill.presetmanager.newps.SingleStateZone}. */

	void setZoneBarrierPosition(int zone, float position);

	/**	Turn on or off zoning support for this panel. No effect on the preset data if the panel
		is already in this mode.
	*/

	void setZoningEnableForPanel(int panel, boolean how);

	/** Enable or disable a particular zone in an entire set of morphing vectors
		(rather than leave it empty).

		@param zone the index of the zone to enable or disable
		@param how true to enable, false to disable
	*/

	void setZoneBarrierEnable(int zone, boolean how);

	/**	@see kill.interf.newps.IPrincipalPresetManager#copyFromCurrent(int) */
	void copyFromCurrent(int target);

	/** @see kill.interf.newps.IPrincipalPresetManager#distributeNonZoneableParams(INonZoneableCollector) */
	void distributeNonZoneableParams(INonZoneableCollector nonZoneables);
}
