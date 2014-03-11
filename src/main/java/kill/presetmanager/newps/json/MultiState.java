package mi.presetmanager.newps.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiState {
	@JsonCreator
	public MultiState(@JsonProperty("states") SingleZoneState[] states) {
		this.itsStates = states;
	}

	SingleZoneState[] itsStates;

	public SingleZoneState[] getStates() {
		return itsStates;
	}
}
