package kill.presetmanager.newps;

import kill.interf.IEditState;
import kill.interf.newps.IZone;
import kill.sys.XManifest;

/** Take an IZone, return a zone which can
 	take an input in an arbitrary domain rather than the normalised 0.0...1.0.
 	
 	<P>We don't know (ahead of time) the domain limits, and these are dynamic, so all
 	we can do is track them dynamically, and normalised based on what we've seen.
 	
 	<P>(If we only have one zone our domain calculations will fail, but this is a degenerate
 	case anyway for the underlying IZone.)
 	
 	<P>Note: we have to track zone enable/disable calls as well.

	@author nick
 */

public class ScaledZone {
	private IZone itsZone;
	private float[] itsDomainLocations;
	private float[] itsExtremes00 = null;

	public ScaledZone(IZone zone) {
		itsZone = zone;
		itsDomainLocations = new float[zone.getNumKeyStates()];
	}
	
	private float[] calculateExtremes00() {
		Float low = null, high = null;

		for (int i = 0; i < itsZone.getNumKeyStates(); i++) {
			if (itsZone.keyStateIsEnabled(i)) {
				float domainPt = itsDomainLocations[i];
				low = (low == null) ? domainPt : Math.min(low, domainPt);
				high = (high == null) ? domainPt : Math.max(high, domainPt);
			}
		}
		
		if (low == null || high == null) {
			return null;
		} else {
			return new float[] { low, high };
		}
	}
	
	/**	Recalibrate all zone positions based on new extremes.
	 */

	private void recalibrate() {
		itsExtremes00 = calculateExtremes00();
		
		if (itsExtremes00 != null) {
			for (int i = 0; i < itsZone.getNumKeyStates(); i++) {
				if (itsZone.keyStateIsEnabled(i)) {
					itsZone.setZoneBarrier(i, interpolate(itsExtremes00[0], itsDomainLocations[i], itsExtremes00[1]));
				}
			}
		}
	}

	private float interpolate(float low, float f, float high) {
		if (Math.abs(high - low) < XManifest.EPSILON) {
			return 0f;
		} else {
			return (f - low) / (high - low);
		}
	}

	private void ensureZone(int zone) {
		if (zone >= itsDomainLocations.length) {
			float[] newLocations = new float[zone + 1];
			System.arraycopy(itsDomainLocations, 0, newLocations, 0, itsDomainLocations.length);
			itsDomainLocations = newLocations;
		}
	}

	/**	Set this zone position. All other zones may have to move, to renormalise. We
	 	calculate the new normalised positions based on the current domain extremes
	 	plus this value.

		@param zone the zone index
		@param domainPosition the position in domain coordinates
	 */

	public void setZoneBarrier(int zone, float domainPosition) {
		itsZone.setZoneBarrier(zone, 0f);
		
		ensureZone(zone);
		itsDomainLocations[zone] = domainPosition;

		recalibrate();
	}
	
	/**	Calculate a morphed state from domain coordinates.

		@param domainPosition the domain position
		@return the morphed state
	 */

	public IEditState generateMorphedState(float domainPosition) {
		if (itsExtremes00 == null) {
			return itsZone.generateMorphedState(0f);
		} else { 
			return itsZone.generateMorphedState(interpolate(itsExtremes00[0], domainPosition, itsExtremes00[1]));
		}
	}

	public void enableKeyState(int i) {
		itsZone.enableKeyState(i);
		recalibrate();
	}
	
	public void disableKeyState(int i) {
		itsZone.disableKeyState(i);
		recalibrate();
	}
}
