package kill.interf.newps;

import java.util.Set;

import kill.interf.IEditState;
import kill.m.dispatch.INonZoneableCollector;

/**	This preset manager responds to edit state and param changes because it takes a feed
		from its secondary to keep its vector updated.

		@author nick
 */

public interface IPrincipalPresetManager extends IPresetManager,
												 IPresetFrame<IPrincipalPreset>
{
	IEditState examineMorphState(int panel, float xfade);

	IEditState getCurrentEditBuffer(int panel);

	/**	Switch to a discrete edit buffer for a particular zone. Cause the new edit buffer
				to be transmitted to the receiver for all panels.

				@param zone
	 */

	void switchToBufferForZone(int zone);
	void cloneSelectedEditStatesToZonePosition(int pos);

	IPrincipalPreset getCurrentPreset();

	/**	Distribute non-zoneable parameters from the currently selected edit zone to all other
				zones, for all panels (those which are zoneable).

				@param nonZoneables callback to determine non-zoneables for every panel
	 */

	void distributeNonZoneableParams(INonZoneableCollector nonZoneables);

	/**	Copy all parameters from the currently selected edit zone to a different target, for
				all panels which are zoneable.

				@param target
	 */

	void copyFromCurrent(int target);

	void setNotifiables(Set<IEditChangeNotifiable> notifiables);
	
	/**	Inject a principal preset (perhaps from some serialised source) into the
 		current live state

		@param preset a principal preset
	 */

	public void injectPreset(IPrincipalPreset preset);
}
